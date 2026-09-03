package net.bovel.vik

import scala.collection.mutable.{ArrayBuffer, HashMap, HashSet}

import net.bovel.smtlib.{ArithOp, BvCmpOp, BvOp, Sort}
import net.bovel.smtlib as s2
import Formula.*
import Term.*

/** Lowers a surface script (`net.bovel.smtlib`) to the solver's language: the
 *  one function from the language the parser reads to the trees the solver
 *  plays — a [[Session]] applies it, and `smt2lowered` prints its result. This
 *  is where the surface language's conveniences are compiled away and where the
 *  fragment line is drawn, with a message:
 *    - `let` names become substitutions; `define-fun` macros are expanded at
 *      their uses;
 *    - `+`, `-` and `*` by a literal become the linear normal form; rational
 *      literals and divisions become defined `Real` constants; `div`, `mod`,
 *      `abs` and nonlinear multiplication are rejected;
 *    - chained `=`, `distinct` and comparisons become pairwise bounds on
 *      numbers and bit-vectors, equalities and disequalities elsewhere;
 *    - a term-level `ite` becomes a fresh constant with two defining assertions
 *      placed before the assertion using it;
 *    - `select` is application, `store` an application of the array sort's
 *      `store!N` symbol, which the solver's array lemmas recognize;
 *    - bit-vectors go to linear integer arithmetic: an `n`-bit term is an `Int`
 *      in `[0, 2^n)`, a declaration an `Int` declaration with its range
 *      asserted, and each operation a linear relation between its arguments,
 *      its result and fresh bounded integers — `bvadd` and friends through an
 *      overflow count `k` (`value = r + 2^n × k`), divisions, shifts and
 *      extractions through `x = d × q + r`, the signed comparisons and
 *      `sign_extend` through the sign bit (`x = u + 2^(n−1) × s`).
 *      Multiplication, division and shifts by a variable, and the other bitwise
 *      operations would need bit-blasting: rejected
 *      ([[net.bovel.smtlib.Unsupported]]).
 */
object Lowering:
  def apply(script: s2.Script): Script = Script(Lowerer().commands(script.commands))

/** One lowering: the declared sorts, the macros, the fresh constants of the
 *  assertion being lowered and their defining assertions.
 */
