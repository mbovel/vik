package io.bovel.vik

import scala.collection.mutable.{ArrayBuffer, HashMap, HashSet}

import io.bovel.smtlib.{ArithOp, BvCmpOp, BvOp, Sort}
import io.bovel.smtlib as s2
import Formula.*
import Term.*

/** Checks a lowering with proofs on: replays [[Lowering]] on the surface
 *  commands, as a function of them and of the symbols the lowering minted
 *  ([[Lowering.Intro]]), and checks that the lowered commands are exactly that.
 *  The replay mints nothing: where the lowering introduces fresh constants, it
 *  looks up the intro of the rule, and the first use of an intro checks it —
 *  its symbols new, declared as the rule declares them, and its defining
 *  assertions those of the rule. Every assertion the lowering sends is then
 *  either the lowering of a surface assertion or a defining assertion of a rule
 *  the replay used, so a refutation of the lowered assertions is one of the
 *  surface script, given the rules: each rule's definitions are satisfied by
 *  the values its symbols stand for (the overflow of a sum, the bits of a term,
 *  the branch of an `ite`), and each lowered term is the value of its surface
 *  term — for a bit-vector, its unsigned value. Those are the per-rule lemmas a
 *  proof checker states once.
 *
 *  The replay follows the lowering's decisions (which side of a product to
 *  decompose, when an equality goes bit by bit), and keeps what they depend on
 *  per level as the lowering does; it shares none of its state.
 */
final class LoweringCheck:
  import LoweringCheck.Failed
  import Lowering.Intro

  private enum Value:
    case T(term: Term, sort: Sort)
    case F(formula: Formula)
  private type Env = Map[String, Value]

  private def fail(msg: String): Nothing = throw Failed(msg)

  // The levels, as the lowering keeps them: one list of undos per open level.
  private val scopes = ArrayBuffer.empty[ArrayBuffer[() => Unit]]
  private var globalDeclarations = false

  /** A table whose entries go with the level that made them, as the lowering's
   *  memos do.
   */
  private final class Scoped[K, V]:
    private val entries = HashMap.empty[K, V]
    def get(key: K): Option[V] = entries.get(key)
    def contains(key: K): Boolean = entries.contains(key)
    def apply(key: K): V = entries(key)
    def update(key: K, value: V): Unit =
      if !entries.contains(key) then
        if scopes.nonEmpty then scopes.last += (() => entries -= key)
        entries(key) = value
    def getOrElseUpdate(key: K, value: => V): V = entries.get(key) match
      case Some(v) => v
      case None =>
        val v = value
        update(key, v)
        v
    def clear(): Unit = entries.clear()

  private val functions = HashMap.empty[String, Sort]
  private val macros = HashMap.empty[String, (List[(String, Sort)], Sort, s2.Term | s2.Formula)]
  private def undoable[V](map: HashMap[String, V], key: String): Unit =
    if scopes.nonEmpty && !globalDeclarations then
      val previous = map.get(key)
      scopes.last += (() => previous.fold(map -= key)(v => map(key) = v))

  /** Every name declared so far, and the lowering's own: a minted symbol must
   *  be none of them.
   */
  private val declared = HashSet.empty[String]
  private val minted = HashSet.empty[String]

  private val intros = Scoped[(String, List[Any]), Intro]()
  private val stores = HashMap.empty[String, Intro] // the store symbols stay, as the lowering's do
  private val checked = java.util.IdentityHashMap[Intro, Unit]()
  private val rationalValues = HashMap.empty[Term, (BigInt, BigInt)]
  private val knownBitsOf = Scoped[(Term, Int), IndexedSeq[Formula]]()
  private val bitConstants = Scoped[Formula, Term]()

  // The facts and declarations of the intros checked during the current command.
  private val facts = ArrayBuffer.empty[Formula]
  private val declarations = ArrayBuffer.empty[(String, String)]
  private var current = Set.empty[Intro]

  private def clearMemos(): Unit =
    intros.clear()
    knownBitsOf.clear()
    bitConstants.clear()

  /** Checks one surface command against its lowering and the intros the
   *  lowering recorded for it: `None` if they agree, what is wrong otherwise.
   */
  def command(c: s2.Command, lowered: List[Command], made: List[Intro]): Option[String] =
    facts.clear()
    declarations.clear()
    current = made.toSet
    try
      for intro <- made do register(intro)
      val expected = replay(c)
      c match
        case s2.Command.GetValue(_) =>
          // Nothing is asserted: the lowered items only answer the query. The minted symbols are declared.
          lowered.lastOption match
            case Some(Command.GetValue(items, _)) =>
              if items.map(_._2) != expected then fail("the items of get-value are not their lowering")
            case _ => fail("get-value lowered to something else")
          for intro <- made; s <- intro.symbols do declared += s
          clearMemos()
        case _ =>
          val unused = made.filterNot(checked.containsKey)
          if unused.nonEmpty then fail(s"symbols minted by no rule of the replay: ${unused.mkString(", ")}")
          val main = expected.asInstanceOf[List[Command]]
          val (prefix, rest) = lowered.splitAt(lowered.length - main.length)
          if rest != main then fail(s"not the lowering of $c: ${rest.mkString("; ")}")
          val decls = prefix.collect { case Command.DeclareFun(n, Nil, s) => (n, s) }
          val asserts = prefix.collect { case Command.Assert(f) => f }
          if decls.length + asserts.length != prefix.length then fail("the lowering sent other commands")
          if decls.sorted != declarations.toList.sorted then
            fail(s"declarations ${decls.mkString(", ")}, expected ${declarations.mkString(", ")}")
          if !sameElements(asserts, facts.toList) then fail("the defining assertions are not those of the rules")
          for (n, _) <- decls do declared += n
      None
    catch case Failed(msg) => Some(msg)

  private def sameElements(a: List[Formula], b: List[Formula]): Boolean =
    a.length == b.length &&
      a.groupBy(identity).view.mapValues(_.length).toMap ==
      b.groupBy(identity).view
        .mapValues(_.length)
        .toMap

  private def register(intro: Intro): Unit =
    for s <- intro.symbols do
      if declared(s) || minted(s) then fail(s"$s is not a new symbol")
      minted += s
    if intro.kind == "store" then stores(intro.key.head.asInstanceOf[String]) = intro
    else intros((intro.kind, intro.key)) = intro
    intro.kind match
      case "rat" =>
        val List(p: BigInt, q: BigInt) = intro.key: @unchecked
        rationalValues(Const(intro.symbols.head)) = (p, q)
      case _ => ()
    intro.kind match
      case "ite" => functions(intro.symbols.head) = intro.args.head.asInstanceOf[Sort]
      case "floor" => functions(intro.symbols.head) = Sort.Int
      case "rat" | "ratmul" => functions(intro.symbols.head) = Sort.Real
      case "store" => functions(intro.symbols.head) = Sort.Named(intro.symbols.head)
      case _ => ()

  /** The symbols of the intro of the rule `kind` at `key`, checked at its first
   *  use: `args` are what the replay applies the rule to beyond the key.
   */
  private def use(kind: String, key: List[Any], args: List[Any] = Nil): List[Term] =
    val intro =
      (if kind == "store" then stores.get(key.head.asInstanceOf[String]) else intros.get((kind, key)))
        .getOrElse(fail(s"no $kind for ${key.mkString(", ")}"))
    if intro.args != args then fail(s"the $kind of ${key.mkString(", ")} is of ${intro.args}, not $args")
    if !checked.containsKey(intro) then
      checked.put(intro, ())
      if !current(intro) then fail(s"the $kind of ${key.mkString(", ")} is not of this level")
      check(intro)
    intro.symbols.map(Const(_))

  /** The rule's declarations and definitions for `intro`. */
  private def check(intro: Intro): Unit =
    val ks = intro.symbols.map(Const(_))
    def int(k: Term, lo: BigInt, hi: BigInt): Unit =
      declare(k, "Int")
      facts += Ge(k, Num(lo))
      facts += Le(k, Num(hi))
    def declare(k: Term, sort: String): Unit = declarations += ((k.toString, sort))
    def arity(n: Int): Unit = if ks.length != n then fail(s"${intro.kind} with ${ks.length} symbols")
    (intro.kind, intro.key, intro.args) match
      case ("ite", List(c: Term, t: Term, e: Term), List(sort: Sort)) =>
        arity(1)
        val k = ks.head
        val cond = Formula.pred(c)
        sort match
          case Sort.BitVec(w) =>
            declare(k, "Int")
            facts += range(k, w)
          case _ => declare(k, sortString(sort))
        facts += Implies(cond, equal(sort, k, t))
        facts += Implies(Not(cond), equal(sort, k, e))
      case ("floor", List(r: Term), Nil) =>
        arity(1)
        declare(ks.head, "Int")
        facts += Le(ks.head, r)
        facts += Less(r, Term.linear(List((BigInt(1), ks.head)), 1))
      case ("rat", List(p: BigInt, q: BigInt), Nil) if q.signum != 0 =>
        arity(1)
        declare(ks.head, "Real")
        facts += IntEq(Term.linear(List((q, ks.head))), Num(p))
      case ("ratmul", List(p: BigInt, q: BigInt, t: Term), Nil) if q.signum != 0 =>
        arity(1)
        declare(ks.head, "Real")
        facts += IntEq(Term.linear(List((q, ks.head))), Term.linear(List((p, t))))
      case ("store", List(key: String), Nil) =>
        arity(1)
        val n = intro.symbols.head
        if !n.startsWith("store!") then fail(s"the store symbol $n")
        val (params, _) = s2.Sorts.arrow(key).getOrElse(fail(s"store on $key"))
        declarations += ((n, s2.Sorts.function(key :: params.head :: List(s2.Sorts.applied(key, 1)), key)))
      case ("range", List(t: Term), List(w: Int)) =>
        arity(0)
        facts += range(t, w)
      case ("modulo", List(value: Term, w: Int, lo: BigInt, hi: BigInt), Nil) =>
        val m = modulus(w)
        int(ks.head, 0, m - 1)
        val r = ks.head
        if hi - lo < splitCases then
          arity(1)
          facts += Or((lo to hi).toList.map(k => IntEq(value, r + Num(k * m))))
        else
          arity(2)
          int(ks(1), lo, hi)
          facts += IntEq(value, r + ks(1) * m)
      case ("cases", List(_: String, c: Term, thn: Term, els: Term, lo: BigInt, hi: BigInt), Nil) =>
        arity(1)
        val cond = Formula.pred(c)
        int(ks.head, lo, hi)
        facts += Implies(cond, IntEq(ks.head, thn))
        facts += Implies(Not(cond), IntEq(ks.head, els))
      case ("divide", List(value: Term, d: BigInt, qmax: BigInt), Nil) if d.signum > 0 =>
        arity(2)
        int(ks(0), 0, qmax)
        int(ks(1), 0, d - 1)
        facts += IntEq(value, ks(0) * d + ks(1))
      case ("udiv", List(_, _, _), List(a: Term, b: Term, w: Int)) =>
        arity(2)
        val m = modulus(w)
        val (q, r) = (ks(0), ks(1))
        int(q, 0, m - 1)
        int(r, 0, m - 1)
        val zero = IntEq(b, Num(0))
        facts += Implies(zero, And(List(IntEq(q, Num(m - 1)), IntEq(r, a))))
        facts += Implies(Not(zero), And(List(IntEq(a, exactProduct(b, q, w) + r), Less(r, b))))
      case ("sdivide", List(t: Term, w: Int, d: BigInt, floor: Boolean), Nil) if d.signum > 0 =>
        arity(2)
        val half = modulus(w - 1)
        val (q, r) = (ks(0), ks(1))
        int(q, -(half / d) - 1, (half - 1) / d)
        int(r, if floor then 0 else -(d - 1), d - 1)
        facts += IntEq(signed(t, w), q * d + r)
        if !floor then
          facts += Implies(negative(t, w), Le(r, Num(0)))
          facts += Implies(Not(negative(t, w)), Ge(r, Num(0)))
      case ("bits", List(t: Term, w: Int), Nil) =>
        arity(w)
        for b <- ks do int(b, 0, 1)
        facts += IntEq(t, Term.linear(ks.zipWithIndex.map((b, i) => (modulus(i), b))))
        // Known from here on, as the lowering knows them once it decomposed `t`.
        val atoms = ks.map(b => Ge(b, Num(1))).toIndexedSeq
        for (f, b) <- atoms.zip(ks) do bitConstants(f) = b
        knownBitsOf((t, w)) = atoms
      case ("int2bv", List(t: Term, w: Int), Nil) =>
        arity(2)
        int(ks(0), 0, modulus(w) - 1)
        declare(ks(1), "Int")
        facts += IntEq(t, ks(0) + ks(1) * modulus(w))
      case ("smodz", List(a: Term, c: BigInt, w: Int), Nil) =>
        arity(1)
        val m = modulus(w)
        val t = if c >= m / 2 then c - m else c
        if t.signum == 0 then fail("smodz by zero")
        val r = divideSigned(a, w, t.abs)._2
        val differ = if t.signum > 0 then negative(a, w) else Not(negative(a, w))
        val z = ks.head
        int(z, 0, 1)
        facts += Implies(Not(differ), IntEq(z, Num(0)))
        facts += Implies(IntEq(r, Num(0)), IntEq(z, Num(0)))
        facts += Implies(And(List(differ, Not(IntEq(r, Num(0))))), IntEq(z, Num(1)))
      case _ => fail(s"not a rule: $intro")

  // The replay of the commands.

  private def replay(c: s2.Command): List[Command] | List[Formula | Term] = c match
    case s2.Command.SetLogic(logic) => List(Command.SetLogic(logic.replace("BV", "LIA")))
    case s2.Command.SetInfo(key, value) => List(Command.SetInfo(key, value))
    case s2.Command.DeclareSort(name, arity) => List(Command.DeclareSort(name, arity))
    case s2.Command.DeclareFun(name, argSorts, sort) =>
      if minted(name) then fail(s"$name is a minted symbol")
      undoable(functions, name)
      declared += name
      sort match
        case Sort.BitVec(w) if argSorts.isEmpty =>
          functions(name) = sort
          List(Command.DeclareFun(name, Nil, "Int"), Command.Assert(range(Const(name), w)))
        case _ =>
          functions(name) = Sort.fun(argSorts, sort)
          List(Command.DeclareFun(name, argSorts.map(sortString), sortString(sort)))
    case s2.Command.DeclareDatatypes(datatypes) =>
      for datatype <- datatypes; constructor <- datatype.constructors do
        val d = Sort.Named(datatype.name)
        undoable(functions, constructor.name)
        functions(constructor.name) = Sort.fun(constructor.selectors.map(_._2), d)
        undoable(functions, constructor.tester)
        functions(constructor.tester) = Sort.fun(List(d), Sort.Bool)
        for (selector, s) <- constructor.selectors do
          undoable(functions, selector)
          functions(selector) = Sort.fun(List(d), s)
        declared ++= constructor.name :: constructor.tester :: constructor.selectors.map(_._1)
      List(Command.DeclareDatatypes(datatypes.map(d =>
        d.copy(constructors = d.constructors.map(c => c.copy(selectors = c.selectors.map((n, s) => (n, lowered(s))))))
      )))
    case s2.Command.DefineFun(name, params, sort, body) =>
      if minted(name) then fail(s"$name is a minted symbol")
      undoable(macros, name)
      macros(name) = (params, sort, body)
      Nil
    case s2.Command.Assert(f) => List(Command.Assert(formula(f, Map.empty)))
    case s2.Command.AssertNamed(name, f) => List(Command.AssertNamed(name, formula(f, Map.empty)))
    case s2.Command.CheckSat => List(Command.CheckSat)
    case s2.Command.CheckSatAssuming(assumptions) =>
      List(Command.CheckSatAssuming(assumptions.map(formula(_, Map.empty))))
    case s2.Command.Push(levels) =>
      for _ <- 1 to levels do scopes += ArrayBuffer.empty
      List(Command.Push(levels))
    case s2.Command.Pop(levels) =>
      for _ <- 1 to (levels min scopes.length) do scopes.remove(scopes.length - 1).reverseIterator.foreach(_())
      List(Command.Pop(levels))
    case s2.Command.ResetAssertions =>
      clearMemos()
      Command.ResetAssertions +: functions.toList.sortBy(_._1).collect { case (n, Sort.BitVec(w)) =>
        Command.Assert(range(Const(n), w))
      }
    case s2.Command.Reset =>
      scopes.clear()
      globalDeclarations = false
      functions.clear()
      macros.clear()
      rationalValues.clear()
      stores.clear()
      clearMemos()
      List(Command.Reset)
    case s2.Command.GetModel => List(Command.GetModel)
    case s2.Command.GetValue(items) =>
      items.map((_, item) =>
        item match
          case f: s2.Formula => formula(f, Map.empty)
          case t: s2.Term => term(t, Map.empty)
      )
    case s2.Command.GetUnsatCore => List(Command.GetUnsatCore)
    case s2.Command.GetAssertions => List(Command.GetAssertions)
    case s2.Command.Exit => List(Command.Exit)
    case s2.Command.SetOption(key, value) =>
      if key == ":global-declarations" then globalDeclarations = value == "true"
      List(Command.SetOption(key, value))
    case s2.Command.GetInfo(key) => List(Command.GetInfo(key))
    case s2.Command.Other(sexpr) => List(Command.Other(sexpr))

  // Sorts.

  private def lowered(s: Sort): Sort = s match
    case Sort.BitVec(_) => Sort.Int
    case Sort.Fun(args, result) => Sort.Fun(args.map(lowered), lowered(result))
    case Sort.Array(index, element) =>
      val i = index match
        case Sort.BitVec(_) => index
        case other => lowered(other)
      Sort.fun(List(i), lowered(element))
    case other => other

  private def sortString(s: Sort): String = s match
    case Sort.BitVec(_) => "Int"
    case Sort.Fun(args, result) => s2.Sorts.function(args.map(sortString), sortString(result))
    case Sort.Array(index, element) =>
      val i = index match
        case Sort.BitVec(_) => index.text
        case other => sortString(other)
      s2.Sorts.function(List(i), sortString(element))
    case other => other.text

  private def sortOf(t: s2.Term, env: Env): Sort = t match
    case s2.Term.Const(name) =>
      env.get(name) match
        case Some(Value.T(_, sort)) => sort
        case Some(Value.F(_)) => Sort.Bool
        case None => functions.get(name).orElse(macros.get(name).map(_._2)).getOrElse(fail(s"unknown symbol $name"))
    case s2.Term.Numeral(_) => Sort.Int
    case s2.Term.Decimal(_, _) => Sort.Real
    case s2.Term.Bits(_, width) => Sort.BitVec(width)
    case s2.Term.App(s2.Term.Const(name), _) if macros.contains(name) => macros(name)._2
    case s2.Term.App(fun, args) => sortOf(fun, env).applied(args.length).getOrElse(fail(s"$fun applied"))
    case s2.Term.ArithApp(op, args) =>
      op match
        case ArithOp.Div | ArithOp.ToReal => Sort.Real
        case ArithOp.IntDiv | ArithOp.Mod | ArithOp.Abs | ArithOp.ToInt => Sort.Int
        case _ => if args.exists(a => sortOf(a, env) == Sort.Real) then Sort.Real else Sort.Int
    case s2.Term.BvApp(op, args) =>
      op match
        case BvOp.UbvToInt | BvOp.SbvToInt => Sort.Int
        case BvOp.IntToBv(n) => Sort.BitVec(n)
        case BvOp.Extract(i, j) => Sort.BitVec(i - j + 1)
        case BvOp.ZeroExtend(k) => Sort.BitVec(width(args.head, env) + k)
        case BvOp.SignExtend(k) => Sort.BitVec(width(args.head, env) + k)
        case BvOp.Repeat(k) => Sort.BitVec(width(args.head, env) * k)
        case BvOp.Concat => Sort.BitVec(args.map(width(_, env)).sum)
        case BvOp.Comp => Sort.BitVec(1)
        case _ => Sort.BitVec(width(args.head, env))
    case s2.Term.Select(array, _) =>
      sortOf(array, env) match
        case Sort.Array(_, element) => element
        case s => s.applied(1).getOrElse(fail(s"select on $s"))
    case s2.Term.Store(array, _, _) => sortOf(array, env)
    case s2.Term.Ite(_, thn, els) =>
      val t = sortOf(thn, env)
      if t == Sort.Int && sortOf(els, env) == Sort.Real then Sort.Real else t
    case s2.Term.Let(bindings, body) =>
      sortOf(
        body,
        env ++ bindings.map((name, value) =>
          name ->
            (value match
              case t: s2.Term => Value.T(Num(0), sortOf(t, env))
              case _: s2.Formula => Value.F(True))
        )
      )
    case s2.Term.Bool(_) => Sort.Bool

  private def width(t: s2.Term, env: Env): Int = sortOf(t, env) match
    case Sort.BitVec(w) => w
    case s => fail(s"a bit-vector was expected, not $s")

  private def arithmetic(sort: Sort): Boolean = sort match
    case Sort.Int | Sort.Real | Sort.BitVec(_) => true
    case _ => false

  // Formulas and terms.

  private def equal(sort: Sort, lhs: Term, rhs: Term): Formula = sort match
    case Sort.BitVec(w) if bitwiseEquality(List(lhs, rhs), w) =>
      And(bits(lhs, w).zip(bits(rhs, w)).map(same).toList)
    case _ => if arithmetic(sort) then IntEq(lhs, rhs) else Eq(lhs, rhs)

  private def bitwiseEquality(ts: List[Term], width: Int): Boolean =
    ts.forall(t => knownBits(t, width).isDefined) && ts.exists(!_.isInstanceOf[Num])

  private def chain(conditions: List[Formula]): Formula = conditions match
    case single :: Nil => single
    case _ => And(conditions)

  private def formula(f: s2.Formula, env: Env): Formula = f match
    case s2.Formula.True => True
    case s2.Formula.False => False
    case s2.Formula.Not(arg) => Not(formula(arg, env))
    case s2.Formula.And(args) => And(args.map(formula(_, env)))
    case s2.Formula.Or(args) => Or(args.map(formula(_, env)))
    case s2.Formula.Implies(args) => args.map(formula(_, env)).reduceRight(Implies(_, _))
    case s2.Formula.Xor(args) => args.map(formula(_, env)).reduceLeft((a, b) => Not(Iff(a, b)))
    case s2.Formula.Iff(args) =>
      val fs = args.map(formula(_, env))
      chain(fs.zip(fs.tail).map(Iff(_, _)))
    case s2.Formula.Eq(args) =>
      val sort = sortOf(args.head, env)
      val ts = args.map(term(_, env))
      chain(ts.zip(ts.tail).map(equal(sort, _, _)))
    case s2.Formula.Distinct(args) =>
      val sort = sortOf(args.head, env)
      val ts = args.map(term(_, env))
      sort match
        case Sort.BitVec(w) if bitwiseEquality(ts, w) =>
          chain(for (a, i) <- ts.zipWithIndex; b <- ts.drop(i + 1) yield Not(equal(sort, a, b)))
        case _ =>
          if !arithmetic(sort) then Distinct(ts)
          else if args.exists(a => sortOf(a, env) == Sort.Real) then RealDistinct(ts)
          else IntDistinct(ts)
    case s2.Formula.Cmp(op, args) =>
      val real = args.exists(a => sortOf(a, env) == Sort.Real)
      val ts = args.map(term(_, env))
      val compare: (Term, Term) => Formula = op match
        case s2.CmpOp.Le => Le(_, _)
        case s2.CmpOp.Lt => if real then Less(_, _) else Lt(_, _)
        case s2.CmpOp.Ge => Ge(_, _)
        case s2.CmpOp.Gt => if real then (lhs, rhs) => Less(rhs, lhs) else Gt(_, _)
      chain(ts.zip(ts.tail).map(compare(_, _)))
    case s2.Formula.BvCmp(op, lhs, rhs) => compare(op, term(lhs, env), term(rhs, env), width(lhs, env))
    case s2.Formula.Ite(cond, thn, els) => Ite(formula(cond, env), formula(thn, env), formula(els, env))
    case s2.Formula.IsConstructor(constructor, arg) => Pred(App(Const(s"(_ is $constructor)"), List(term(arg, env))))
    case s2.Formula.IsInt(arg) =>
      val r = term(arg, env)
      literalValue(r) match
        case Some((p, q)) => if p.mod(q).signum == 0 then True else False
        case None => IntEq(r, use("floor", List(r)).head)
    case s2.Formula.Let(bindings, body) => formula(body, bind(bindings, env))
    case s2.Formula.Pred(t) => Formula.pred(term(t, env))

  private def bind(bindings: List[(String, s2.Term | s2.Formula)], env: Env): Env =
    env ++ bindings.map((name, value) =>
      name ->
        (value match
          case t: s2.Term =>
            val sort = sortOf(t, env)
            if sort == Sort.Bool then Value.F(Formula.pred(term(t, env))) else Value.T(term(t, env), sort)
          case f: s2.Formula => Value.F(formula(f, env)))
    )

  private def term(t: s2.Term, env: Env): Term = t match
    case s2.Term.Const(name) =>
      env.get(name) match
        case Some(Value.T(t, _)) => t
        case Some(Value.F(f)) => Term.boolean(f)
        case None =>
          if macros.contains(name) then expand(name, Nil, env)
          else if functions.contains(name) && !minted(name) then Const(name)
          else fail(s"unknown symbol $name")
    case s2.Term.Numeral(value) => Num(value)
    case s2.Term.Decimal(unscaled, scale) => rational(unscaled, BigInt(10).pow(scale))
    case s2.Term.Bits(value, _) => Num(value)
    case s2.Term.App(s2.Term.Const(name), args) if macros.contains(name) => expand(name, args, env)
    case s2.Term.App(fun, args) => ranged(App(term(fun, env), args.map(term(_, env))), sortOf(t, env))
    case s2.Term.ArithApp(op, args) => arith(op, args, env)
    case s2.Term.BvApp(op, args) => bv(op, args, env)
    case s2.Term.Select(array, index) => ranged(App(term(array, env), List(term(index, env))), sortOf(t, env))
    case s2.Term.Store(array, index, value) =>
      val key = sortString(sortOf(array, env))
      App(use("store", List(key)).head, List(term(array, env), term(index, env), term(value, env)))
    case s2.Term.Ite(cond, thn, els) =>
      val sort = sortOf(t, env)
      val c = formula(cond, env)
      use("ite", List(Term.boolean(c), term(thn, env), term(els, env)), List(sort)).head
    case s2.Term.Let(bindings, body) => term(body, bind(bindings, env))
    case s2.Term.Bool(f) => Term.boolean(formula(f, env))

  private def expand(name: String, args: List[s2.Term], env: Env): Term =
    val (params, _, body) = macros(name)
    if params.length != args.length then fail(s"$name expects ${params.length} arguments")
    val inner: Env = params.zip(args).map { case ((p, s), a) =>
      p -> (if s == Sort.Bool then Value.F(Formula.pred(term(a, env))) else Value.T(term(a, env), s))
    }.toMap
    body match
      case t: s2.Term => term(t, inner)
      case f: s2.Formula => Term.boolean(formula(f, inner))

  private def ranged(t: Term, sort: Sort): Term =
    sort match
      case Sort.BitVec(w) => use("range", List(t), List(w))
      case _ => ()
    t

  // Arithmetic.

  private def arith(op: ArithOp, args: List[s2.Term], env: Env): Term = op match
    case ArithOp.Add => Term.linear(args.map(a => (BigInt(1), term(a, env))))
    case ArithOp.Sub => Term.linear((BigInt(1), term(args.head, env)) :: args.tail.map(a => (BigInt(-1), term(a, env))))
    case ArithOp.Neg => Term.linear(List((BigInt(-1), term(args.head, env))))
    case ArithOp.Mul =>
      val (literals, others) = args.map(term(_, env)).partitionMap(t => literalValue(t).toLeft(t))
      val (p, q) = literals.foldLeft((BigInt(1), BigInt(1))) { case ((p, q), (a, b)) => (p * a, q * b) }
      others match
        case Nil => rational(p, q)
        case t :: Nil => rationalProduct(p, q, t)
        case _ => fail("non-linear multiplication")
    case ArithOp.Div =>
      args.map(term(_, env)).reduceLeft { (numerator, divisorTerm) =>
        val divisor = literalValue(divisorTerm).getOrElse(fail("nonlinear division"))
        literalValue(numerator) match
          case Some((p, q)) => rational(p * divisor._2, q * divisor._1)
          case None => rationalProduct(divisor._2, divisor._1, numerator)
      }
    case ArithOp.ToReal => term(args.head, env)
    case ArithOp.ToInt =>
      val r = term(args.head, env)
      literalValue(r) match
        case Some((p, q)) => Num(floorDiv(p, q))
        case None => use("floor", List(r)).head
    case _ => fail(s"${op.name} is not supported")

  private def floorDiv(p: BigInt, q: BigInt): BigInt =
    val (d, r) = p /% q
    if r.signum != 0 && r.signum != q.signum then d - 1 else d

  private def literalValue(t: Term): Option[(BigInt, BigInt)] = t match
    case Num(v) => Some((v, BigInt(1)))
    case _ if rationalValues.contains(t) => Some(rationalValues(t))
    case Linear(List((a, u)), c) if c.signum == 0 && rationalValues.contains(u) =>
      val (p, q) = rationalValues(u)
      Some((a * p, q))
    case _ => None

  private def rational(p0: BigInt, q0: BigInt): Term =
    if q0.signum == 0 then fail("division by zero")
    val g = p0.gcd(q0) * q0.signum
    val (p, q) = (p0 / g, q0 / g)
    if q == BigInt(1) then Num(p) else use("rat", List(p, q)).head

  private def rationalProduct(p0: BigInt, q0: BigInt, t: Term): Term =
    if q0.signum == 0 then fail("division by zero")
    val g = p0.gcd(q0) * q0.signum
    val (p, q) = (p0 / g, q0 / g)
    if q == BigInt(1) then Term.linear(List((p, t))) else use("ratmul", List(p, q, t)).head

  // Bit-vectors.

  private def range(t: Term, width: Int): Formula = And(List(Ge(t, Num(0)), Le(t, Num(modulus(width) - 1))))
  private def modulus(width: Int): BigInt = BigInt(2).pow(width)
  private val splitCases = 4

  private def bv(op: BvOp, args: List[s2.Term], env: Env): Term = op match
    case BvOp.UbvToInt => term(args.head, env)
    case BvOp.SbvToInt => signed(term(args.head, env), width(args.head, env))
    case BvOp.IntToBv(n) =>
      term(args.head, env) match
        case Num(v) => Num(v.mod(modulus(n)))
        case t => use("int2bv", List(t, n)).head
    case BvOp.Extract(i, j) => extract(i, j, term(args.head, env), width(args.head, env))
    case BvOp.ZeroExtend(_) => term(args.head, env)
    case BvOp.SignExtend(k) => signExtend(k, term(args.head, env), width(args.head, env))
    case BvOp.Repeat(k) =>
      val (t, w) = (term(args.head, env), width(args.head, env))
      Term.linear((0 until k).toList.map(j => (modulus(j * w), t)))
    case BvOp.RotateLeft(k) => rotate(k, term(args.head, env), width(args.head, env))
    case BvOp.RotateRight(k) =>
      val w = width(args.head, env)
      rotate(w - k % w, term(args.head, env), w)
    case BvOp.Concat =>
      args.map(a => (term(a, env), width(a, env))).reduceLeft { case ((hi, wh), (lo, wl)) =>
        (Term.linear(List((BigInt(2).pow(wl), hi), (BigInt(1), lo))), wh + wl)
      }._1
    case _ => arithmeticBv(op, args.map(term(_, env)), width(args.head, env))

  private def arithmeticBv(op: BvOp, args: List[Term], width: Int): Term =
    val m = modulus(width)
    (op, args) match
      case (BvOp.Add, _ :: _ :: _ :: _) => args.reduceLeft((a, b) => arithmeticBv(BvOp.Add, List(a, b), width))
      case (BvOp.Mul, _ :: _ :: _ :: _) => args.reduceLeft((a, b) => arithmeticBv(BvOp.Mul, List(a, b), width))
      case (BvOp.Add, a :: b :: Nil) => modulo(a + b, width, 0, 1)
      case (BvOp.Sub, a :: b :: Nil) => modulo(a - b, width, -1, 0)
      case (BvOp.Neg, a :: Nil) => modulo(Term.linear(List((BigInt(-1), a))), width, -1, 0)
      case (BvOp.Not, a :: Nil) => not(a, width)
      case (BvOp.Mul, a :: b :: Nil) =>
        (a, b) match
          case (Num(u), Num(v)) => Num((u * v).mod(m))
          case (t, Num(c)) => times(t, c.mod(m), width)
          case (Num(c), t) => times(t, c.mod(m), width)
          case _ => product(a, b, width)
      case (BvOp.Shl, a :: Num(c) :: Nil) => if c >= width then Num(0) else times(a, BigInt(2).pow(c.toInt), width)
      case (BvOp.Lshr, a :: Num(c) :: Nil) =>
        if c >= width then Num(0) else divide(a, BigInt(2).pow(c.toInt), m / BigInt(2).pow(c.toInt) - 1)._1
      case (BvOp.Udiv, a :: Num(c) :: Nil) => if c.signum == 0 then Num(m - 1) else divide(a, c, (m - 1) / c)._1
      case (BvOp.Urem, a :: Num(c) :: Nil) => if c.signum == 0 then a else divide(a, c, (m - 1) / c)._2
      case (BvOp.Sdiv | BvOp.Srem | BvOp.Smod, a :: Num(c) :: Nil) => signedDivision(op, a, c, width)
      case (BvOp.Ashr, Num(v) :: Num(c) :: Nil) => Num(((if v >= m / 2 then v - m else v) >> c.min(width).toInt).mod(m))
      case (BvOp.Ashr, a :: Num(c) :: Nil) =>
        val q =
          if c >= width then cases("sign", negative(a, width), Num(-1), Num(0), -1, 0)
          else divideSigned(a, width, BigInt(2).pow(c.toInt), floor = true)._1
        modulo(q, width, -1, 0)
      case (BvOp.Shl | BvOp.Lshr | BvOp.Ashr, a :: b :: Nil) => shifted(op, a, b, width)
      case (BvOp.Udiv, a :: b :: Nil) => divideUnknown(a, b, width)._1
      case (BvOp.Urem, a :: b :: Nil) => divideUnknown(a, b, width)._2
      case (BvOp.Sdiv | BvOp.Srem | BvOp.Smod, a :: b :: Nil) => signedDivisionUnknown(op, a, b, width)
      case (BvOp.And | BvOp.Or | BvOp.Xor, _) => args.reduceLeft((a, b) => bitwise(op, a, b, width))
      case (BvOp.Nand, a :: b :: Nil) => not(bitwise(BvOp.And, a, b, width), width)
      case (BvOp.Nor, a :: b :: Nil) => not(bitwise(BvOp.Or, a, b, width), width)
      case (BvOp.Xnor, a :: b :: Nil) => not(bitwise(BvOp.Xor, a, b, width), width)
      case (BvOp.Comp, a :: b :: Nil) =>
        (a, b) match
          case (Num(u), Num(v)) => Num(if u == v then 1 else 0)
          case _ => cases("comp", equal(Sort.BitVec(width), a, b), Num(1), Num(0), 0, 1)
      case _ => fail(s"${op.name} with these arguments is not supported")

  private def product(a: Term, b: Term, width: Int): Term =
    val (kept, blasted) = if knownBits(a, width).isDefined && knownBits(b, width).isEmpty then (b, a) else (a, b)
    val m = modulus(width)
    val partials = bits(blasted, width).zipWithIndex.map((bit, i) =>
      bit match
        case True => kept * modulus(i)
        case False => Num(0)
        case _ => cases("part", bit, kept * modulus(i), Num(0), 0, modulus(i) * (m - 1))
    )
    modulo(Term.linear(partials.toList.map(p => (BigInt(1), p))), width, 0, m - 2)

  private def divideUnknown(a: Term, b: Term, width: Int): (Term, Term) =
    val m = modulus(width)
    val List(q, r) = use("udiv", List(Term.linear(List((BigInt(1), a), (m, b))), m, m), List(a, b, width)): @unchecked
    (q, r)

  private def exactProduct(a: Term, b: Term, width: Int): Term =
    val m = modulus(width)
    val partials = bits(b, width).zipWithIndex.map((bit, i) =>
      bit match
        case True => a * modulus(i)
        case False => Num(0)
        case _ => cases("part", bit, a * modulus(i), Num(0), 0, modulus(i) * (m - 1))
    )
    Term.linear(partials.toList.map(p => (BigInt(1), p)))

  private def signedDivisionUnknown(op: BvOp, a: Term, b: Term, width: Int): Term =
    val m = modulus(width)
    val (na, nb) = (negative(a, width), negative(b, width))
    def magnitude(t: Term, n: Formula): Term = cases("abs", n, Term.linear(List((BigInt(-1), t)), m), t, 0, m / 2)
    val (q, r) = divideUnknown(magnitude(a, na), magnitude(b, nb), width)
    val zero = IntEq(b, Num(0))
    op match
      case BvOp.Sdiv =>
        val negate = And(List(Not(Iff(na, nb)), Ge(q, Num(1))))
        val signed = cases("sdivq", negate, Term.linear(List((BigInt(-1), q)), m), q, 0, m - 1)
        cases("sdiv0", zero, cases("sdiv0!", na, Num(1), Num(m - 1), 1, m - 1), signed, 0, m - 1)
      case _ =>
        val negate = And(List(na, Ge(r, Num(1))))
        val rem = cases("sremr", negate, Term.linear(List((BigInt(-1), r)), m), r, 0, m - 1)
        val result =
          if op == BvOp.Srem then rem
          else
            val differ = And(List(Not(Iff(na, nb)), Not(IntEq(r, Num(0)))))
            cases("smodr", differ, modulo(rem + b, width, 0, 1), rem, 0, m - 1)
        cases("srem0", zero, a, result, 0, m - 1)

  private def shifted(op: BvOp, a: Term, amount: Term, width: Int): Term =
    val stages = Iterator.iterate(1)(_ * 2).takeWhile(_ < width).length
    val bs = bits(amount, width)
    val shifted = (0 until stages).foldLeft(a) { (t, j) =>
      val by = modulus(1 << j)
      val step = op match
        case BvOp.Shl => times(t, by, width)
        case BvOp.Lshr => divide(t, by, modulus(width) / by - 1)._1
        case _ => modulo(divideSigned(t, width, by, floor = true)._1, width, -1, 0)
      bs(j) match
        case True => step
        case False => t
        case bit => cases(op.name, bit, step, t, 0, modulus(width) - 1)
    }
    val far = Or(
      bs.drop(stages).toList.filter(_ != False) ++
        (if (1 << stages) == width then Nil else List(Ge(amount, Num(width))))
    )
    val past = op match
      case BvOp.Ashr => cases("sign", negative(a, width), Num(modulus(width) - 1), Num(0), 0, modulus(width) - 1)
      case _ => Num(0)
    far match
      case False => shifted
      case True => past
      case _ => cases(op.name + "!far", far, past, shifted, 0, modulus(width) - 1)

  private def not(t: Term, width: Int): Term =
    val r = Term.linear(List((BigInt(-1), t)), modulus(width) - 1)
    for bs <- knownBitsOf.get((t, width)) do knownBitsOf((r, width)) = bs.map(Not(_))
    r

  private def rotate(k: Int, t: Term, width: Int): Term =
    val shift = k % width
    if shift == 0 then t
    else
      val (q, r) = divide(t, modulus(width - shift), modulus(shift) - 1)
      Term.linear(List((modulus(shift), r), (BigInt(1), q)))

  private def bits(t: Term, width: Int): IndexedSeq[Formula] =
    knownBits(t, width).getOrElse {
      use("bits", List(t, width))
      knownBitsOf((t, width))
    }

  private def knownBits(t: Term, width: Int): Option[IndexedSeq[Formula]] = t match
    case Num(v) => Some((0 until width).map(i => if v.testBit(i) then True else False))
    case Linear(List((a, u)), c) if a == -1 && c == modulus(width) - 1 => knownBits(u, width).map(_.map(Not(_)))
    case _ => knownBitsOf.get((t, width))

  private def bitConstant(f: Formula): Term = f match
    case True => Num(1)
    case False => Num(0)
    case Not(g) if bitConstants.contains(g) => Num(1) - bitConstants(g)
    case _ => bitConstants.getOrElseUpdate(f, cases("bit", f, Num(1), Num(0), 0, 1))

  private def fromBits(fs: IndexedSeq[Formula]): Term =
    Term.linear(fs.toList.zipWithIndex.collect { case (f, i) if f != False => (modulus(i), bitConstant(f)) })

  private def same(f: Formula, g: Formula): Formula = (f, g) match
    case (True, _) => g
    case (_, True) => f
    case (False, _) => Not(g)
    case (_, False) => Not(f)
    case _ => if f == g then True else Iff(f, g)

  private def bitwise(op: BvOp, a: Term, b: Term, width: Int): Term =
    (a, b) match
      case (Num(u), Num(v)) =>
        Num(op match
          case BvOp.And => u & v
          case BvOp.Or => u | v
          case _ => u ^ v)
      case _ =>
        val (l, r) = if a.hashCode <= b.hashCode then (a, b) else (b, a)
        val run = (l, r) match
          case (t, Num(mask)) => masked(op, t, mask, width)
          case (Num(mask), t) => masked(op, t, mask, width)
          case _ => None
        run.getOrElse {
          val (ls, rs) = (bits(l, width), bits(r, width))
          val fs = (0 until width).map(i => gate(op, ls(i), rs(i)))
          val result = fromBits(fs)
          knownBitsOf((result, width)) = fs
          result
        }

  private def gate(op: BvOp, x: Formula, y: Formula): Formula = (op, x, y) match
    case (BvOp.And, True, f) => f
    case (BvOp.And, f, True) => f
    case (BvOp.And, False, _) => False
    case (BvOp.And, _, False) => False
    case (BvOp.Or, True, _) => True
    case (BvOp.Or, _, True) => True
    case (BvOp.Or, False, f) => f
    case (BvOp.Or, f, False) => f
    case (BvOp.Xor, True, f) => Not(f)
    case (BvOp.Xor, f, True) => Not(f)
    case (BvOp.Xor, False, f) => f
    case (BvOp.Xor, f, False) => f
    case (_, f, g) if f == g => if op == BvOp.Xor then False else f
    case (BvOp.And, _, _) => And(List(x, y))
    case (BvOp.Or, _, _) => Or(List(x, y))
    case _ => Not(Iff(x, y))

  private def modulo(value: Term, width: Int, lo: BigInt, hi: BigInt): Term = value match
    case Num(v) => Num(v.mod(modulus(width)))
    case _ => use("modulo", List(value, width, lo, hi)).head

  private def negative(t: Term, width: Int): Formula = t match
    case Num(v) => if v >= modulus(width - 1) then True else False
    case _ => Ge(t, Num(modulus(width - 1)))

  private def cases(tag: String, cond: Formula, thn: Term, els: Term, lo: BigInt, hi: BigInt): Term =
    use("cases", List(tag, Term.boolean(cond), thn, els, lo, hi)).head

  private def divide(value: Term, divisor: BigInt, quotientMax: BigInt): (Term, Term) = value match
    case Num(v) => (Num(v / divisor), Num(v.mod(divisor)))
    case _ =>
      val List(q, r) = use("divide", List(value, divisor, quotientMax)): @unchecked
      (q, r)

  private def signedDivision(op: BvOp, a: Term, c: BigInt, width: Int): Term =
    val m = modulus(width)
    val t = if c >= m / 2 then c - m else c
    (a, op) match
      case (Num(v), _) =>
        val sv = if v >= m / 2 then v - m else v
        val result = op match
          case BvOp.Sdiv => if t.signum == 0 then (if sv.signum >= 0 then BigInt(-1) else BigInt(1)) else sv / t
          case BvOp.Srem => if t.signum == 0 then sv else sv % t
          case _ =>
            val r = if t.signum == 0 then sv else sv % t
            if r.signum == 0 || t.signum == 0 || r.signum == t.signum then r else r + t
        Num(result.mod(m))
      case (_, BvOp.Sdiv) if t.signum == 0 => cases("sdiv0", negative(a, width), Num(1), Num(m - 1), 1, m - 1)
      case (_, _) if t.signum == 0 => a
      case _ =>
        val (q, r) = divideSigned(a, width, t.abs)
        op match
          case BvOp.Sdiv => modulo(if t.signum > 0 then q else q * -1, width, -1, 0)
          case BvOp.Srem => modulo(r, width, -1, 0)
          case _ =>
            val z = use("smodz", List(a, c, width)).head
            modulo(r + z * t, width, -1, 0)

  private def divideSigned(t: Term, width: Int, d: BigInt, floor: Boolean = false): (Term, Term) =
    val List(q, r) = use("sdivide", List(t, width, d, floor)): @unchecked
    (q, r)

  private def signed(t: Term, width: Int): Term = t match
    case Num(v) => Num(if v >= modulus(width - 1) then v - modulus(width) else v)
    case _ =>
      cases("signed", negative(t, width), t - Num(modulus(width)), t, -modulus(width - 1), modulus(width - 1) - 1)

  private def times(t: Term, c: BigInt, width: Int): Term =
    if c.signum == 0 then Num(0) else modulo(t * c, width, 0, c - 1)

  private def masked(op: BvOp, t: Term, mask: BigInt, width: Int): Option[Term] =
    val m = modulus(width)
    val k = (0 to width).find(k => mask == modulus(k) - 1 || mask == m - modulus(k))
    k.map { k =>
      val low = if k == width then t else if k == 0 then Num(0) else divide(t, modulus(k), m / modulus(k) - 1)._2
      val high = t - low
      val (kept, masked) = if mask == modulus(k) - 1 then (high, low) else (low, high)
      op match
        case BvOp.And => masked
        case BvOp.Or => kept + Num(mask)
        case _ => kept + (Num(mask) - masked)
    }

  private def extract(i: Int, j: Int, t: Term, width: Int): Term =
    val q = if j == 0 then t else divide(t, BigInt(2).pow(j), modulus(width - j) - 1)._1
    if i == width - 1 then q else divide(q, modulus(i - j + 1), modulus(width - i - 1) - 1)._2

  private def signExtend(k: Int, t: Term, width: Int): Term =
    if k == 0 then t
    else
      t match
        case Num(v) => Num(if v >= modulus(width - 1) then v + modulus(width + k) - modulus(width) else v)
        case _ =>
          val extended = modulus(width + k) - modulus(width)
          cases("ext", negative(t, width), t + Num(extended), t, 0, modulus(width + k) - 1)

  private def compare(op: BvCmpOp, a: Term, b: Term, width: Int): Formula =
    val (strict, flipped) = op match
      case BvCmpOp.Ult | BvCmpOp.Slt => (true, false)
      case BvCmpOp.Ule | BvCmpOp.Sle => (false, false)
      case BvCmpOp.Ugt | BvCmpOp.Sgt => (true, true)
      case BvCmpOp.Uge | BvCmpOp.Sge => (false, true)
    val (l, r) = if flipped then (b, a) else (a, b)
    val unsigned = if strict then Lt(l, r) else Le(l, r)
    if !op.signed then unsigned
    else
      val (nl, nr) = (negative(l, width), negative(r, width))
      Or(List(And(List(nl, Not(nr))), And(List(Iff(nl, nr), unsigned))))

object LoweringCheck:
  private final case class Failed(msg: String) extends Exception(msg, null, false, false)