private final class Lowerer:
  private enum Value: // a `let`-bound value
    case T(term: Term, sort: Sort)
    case F(formula: Formula)

  private type Env = Map[String, Value]

  private val functions = HashMap.empty[String, Sort] // declared symbols, with their whole function sort
  private val macros = HashMap.empty[String, (List[(String, Sort)], Sort, s2.Term | s2.Formula)]

  private var fresh = 0
  private val introduced = ArrayBuffer.empty[(String, String)] // fresh constants, declared before the assertion
  private val definitions = ArrayBuffer.empty[Formula] // the assertions defining them
  private val rangedApplications = HashSet.empty[Term] // bit-vector applications whose range is asserted

  def commands(cs: List[s2.Command]): List[Command] = cs.flatMap(command)

  /** A sort as the solver writes it: a string, arrays as function sorts. */
  /** A sort as the lowered script declares it: a bit-vector is an `Int`, in a
   *  field, an argument, a result or an array as much as in a constant — the
   *  solver ties a selector to its constructor's argument by the field's sort,
   *  and every bit-vector term is an integer to it.
   */
  private def lowered(s: Sort): Sort = s match
    case Sort.BitVec(_) => Sort.Int
    case Sort.Fun(args, result) => Sort.Fun(args.map(lowered), lowered(result))
    case Sort.Array(index, element) => Sort.Array(lowered(index), lowered(element))
    case other => other

  private def sortString(s: Sort): String = lowered(s) match
    case Sort.Fun(args, result) => s2.Sorts.function(args.map(sortString), sortString(result))
    case Sort.Array(index, element) => s2.Sorts.function(List(sortString(index)), sortString(element))
    case other => other.text

  private def command(c: s2.Command): List[Command] = c match
    // The lowered script is in the corresponding integer logic.
    case s2.Command.SetLogic(logic) => List(Command.SetLogic(logic.replace("BV", "LIA")))
    case s2.Command.SetInfo(key, value) => List(Command.SetInfo(key, value))
    case s2.Command.DeclareSort(name, arity) => List(Command.DeclareSort(name, arity))
    case s2.Command.DeclareFun(name, Nil, Sort.BitVec(w)) =>
      functions(name) = Sort.BitVec(w)
      List(Command.DeclareFun(name, Nil, "Int"), Command.Assert(range(Const(name), w)))
    case s2.Command.DeclareFun(name, argSorts, sort) =>
      functions(name) = Sort.fun(argSorts, sort)
      // An array constant is declared as a constant of its function sort — `(declare-fun a () (-> I E))` —
      // not flattened into a function symbol: the solver applies it either way, and the model knows an
      // array from a function by that.
      List(Command.DeclareFun(name, argSorts.map(sortString), sortString(sort)))
    case s2.Command.DeclareDatatypes(datatypes) =>
      for datatype <- datatypes; constructor <- datatype.constructors do
        val d = Sort.Named(datatype.name)
        functions(constructor.name) = Sort.fun(constructor.selectors.map(_._2), d)
        functions(constructor.tester) = Sort.fun(List(d), Sort.Bool)
        for (selector, s) <- constructor.selectors do functions(selector) = Sort.fun(List(d), s)
      List(Command.DeclareDatatypes(datatypes.map(d =>
        d.copy(constructors = d.constructors.map(c => c.copy(selectors = c.selectors.map((n, s) => (n, lowered(s))))))
      )))
    case s2.Command.DefineFun(name, params, sort, body) =>
      macros(name) = (params, sort, body) // expanded at each use; the definition itself vanishes
      Nil
    case s2.Command.Assert(f) => flush(Command.Assert(formula(f, Map.empty)))
    case s2.Command.AssertNamed(name, f) => flush(Command.AssertNamed(name, formula(f, Map.empty)))
    case s2.Command.CheckSat => List(Command.CheckSat)
    case s2.Command.CheckSatAssuming(assumptions) =>
      flush(Command.CheckSatAssuming(assumptions.map(formula(_, Map.empty))))
    case s2.Command.Push(levels) => List(Command.Push(levels))
    case s2.Command.Pop(levels) =>
      // A defining assertion lives and dies with its level: a rational constant
      // cached across the pop would come back without its definition, so later
      // uses mint a fresh one, defined again.
      rationals.clear()
      rationalProducts.clear()
      rangedApplications.clear()
      List(Command.Pop(levels))
    case s2.Command.ResetAssertions =>
      rationals.clear()
      rationalProducts.clear()
      rangedApplications.clear()
      List(Command.ResetAssertions)
    case s2.Command.Reset =>
      functions.clear()
      macros.clear()
      rationals.clear()
      rationalProducts.clear()
      rationalValues.clear()
      storeSymbols.clear()
      rangedApplications.clear()
      List(Command.Reset)
    case s2.Command.GetModel => List(Command.GetModel)
    case s2.Command.GetValue(items) =>
      // A compound operation's defining assertions (a term-level `ite`'s) cannot be asserted after a
      // check: they go with the command, for the session to evaluate under the model.
      val lowered = items.map((source, item) =>
        (
          source,
          (item match
            case f: s2.Formula => formula(f, Map.empty)
            case t: s2.Term => term(t, Map.empty)
          ): Formula | Term
        )
      )
      val defined = definitions.toList
      introduced.clear()
      definitions.clear()
      rationals.clear() // a constant minted here has no definition: later uses must not find it
      rationalProducts.clear()
      rangedApplications.clear()
      List(Command.GetValue(lowered, defined))
    case s2.Command.GetUnsatCore => List(Command.GetUnsatCore)
    case s2.Command.GetAssertions => List(Command.GetAssertions)
    case s2.Command.Exit => List(Command.Exit)
    case s2.Command.SetOption(key, value) => List(Command.SetOption(key, value))
    case s2.Command.GetInfo(key) => List(Command.GetInfo(key))
    case s2.Command.Other(sexpr) => List(Command.Other(sexpr))

  /** The introduced declarations and defining assertions, then the command. */
  private def flush(command: Command): List[Command] =
    val out = introduced.toList.map((name, sort) => Command.DeclareFun(name, Nil, sort)) ++
      definitions.toList.map(Command.Assert(_)) :+ command
    introduced.clear()
    definitions.clear()
    out

  /** The sort of a surface term, for the sort-directed encodings: which `=` a
   *  chain means, the widths of bit-vectors, what a `select` returns.
   */
  private def sortOf(t: s2.Term, env: Env): Sort = t match
    case s2.Term.Const(name) =>
      env.get(name) match
        case Some(Value.T(_, sort)) => sort
        case Some(Value.F(_)) => Sort.Bool
        case None =>
          functions.get(name).orElse(macros.get(name).map(_._2)).getOrElse(
            throw s2.Smt2Error(s"unknown symbol $name")
          )
    case s2.Term.Numeral(_) => Sort.Int
    case s2.Term.Decimal(_, _) => Sort.Real
    case s2.Term.Bits(_, width) => Sort.BitVec(width)
    case s2.Term.App(s2.Term.Const(name), args) if macros.contains(name) => macros(name)._2
    case s2.Term.App(fun, args) =>
      sortOf(fun, env).applied(args.length).getOrElse(
        throw s2.Smt2Error(s"${sortOf(fun, env)} applied to ${args.length} arguments")
      )
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
        case s => s.applied(1).getOrElse(throw s2.Smt2Error(s"select on $s"))
    case s2.Term.Store(array, _, _) => sortOf(array, env)
    case s2.Term.Ite(_, thn, _) => sortOf(thn, env)
    case s2.Term.Let(bindings, body) =>
      sortOf(
        body,
        env ++ bindings.map((name, value) =>
          name ->
            (value match
              case t: s2.Term => Value.T(Num(0), sortOf(t, env)) // only the sort matters here
              case _: s2.Formula => Value.F(True))
        )
      )
    case s2.Term.Bool(_) => Sort.Bool

  private def width(t: s2.Term, env: Env): Int = sortOf(t, env) match
    case Sort.BitVec(w) => w
    case s => throw s2.Smt2Error(s"a bit-vector was expected, not $s")

  /** Whether terms of `sort` are numbers after lowering: where `=` and
   *  `distinct` mean bounds.
   */
  private def arithmetic(sort: Sort): Boolean = sort match
    case Sort.Int | Sort.Real | Sort.BitVec(_) => true
    case _ => false

  /** Equality of two lowered terms of `sort`. */
  private def equal(sort: Sort, lhs: Term, rhs: Term): Formula =
    if arithmetic(sort) then IntEq(lhs, rhs) else Eq(lhs, rhs)

  /** The conjunction of `conditions`, itself when there is only one. */
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
      val ts = args.map(term(_, env))
      if arithmetic(sortOf(args.head, env)) then IntDistinct(ts) else Distinct(ts)
    case s2.Formula.Cmp(op, args) =>
      val real = args.exists(a => sortOf(a, env) == Sort.Real) // any side: numerals alone say Int
      val ts = args.map(term(_, env))
      val compare: (Term, Term) => Formula = op match
        case s2.CmpOp.Le => Le(_, _)
        case s2.CmpOp.Lt => if real then Less(_, _) else Lt(_, _)
        case s2.CmpOp.Ge => Ge(_, _)
        case s2.CmpOp.Gt => if real then (lhs, rhs) => Less(rhs, lhs) else Gt(_, _)
      chain(ts.zip(ts.tail).map(compare(_, _)))
    case s2.Formula.BvCmp(op, lhs, rhs) =>
      val w = width(lhs, env)
      compare(op, term(lhs, env), term(rhs, env), w)
    case s2.Formula.Ite(cond, thn, els) => Ite(formula(cond, env), formula(thn, env), formula(els, env))
    case s2.Formula.IsConstructor(constructor, arg) =>
      Pred(App(Const(s"(_ is $constructor)"), List(term(arg, env))))
    case s2.Formula.IsInt(arg) => isInt(term(arg, env))
    case s2.Formula.Let(bindings, body) => formula(body, bind(bindings, env))
    case s2.Formula.Pred(t) => Formula.pred(term(t, env))

  /** The bindings of a `let`, evaluated in the outer environment (`let` binds
   *  in parallel).
   */
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
          else if functions.contains(name) then Const(name)
          else throw s2.Smt2Error(s"unknown symbol $name")
    case s2.Term.Numeral(value) => Num(value)
    case s2.Term.Decimal(unscaled, scale) => rationalConst(unscaled, BigInt(10).pow(scale))
    case s2.Term.Bits(value, _) => Num(value)
    case s2.Term.App(s2.Term.Const(name), args) if macros.contains(name) => expand(name, args, env)
    case s2.Term.App(fun, args) => ranged(App(term(fun, env), args.map(term(_, env))), sortOf(t, env))
    case s2.Term.ArithApp(op, args) => arith(op, args, env)
    case s2.Term.BvApp(op, args) => bv(op, args, env)
    case s2.Term.Select(array, index) =>
      val a = term(array, env)
      ranged(App(a, List(term(index, env))), sortOf(t, env))
    case s2.Term.Store(array, index, value) =>
      App(storeSymbol(sortOf(array, env)), List(term(array, env), term(index, env), term(value, env)))
    case s2.Term.Ite(cond, thn, els) =>
      // A fresh constant `k` with `cond => k = thn` and `not cond => k = els`, asserted before the
      // current assertion.
      val sort = sortOf(thn, env)
      fresh += 1
      val name = s"ite!$fresh"
      val k = Const(name)
      sort match
        case Sort.BitVec(w) => // introduced as the lowering of its declaration would
          introduced += ((name, "Int"))
          definitions += range(k, w)
        case _ => introduced += ((name, sortString(sort)))
      functions(name) = sort
      val c = formula(cond, env)
      definitions += Implies(c, equal(sort, k, term(thn, env)))
      definitions += Implies(Not(c), equal(sort, k, term(els, env)))
      k
    case s2.Term.Let(bindings, body) => term(body, bind(bindings, env))
    case s2.Term.Bool(f) => Term.boolean(formula(f, env))

  /** Expands the macro `name` applied to `args` (evaluated in `env`). */
  private def expand(name: String, args: List[s2.Term], env: Env): Term =
    val (params, sort, body) = macros(name)
    if params.length != args.length then throw s2.Smt2Error(s"$name expects ${params.length} arguments")
    val inner = params.zip(args).foldLeft(env) { case (e, ((p, s), a)) =>
      e +
        (p -> (
          if s == Sort.Bool then Value.F(Formula.pred(term(a, env)))
          else Value.T(term(a, env), s)
        ))
    }
    body match
      case t: s2.Term => term(t, inner)
      case f: s2.Formula => Term.boolean(formula(f, inner))

  // Arithmetic: `+`, `-` and `*` by a literal fold into the linear normal form; rational literals
  // and divisions become defined `Real` constants (so every consumer sees only integers); the
  // integer division operators are outside the fragment.
  private def arith(op: ArithOp, args: List[s2.Term], env: Env): Term = op match
    case ArithOp.Add => Term.linear(args.map(a => (BigInt(1), term(a, env))))
    case ArithOp.Sub =>
      Term.linear((BigInt(1), term(args.head, env)) :: args.tail.map(a => (BigInt(-1), term(a, env))))
    case ArithOp.Neg => Term.linear(List((BigInt(-1), term(args.head, env))))
    case ArithOp.Mul =>
      // Linear: all factors but at most one are literals, rationals included.
      val (literals, others) = args.map(term(_, env)).partitionMap(t => literalValue(t).toLeft(t))
      val (p, q) = literals.foldLeft((BigInt(1), BigInt(1))) { case ((p, q), (a, b)) => (p * a, q * b) }
      others match
        case Nil => rationalConst(p, q)
        case t :: Nil => rationalProduct(p, q, t)
        case _ => throw s2.Smt2Error(s"non-linear multiplication")
    case ArithOp.Div =>
      // Real division by a rational literal, `(/ 1 3)` and `(/ x 2)` alike; the rest is nonlinear.
      args.map(term(_, env)).reduceLeft { (numerator, divisorTerm) =>
        val divisor = literalValue(divisorTerm).getOrElse(throw s2.Smt2Error(s"nonlinear division"))
        literalValue(numerator) match
          case Some((p, q)) => rationalConst(p * divisor._2, q * divisor._1)
          case None => rationalProduct(divisor._2, divisor._1, numerator)
      }
    case ArithOp.ToReal => term(args.head, env) // the same value: sorts decide integrality, not the tree
    case ArithOp.ToInt =>
      val r = term(args.head, env)
      literalValue(r) match
        case Some((p, q)) => Num(floorDiv(p, q))
        case None => floor(r)
    case ArithOp.IntDiv | ArithOp.Mod | ArithOp.Abs =>
      throw s2.Unsupported(s"${op.name} is not supported")

  /** `is_int r`: `r` equals its own floor — sound under negation too, the floor
   *  being pinned uniquely by its definition. A literal folds.
   */
  private def isInt(r: Term): Formula = literalValue(r) match
    case Some((p, q)) => if p.mod(q).signum == 0 then True else False
    case None => IntEq(r, floor(r))

  /** The floor of `r` (`to_int`): a fresh `Int` constant `k` with
   *  `k ≤ r < k + 1`, declared and defined before the current assertion —
   *  determined uniquely by `r`, so the definition is sound under both
   *  polarities.
   */
  private def floor(r: Term): Term =
    fresh += 1
    val name = s"floor!$fresh"
    introduced += ((name, "Int"))
    functions(name) = Sort.Int
    val k = Const(name)
    definitions += Le(k, r)
    definitions += Less(r, Term.linear(List((BigInt(1), k)), 1))
    k

  private def floorDiv(p: BigInt, q: BigInt): BigInt =
    val (d, r) = p /% q
    if r.signum != 0 && r.signum != q.signum then d - 1 else d

  private val rationals = HashMap.empty[(BigInt, BigInt), Term]

  /** The value `p / q` of every term made by [[rationalConst]], for the literal
   *  folding of products and divisions.
   */
  private val rationalValues = HashMap.empty[Term, (BigInt, BigInt)]

  /** The rational value of a term when it is a literal: a numeral, a rational
   *  constant, or an integer multiple of one — `(- 0.1)` lowers to the latter.
   */
  private def literalValue(t: Term): Option[(BigInt, BigInt)] = t match
    case Num(v) => Some((v, BigInt(1)))
    case _ if rationalValues.contains(t) => Some(rationalValues(t))
    case Linear(List((a, u)), c) if c.signum == 0 && rationalValues.contains(u) =>
      val (p, q) = rationalValues(u)
      Some((a * p, q))
    case _ => None

  /** The term standing for the rational `p / q`: a numeral when integral, else
   *  a fresh `Real` constant `k` defined by `q k = p` before the current
   *  assertion — the mechanism of the bit-vector lowering, so every consumer
   *  (the solver, the models, the z3 replay) sees only integers.
   */
  private def rationalConst(p0: BigInt, q0: BigInt): Term =
    require(q0.signum != 0, "division by zero")
    val g = p0.gcd(q0) * q0.signum
    val (p, q) = (p0 / g, q0 / g)
    if q == BigInt(1) then Num(p)
    else
      rationals.getOrElseUpdate(
        (p, q), {
          fresh += 1
          val name = s"rat!$fresh"
          introduced += ((name, "Real"))
          functions(name) = Sort.Real
          val k = Const(name)
          definitions += IntEq(Term.linear(List((q, k))), Num(p))
          rationalValues(k) = (p, q)
          k
        }
      )

  /** The term standing for `(p / q) × t`: `t` scaled by an integer when `q`
   *  divides `p`, else a fresh `Real` constant `k` defined by `q k = p t`.
   */
  private def rationalProduct(p0: BigInt, q0: BigInt, t: Term): Term =
    require(q0.signum != 0, "division by zero")
    val g = p0.gcd(q0) * q0.signum
    val (p, q) = (p0 / g, q0 / g)
    if q == BigInt(1) then Term.linear(List((p, t)))
    else
      rationalProducts.getOrElseUpdate(
        (p, q, t), {
          fresh += 1
          val name = s"rat!$fresh"
          introduced += ((name, "Real"))
          functions(name) = Sort.Real
          val k = Const(name)
          definitions += IntEq(Term.linear(List((q, k))), Term.linear(List((p, t))))
          k
        }
      )

  private val rationalProducts = HashMap.empty[(BigInt, BigInt, Term), Term]

  /** The `store!N` function symbol for arrays of `sort`, declared on first use:
   *  `store` is not a function of the tree, each array sort gets its own
   *  uninterpreted symbol, and the solver's array lemmas recognize the name.
   */
  private val storeSymbols = HashMap.empty[String, String]

  private def storeSymbol(arraySort: Sort): Term =
    val key = sortString(arraySort)
    val name = storeSymbols.getOrElseUpdate(
      key, {
        // The array's function sort, flattened when its elements are arrays: the index is the first
        // parameter, the value sort what the array applied to one index has.
        val (params, _) = s2.Sorts.arrow(key).getOrElse(throw s2.Smt2Error(s"store on $arraySort"))
        val n = s"store!${storeSymbols.size + 1}"
        val argSorts = key :: params.head :: List(s2.Sorts.applied(key, 1)) // the array, the index, the value
        introduced += ((n, s2.Sorts.function(argSorts, key)))
        functions(n) = Sort.Named(n) // the solver reads the introduced declaration; the name is enough here
        n
      }
    )
    Const(name)

  // Bit-vectors, to linear integer arithmetic.

  /** A fresh `Int` constant in `[lo, hi]`, declared and bounded before the
   *  current assertion.
   */
  private def freshInt(prefix: String, lo: BigInt, hi: BigInt): Term =
    fresh += 1
    val name = s"$prefix!$fresh"
    introduced += ((name, "Int"))
    val k = Const(name)
    definitions += Ge(k, Num(lo))
    definitions += Le(k, Num(hi))
    k

  /** `0 ≤ t < 2^width`. */
  private def range(t: Term, width: Int): Formula = And(List(Ge(t, Num(0)), Le(t, Num(modulus(width) - 1))))

  /** A bit-vector that comes out of an application — of a function, a selector,
   *  an array — is an integer the solver knows nothing about: its range is
   *  asserted at its first use, as a declared constant's is at the declaration.
   *  Once per term since the last pop: the assertion lives at the level of the
   *  use ([[rangedApplications]] is cleared where the rational caches are).
   */
  private def ranged(t: Term, sort: Sort): Term =
    sort match
      case Sort.BitVec(w) if rangedApplications.add(t) => definitions += range(t, w)
      case _ => ()
    t

  private def modulus(width: Int): BigInt = BigInt(2).pow(width)

  private def bv(op: BvOp, args: List[s2.Term], env: Env): Term = op match
    // The value of a lowered bit-vector is its unsigned value: `ubv_to_int` vanishes, `sbv_to_int`
    // splits off the sign bit, `(_ int_to_bv n)` is the value modulo 2^n through a pinned overflow
    // count — determined uniquely, so sound under both polarities.
    case BvOp.UbvToInt => term(args.head, env)
    case BvOp.SbvToInt => signed(term(args.head, env), width(args.head, env))
    case BvOp.IntToBv(n) => intToBv(term(args.head, env), n)
    case BvOp.Extract(i, j) => extract(i, j, term(args.head, env), width(args.head, env))
    case BvOp.ZeroExtend(_) => term(args.head, env)
    case BvOp.SignExtend(k) => signExtend(k, term(args.head, env), width(args.head, env))
    case BvOp.Concat =>
      args.map(a => (term(a, env), width(a, env))).reduceLeft { case ((hi, wh), (lo, wl)) =>
        (Term.linear(List((BigInt(2).pow(wl), hi), (BigInt(1), lo))), wh + wl)
      }._1
    case _ =>
      val w = width(args.head, env)
      arithmeticBv(op, args.map(term(_, env)), w)

  private def arithmeticBv(op: BvOp, args: List[Term], width: Int): Term =
    val m = modulus(width)
    (op, args) match
      case (BvOp.Add, a :: b :: Nil) => modulo(a + b, width, 0, 1)
      case (BvOp.Sub, a :: b :: Nil) => modulo(a - b, width, -1, 0)
      case (BvOp.Neg, a :: Nil) => modulo(Term.linear(List((BigInt(-1), a))), width, -1, 0)
      case (BvOp.Not, a :: Nil) => Term.linear(List((BigInt(-1), a)), m - 1)
      case (BvOp.Mul, a :: b :: Nil) =>
        (a, b) match
          case (Num(u), Num(v)) => Num((u * v).mod(m))
          case (t, Num(c)) => times(t, c.mod(m), width)
          case (Num(c), t) => times(t, c.mod(m), width)
          case _ => throw s2.Unsupported("bvmul of two variables is not supported (no bit-blasting)")
      case (BvOp.Shl, a :: Num(c) :: Nil) =>
        if c >= width then Num(0) else times(a, BigInt(2).pow(c.toInt), width)
      case (BvOp.Lshr, a :: Num(c) :: Nil) =>
        if c >= width then Num(0) else divide(a, BigInt(2).pow(c.toInt), m / BigInt(2).pow(c.toInt) - 1)._1
      case (BvOp.Udiv, a :: Num(c) :: Nil) => if c.signum == 0 then Num(m - 1) else divide(a, c, (m - 1) / c)._1
      case (BvOp.Urem, a :: Num(c) :: Nil) => if c.signum == 0 then a else divide(a, c, (m - 1) / c)._2
      case (BvOp.Sdiv | BvOp.Srem | BvOp.Smod, a :: Num(c) :: Nil) => signedDivision(op, a, c, width)
      case (BvOp.Ashr, Num(v) :: Num(c) :: Nil) =>
        Num(((if v >= m / 2 then v - m else v) >> c.min(width).toInt).mod(m))
      case (BvOp.Ashr, a :: Num(c) :: Nil) => // the floor of the signed value over 2^c: all ones or zero past the width
        val q =
          if c >= width then signBit(a, width) * -1 else divideSigned(a, width, BigInt(2).pow(c.toInt), floor = true)._1
        modulo(q, width, -1, 0)
      case (BvOp.And, a :: b :: Nil) =>
        (a, b) match
          case (Num(u), Num(v)) => Num(u & v)
          case (t, Num(mask)) => masked(t, mask, width)
          case (Num(mask), t) => masked(t, mask, width)
          case _ => throw s2.Unsupported("bvand of two variables is not supported (no bit-blasting)")
      case _ => throw s2.Unsupported(s"${op.name} with these arguments is not supported (no bit-blasting)")
    end match

  /** `value` reduced modulo `2^width`: a fresh result `r` with
   *  `value = r + 2^width × k`, the overflow count `k` in `[lo, hi]`.
   */
  private def modulo(value: Term, width: Int, lo: BigInt, hi: BigInt): Term = value match
    case Num(v) => Num(v.mod(modulus(width)))
    case _ =>
      val r = freshInt("bv", 0, modulus(width) - 1)
      val k = freshInt("k", lo, hi)
      definitions += IntEq(value, r + k * modulus(width))
      r

  /** `value = divisor × q + r` with `0 ≤ r < divisor` and `q ≤ quotientMax`:
   *  the pair `(q, r)`.
   */
  private def divide(value: Term, divisor: BigInt, quotientMax: BigInt): (Term, Term) = value match
    case Num(v) => (Num(v / divisor), Num(v.mod(divisor)))
    case _ =>
      val q = freshInt("q", 0, quotientMax)
      val r = freshInt("r", 0, divisor - 1)
      definitions += IntEq(value, q * divisor + r)
      (q, r)

  /** `bvsdiv`, `bvsrem` and `bvsmod` of `a` by the constant `c`, of signed
   *  value `T`: the truncating division of the signed value of `a` by `|T|`,
   *  `S = |T| × q + r` with `r` of the sign of `S` ([[divideSigned]]), then the
   *  quotient negated when `T < 0`, the remainder as it is, or the remainder
   *  moved to the sign of `T` (`r + T` when the signs differ and `r ≠ 0`, a
   *  choice bit `z` for the latter), each reduced modulo `2^width`. By zero, as
   *  the standard says: the quotient is all ones for `S ≥ 0` and one for
   *  `S < 0`, the remainders are `a`.
   */
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
      case (_, BvOp.Sdiv) if t.signum == 0 => Term.linear(List((2 - m, signBit(a, width))), m - 1)
      case (_, _) if t.signum == 0 => a
      case _ =>
        val (q, r, s) = divideSigned(a, width, t.abs)
        op match
          case BvOp.Sdiv => modulo(if t.signum > 0 then q else q * -1, width, -1, 0)
          case BvOp.Srem => modulo(r, width, -1, 0)
          case _ =>
            val differ = if t.signum > 0 then s else Term.linear(List((BigInt(-1), s)), 1) // the signs differ
            val z = freshInt("z", 0, 1)
            definitions += Le(z, differ)
            definitions += Or(List(IntEq(r, Num(0)), IntEq(z, differ)))
            definitions += Or(List(Not(IntEq(r, Num(0))), IntEq(z, Num(0))))
            modulo(r + z * t, width, -1, 0)

  /** The signed value `S` of `t` divided by `d > 0`: `S = d × q + r` with
   *  `|r| < d`, truncating — `r` of the sign of `S`, which for the sign bit `s`
   *  is `r ∈ [−(d−1) × s, (d−1) × (1−s)]` — or, with `floor`, `r ∈ [0, d)`.
   *  Either determines `q` and `r`. The triple `(q, r, s)`.
   */
  private def divideSigned(t: Term, width: Int, d: BigInt, floor: Boolean = false): (Term, Term, Term) =
    val half = modulus(width - 1)
    val s = signBit(t, width)
    val q = freshInt("q", -(half / d) - 1, (half - 1) / d)
    val r = freshInt("r", if floor then 0 else -(d - 1), d - 1)
    definitions += IntEq(t - s * modulus(width), q * d + r)
    if !floor then
      definitions += Ge(r, s * -(d - 1))
      definitions += Le(r, Term.linear(List((-(d - 1), s)), d - 1))
    (q, r, s)

  /** The signed value of an `width`-bit term: `t − 2^width × s` for its sign
   *  bit `s`, `t = u + 2^(width−1) × s`.
   */
  private def signed(t: Term, width: Int): Term = t match
    case Num(v) => Num(if v >= modulus(width - 1) then v - modulus(width) else v)
    case _ =>
      val s = freshInt("s", 0, 1)
      val u = freshInt("u", 0, modulus(width - 1) - 1)
      definitions += IntEq(t, u + s * modulus(width - 1))
      t - s * modulus(width)

  /** `(_ int_to_bv width) t`: `t mod 2^width` — a fresh result `r` in
   *  `[0, 2^width)` and an unbounded overflow count `k` with
   *  `t = r + 2^width × k`, both determined uniquely by `t`.
   */
  private def intToBv(t: Term, width: Int): Term = t match
    case Num(v) => Num(v.mod(modulus(width)))
    case _ =>
      val r = freshInt("bv", 0, modulus(width) - 1)
      fresh += 1
      val name = s"k!$fresh"
      introduced += ((name, "Int"))
      val k = Const(name)
      definitions += IntEq(t, r + k * modulus(width))
      r

  /** `c × t` modulo `2^width`, for `0 ≤ c < 2^width`. */
  private def times(t: Term, c: BigInt, width: Int): Term =
    if c.signum == 0 then Num(0) else modulo(t * c, width, 0, c - 1)

  /** `t` masked by `mask`: the low bits (`mask = 2^k − 1`) or the high bits
   *  (`mask = 2^width − 2^k`).
   */
  private def masked(t: Term, mask: BigInt, width: Int): Term =
    val m = modulus(width)
    val low = (0 to width).find(k => mask == BigInt(2).pow(k) - 1)
    val high = (0 to width).find(k => mask == m - BigInt(2).pow(k))
    (low, high) match
      case (Some(k), _) => if k == width then t else divide(t, BigInt(2).pow(k), m / BigInt(2).pow(k) - 1)._2
      case (_, Some(k)) => if k == 0 then t else t - divide(t, BigInt(2).pow(k), m / BigInt(2).pow(k) - 1)._2
      case _ => throw s2.Unsupported(s"bvand with the mask $mask is not supported (no bit-blasting)")

  /** Bits `i` down to `j` of `t`: `t = 2^j × q + low`,
   *  `q = 2^(i−j+1) × high + bits`.
   */
  private def extract(i: Int, j: Int, t: Term, width: Int): Term =
    val q = if j == 0 then t else divide(t, BigInt(2).pow(j), modulus(width - j) - 1)._1
    if i == width - 1 then q else divide(q, modulus(i - j + 1), modulus(width - i - 1) - 1)._2

  /** `t` extended by `k` copies of its sign bit:
   *  `t + s × (2^(width+k) − 2^width)`.
   */
  private def signExtend(k: Int, t: Term, width: Int): Term =
    if k == 0 then t
    else Term.linear(List((BigInt(1), t), (modulus(width + k) - modulus(width), signBit(t, width))))

  private def signBit(t: Term, width: Int): Term = t match
    case Num(v) => Num(if v >= modulus(width - 1) then 1 else 0)
    case _ =>
      val s = freshInt("s", 0, 1)
      val u = freshInt("u", 0, modulus(width - 1) - 1)
      definitions += IntEq(t, u + s * modulus(width - 1))
      s

  private def compare(op: BvCmpOp, a: Term, b: Term, width: Int): Formula =
    val (l, r) = if op.signed then (signed(a, width), signed(b, width)) else (a, b)
    op match
      case BvCmpOp.Ult | BvCmpOp.Slt => Lt(l, r)
      case BvCmpOp.Ule | BvCmpOp.Sle => Le(l, r)
      case BvCmpOp.Ugt | BvCmpOp.Sgt => Gt(l, r)
      case BvCmpOp.Uge | BvCmpOp.Sge => Ge(l, r)
