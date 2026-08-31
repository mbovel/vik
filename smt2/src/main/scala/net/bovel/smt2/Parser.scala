package net.bovel.smt2

import scala.collection.mutable.{ArrayBuffer, HashMap}

import net.bovel.sexpr.{SExpr, SExprParser}
import net.bovel.sexpr.SExpr.{SAtom, SList}
import net.bovel.vik.{BitVectors, Constructor, Datatype, Formula, Lowering, Sorts, Term}

/** Reads an SMT-LIB 2 script (QF_UF, QF_LIA, and the arithmetic fragment of
 *  QF_BV) into a [[Script]], translating terms of uninterpreted sorts and of
 *  `Int` into [[net.bovel.vik.Term]]s and Boolean terms into
 *  [[net.bovel.vik.Formula]]s. Bit-vectors are lowered to integers (see
 *  [[BitVectors]]). Sorts of declared symbols decide which is which: `=` on
 *  Booleans is `Iff`, on integers two bounds; a Boolean-sorted constant or
 *  application is a `Pred`; `+`, `-` and `*` by a numeral build linear
 *  combinations, `<=`, `<`, `>=` and `>` bounds. `let` binds names to
 *  translated values; `define-fun` is a macro; `distinct` is expanded to
 *  pairwise disequalities; `ite` on terms is lowered to a fresh constant with
 *  two defining assertions placed before the assertion that uses it. Functions
 *  with `Int` in their signature are rejected: the solver does not combine the
 *  two theories yet.
 */
/** One parse: the class carries the declarations, macros and sorts the commands
 *  build up, so [[Parser.parse]] makes a fresh one per input.
 */
private final class Parser extends Lowering:
  import Formula.*

  private enum Value:
    /** A term, made under `depth` lambda parameters (its free variables are
     *  shifted when used under more).
     */
    case T(term: Term, sort: String, depth: Int)
    case F(formula: Formula, depth: Int)

    /** A parameter of an enclosing lambda: its level (the parameters of the
     *  lambdas outside it, plus its position).
     */
    case Bound(level: Int, sort: String)

  /** The number of parameters of the enclosing lambdas. */
  private var depth = 0

  private def arrow(sort: String): Option[(List[String], String)] = Sorts.arrow(sort)

  /** The sort of a function of `argSorts` to `sort`: a function symbol is a
   *  value of a function sort.
   */
  private def functionSort(declaration: (List[String], String)): String = Sorts.function(declaration._1, declaration._2)

  /** The sort of a term of `sort` applied to `n` arguments. */
  private def result(sort: String, n: Int): String =
    val applied = Sorts.applied(sort, n)
    if applied == "?" then throw Smt2Error(s"$sort applied to $n arguments") else applied

  /** The parameters of a lambda, `((x S) ...)`, with their sorts, and an
   *  environment binding them: parameter `i` of `n` is `Var(i)` in the body, at
   *  level `depth + n - 1 - i`.
   */
  private def parameters(params: List[SExpr]): List[(String, String)] = params.map {
    case SList(SAtom(p) :: sort :: Nil) =>
      checkSort(sort.toString)
      (p, sort.toString)
    case other => throw Smt2Error(s"bad parameter $other")
  }

  private def bindParameters(ps: List[(String, String)], env: Map[String, Value]): Map[String, Value] =
    ps.zipWithIndex.foldLeft(env) { case (e, ((p, sort), i)) =>
      e + (p -> Value.Bound(depth + ps.length - 1 - i, sort))
    }

  private val functions = HashMap.empty[String, (List[String], String)] // name -> argument sorts, result sort
  private val macros = HashMap.empty[String, (List[(String, String)], String, SExpr)]
  private var fresh = 0

  /** Assertions defining the constants introduced for term-level `ite`s of the
   *  assertion being translated.
   */
  private val definitions = ArrayBuffer.empty[Formula]

  /** The constants introduced for those `ite`s, with their sorts, declared
   *  before the definitions.
   */
  private val introduced = ArrayBuffer.empty[(String, String)]

  def parse(input: String): Script = Script(SExprParser.parse(input).flatMap(command))

  private def command(sexpr: SExpr): List[Command] = sexpr match
    // Bit-vectors are lowered to integers, so the script that comes out is in the corresponding integer logic.
    case SList(SAtom("set-logic") :: SAtom(logic) :: Nil) => List(Command.SetLogic(logic.replace("BV", "LIA")))
    case SList(SAtom("set-info") :: SAtom(key) :: value :: Nil) => List(Command.SetInfo(key, value.toString))
    case SList(SAtom("declare-sort") :: SAtom(name) :: SAtom(arity) :: Nil) =>
      List(Command.DeclareSort(name, arity.toInt))
    case SList(SAtom("declare-sort") :: SAtom(name) :: Nil) => List(Command.DeclareSort(name, 0))
    case SList(SAtom("declare-fun") :: SAtom(name) :: SList(args) :: sort :: Nil) =>
      declare(name, args.map(_.toString), sort.toString)
    case SList(SAtom("declare-const") :: SAtom(name) :: sort :: Nil) =>
      arrow(sort.toString) match // a constant of a function sort is a function symbol
        case Some((argSorts, result)) => declare(name, argSorts, result)
        case None => declare(name, Nil, sort.toString)
    case SList(SAtom("declare-datatype") :: SAtom(name) :: SList(constructors) :: Nil) =>
      List(declareDatatypes(List((name, constructors))))
    case SList(SAtom("declare-datatypes") :: SList(names) :: SList(declarations) :: Nil) =>
      val sorts = names.map {
        case SList(SAtom(name) :: SAtom("0") :: Nil) => name
        case other => throw Smt2Error(s"unsupported datatype declaration $other (parametric?)")
      }
      if sorts.length != declarations.length then
        throw Smt2Error("declare-datatypes: as many declarations as sorts expected")
      List(declareDatatypes(sorts.zip(declarations.map {
        case SList(constructors) => constructors
        case other => throw Smt2Error(s"bad constructor list $other")
      })))
    case SList(SAtom("define-fun") :: SAtom(name) :: SList(params) :: sort :: body :: Nil) =>
      val ps = params.map {
        case SList(SAtom(p) :: s :: Nil) => (p, s.toString)
        case other => throw Smt2Error(s"bad parameter $other")
      }
      macros(name) = (ps, sort.toString, body)
      List(Command.DefineFun(name, ps, sort.toString, body))
    case SList(SAtom("assert") :: SList(SAtom("!") :: body :: attributes) :: Nil)
        if attributes.sliding(2).exists { case Seq(SAtom(":named"), _) => true; case _ => false } =>
      introduced.clear()
      definitions.clear()
      val name = attributes.sliding(2).collectFirst { case Seq(SAtom(":named"), SAtom(n)) => n }.get
      val f = formula(body, Map.empty)
      introduced.toList.map((k, sort) => Command.DeclareFun(k, Nil, sort)) ++
        definitions.toList.map(Command.Assert(_)) :+ Command.AssertNamed(name, f)
    case SList(SAtom("assert") :: body :: Nil) =>
      introduced.clear()
      definitions.clear()
      val f = formula(body, Map.empty)
      introduced.toList.map((k, sort) => Command.DeclareFun(k, Nil, sort)) ++
        definitions.toList.map(Command.Assert(_)) :+ Command.Assert(f)
    case SList(SAtom("check-sat") :: Nil) => List(Command.CheckSat)
    case SList(SAtom("check-sat-using") :: _) => List(Command.CheckSat) // z3's, with a tactic: ignored
    case SList(SAtom("check-sat-assuming") :: SList(assumptions) :: Nil) => checkSatAssuming(assumptions)
    case SList(SAtom("check-sat") :: assumptions) => checkSatAssuming(assumptions) // z3's shorthand
    case SList(SAtom("reset-assertions") :: Nil) => List(Command.ResetAssertions)
    case SList(SAtom("reset") :: Nil) =>
      functions.clear()
      macros.clear()
      List(Command.Reset)
    case SList(SAtom("get-unsat-core") :: Nil) => List(Command.GetUnsatCore)
    case SList(SAtom(name @ ("block-model" | "block-model-values" | "get-unsat-assumptions")) :: _) =>
      throw Smt2Error(s"$name is not supported")
    case SList(SAtom("push") :: SAtom(n) :: Nil) => List(Command.Push(n.toInt))
    case SList(SAtom("push") :: Nil) => List(Command.Push(1))
    case SList(SAtom("pop") :: SAtom(n) :: Nil) => List(Command.Pop(n.toInt))
    case SList(SAtom("pop") :: Nil) => List(Command.Pop(1))
    case SList(SAtom("get-model") :: Nil) => List(Command.GetModel)
    case SList(SAtom("get-value") :: SList(items) :: Nil) =>
      // An item outside the fragment (another theory's term) leaves the whole command unanswered, as before.
      try
        List(Command.GetValue(items.map(s =>
          s.toString -> (if sortOf(s, Map.empty) == "Bool" then formula(s, Map.empty) else term(s, Map.empty))
        )))
      catch case _: Smt2Error => List(Command.Other(sexpr))
    case SList(SAtom("set-option") :: SAtom(key) :: value :: Nil) => List(Command.SetOption(key, value.toString))
    case SList(SAtom("get-info") :: SAtom(key) :: Nil) => List(Command.GetInfo(key))
    case SList(SAtom("exit") :: Nil) => List(Command.Exit)
    case SList(SAtom(_) :: _) => List(Command.Other(sexpr))
    case other => throw Smt2Error(s"not a command: $other")

  private def checkSatAssuming(assumptions: List[SExpr]): List[Command] =
    introduced.clear()
    definitions.clear()
    val fs = assumptions.map(formula(_, Map.empty))
    introduced.toList.map((k, sort) => Command.DeclareFun(k, Nil, sort)) ++
      definitions.toList.map(Command.Assert(_)) :+ Command.CheckSatAssuming(fs)

  /** Declares `name`: a bit-vector constant becomes an `Int` constant with its
   *  range asserted; functions over `Int` or bit-vectors are rejected (no
   *  theory combination yet).
   */
  private def declare(name: String, argSorts: List[String], sort: String): List[Command] =
    (sort :: argSorts).foreach(checkSort)
    functions(name) = (argSorts, sort)
    val width = BitVectors.width(sort)
    if argSorts.isEmpty && width >= 0 then
      List(Command.DeclareFun(name, Nil, "Int"), Command.Assert(BitVectors.range(Term.Const(name), width)))
    else List(Command.DeclareFun(name, argSorts, sort))

  /** Rejects the sorts of theories that are not supported, which would
   *  otherwise pass for uninterpreted sorts: reals, strings, sets, arrays, ...
   */
  private def checkSort(sort: String): Unit =
    val unsupported = List(
      "String",
      "RegLan",
      "RoundingMode",
      "UnitTuple",
      "(Set ",
      "(Bag ",
      "(Seq ",
      "(Array ",
      "(Tuple ",
      "(_ FloatingPoint",
      "Float"
    )
    if unsupported.exists(u => sort == u || sort.startsWith(u)) then throw Smt2Error(s"the sort $sort is not supported")
    Sorts.arrow(sort).foreach((params, result) => (result :: params).foreach(checkSort))

  /** Whether terms of `sort` are integers after lowering: `Int` or a bit-vector
   *  sort.
   */
  private def isArithmetic(sort: String): Boolean =
    sort == "Int" || sort == "Real" || BitVectors.width(sort) >= 0

  /** Declares datatypes: their constructors, selectors and testers become
   *  function symbols (a constructor `C` of datatype `D` with selectors
   *  `(s_i S_i)` is `C : S_1 ... S_n -> D`, `s_i : D -> S_i` and the tester
   *  `(_ is C) : D -> Bool`).
   */
  private def declareDatatypes(declarations: List[(String, List[SExpr])]): Command =
    val datatypes = declarations.map { (name, constructors) =>
      Datatype(
        name,
        constructors.map {
          case SList(SAtom(constructor) :: selectors) =>
            Constructor(
              constructor,
              selectors.map {
                case SList(SAtom(selector) :: sort :: Nil) => (selector, sort.toString)
                case other => throw Smt2Error(s"bad selector $other")
              }
            )
          case other => throw Smt2Error(s"bad constructor $other")
        }
      )
    }
    for datatype <- datatypes; constructor <- datatype.constructors do
      functions(constructor.name) = (constructor.selectors.map(_._2), datatype.name)
      functions(constructor.tester) = (List(datatype.name), "Bool")
      for (selector, sort) <- constructor.selectors do functions(selector) = (List(datatype.name), sort)
    Command.DeclareDatatypes(datatypes)

  private def isNumeral(name: String): Boolean = name.nonEmpty && name.forall(_.isDigit)

  private def isDecimal(name: String): Boolean =
    val i = name.indexOf('.')
    i > 0 && i < name.length - 1 && name.take(i).forall(_.isDigit) && name.drop(i + 1).forall(_.isDigit)

  /** The term of a decimal literal: a numeral when integral, else a rational
   *  constant (see [[rationalConst]]).
   */
  private def decimalTerm(name: String): Term =
    val i = name.indexOf('.')
    val fraction = name.drop(i + 1)
    val q = BigInt(10).pow(fraction.length)
    rationalConst(BigInt(name.take(i)) * q + BigInt(fraction), q)

  private val rationals = scala.collection.mutable.HashMap.empty[(BigInt, BigInt), Term]

  /** The value `p / q` of every term made by [[rationalConst]], for the literal
   *  folding of products and divisions.
   */
  private val rationalValues = scala.collection.mutable.HashMap.empty[Term, (BigInt, BigInt)]

  /** The rational value of a term when it is a literal: a numeral, a rational
   *  constant, or an integer multiple of one — `(- 0.1)` parses to the latter.
   */
  private def literalValue(t: Term): Option[(BigInt, BigInt)] = t match
    case Term.Num(v) => Some((v, BigInt(1)))
    case _ if rationalValues.contains(t) => Some(rationalValues(t))
    case Term.Linear(List((a, u)), c) if c.signum == 0 && rationalValues.contains(u) =>
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
    if q == BigInt(1) then Term.Num(p)
    else
      rationals.getOrElseUpdate(
        (p, q), {
          if depth > 0 then throw Smt2Error("a rational literal under a lambda is not supported")
          fresh += 1
          val name = s"rat!$fresh"
          introduced += ((name, "Real"))
          functions(name) = (Nil, "Real")
          val k = Term.Const(name)
          definitions += IntEq(Term.linear(List((q, k))), Term.Num(p))
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
          if depth > 0 then throw Smt2Error("a rational coefficient under a lambda is not supported")
          fresh += 1
          val name = s"rat!$fresh"
          introduced += ((name, "Real"))
          functions(name) = (Nil, "Real")
          val k = Term.Const(name)
          definitions += IntEq(Term.linear(List((q, k))), Term.linear(List((p, t))))
          k
        }
      )

  private val rationalProducts = scala.collection.mutable.HashMap.empty[(BigInt, BigInt, Term), Term]

  private def sortOf(sexpr: SExpr, env: Map[String, Value]): String = sexpr match
    case SAtom("true" | "false") => "Bool"
    case SAtom(name) if isNumeral(name) => "Int"
    case SAtom(name) if isDecimal(name) => "Real"
    case SAtom(name) if name.startsWith("#b") => BitVectors.sort(name.length - 2)
    case SAtom(name) if name.startsWith("#x") => BitVectors.sort(4 * (name.length - 2))
    case SList(SAtom("_") :: SAtom(literal) :: SAtom(width) :: Nil) if literal.startsWith("bv") =>
      BitVectors.sort(width.toInt)
    case SList(SAtom(BitVectors.Arithmetic(_)) :: a :: _) => sortOf(a, env)
    case SList(SAtom(BitVectors.Comparison(_)) :: _) => "Bool"
    case SList(SList(SAtom("_") :: SAtom("extract") :: SAtom(i) :: SAtom(j) :: Nil) :: _ :: Nil) =>
      BitVectors.sort(i.toInt - j.toInt + 1)
    case SList(SList(SAtom("_") :: SAtom("zero_extend" | "sign_extend") :: SAtom(k) :: Nil) :: a :: Nil) =>
      BitVectors.sort(BitVectors.width(sortOf(a, env)) + k.toInt)
    case SList(SAtom("concat") :: args) => BitVectors.sort(args.map(a => BitVectors.width(sortOf(a, env))).sum)
    case SAtom(name) =>
      env.get(name) match
        case Some(Value.F(_, _)) => "Bool"
        case Some(Value.T(_, sort, _)) => sort
        case Some(Value.Bound(_, sort)) => sort
        case None => functions.get(name).map(functionSort).orElse(macros.get(name).map(_._2)).getOrElse(throw Smt2Error(
            s"unknown symbol $name"
          ))
    case SList(SAtom("not" | "and" | "or" | "=>" | "xor" | "=" | "distinct" | "<=" | "<" | ">=" | ">") :: _) => "Bool"
    case SList(SAtom("/") :: _) => "Real"
    case SList(SAtom("+" | "-" | "*") :: args) => if args.exists(a => sortOf(a, env) == "Real") then "Real" else "Int"
    case SList(SAtom("lambda") :: SList(params) :: body :: Nil) =>
      val ps = parameters(params)
      val inner = bindParameters(ps, env)
      depth += ps.length
      val res = try sortOf(body, inner)
      finally depth -= ps.length
      s"(-> ${ps.map(_._2).mkString(" ")} $res)"
    case SList(SAtom(name) :: args) if env.contains(name) => result(sortOf(SAtom(name), env), args.length)
    case SList(SList(SAtom("_") :: SAtom("is") :: SAtom(_) :: Nil) :: _ :: Nil) => "Bool"
    case SList(SAtom("ite") :: _ :: thn :: _ :: Nil) => sortOf(thn, env)
    case SList(SAtom("let") :: SList(bindings) :: body :: Nil) => sortOf(body, bind(bindings, env))
    case SList(SAtom(name) :: args) => // a declared function applied to some of its arguments, or more
      functions.get(name).map(d => result(functionSort(d), args.length)).orElse(macros.get(name).map(_._2)).getOrElse(
        throw Smt2Error(s"unknown function $name")
      )
    case SList((fun @ SList(_)) :: args) => result(sortOf(fun, env), args.length)
    case other => throw Smt2Error(s"cannot type $other")

  private def bind(bindings: List[SExpr], env: Map[String, Value]): Map[String, Value] =
    env ++ bindings.map {
      case SList(SAtom(name) :: value :: Nil) =>
        val sort = sortOf(value, env)
        name -> (if sort == "Bool" then Value.F(formula(value, env), depth) else Value.T(term(value, env), sort, depth))
      case other => throw Smt2Error(s"bad binding $other")
    }

  private def formula(sexpr: SExpr, env: Map[String, Value]): Formula = sexpr match
    case SAtom("true") => True
    case SAtom("false") => False
    case SAtom(name) =>
      env.get(name) match
        case Some(Value.F(f, d)) => f.mapTerms(Term.shift(_, depth - d))
        case Some(Value.T(t, "Bool", d)) => Formula.pred(Term.shift(t, depth - d))
        case Some(Value.Bound(level, "Bool")) => Formula.Pred(Term.Var(depth - 1 - level))
        case Some(Value.T(_, _, _) | Value.Bound(_, _)) => throw Smt2Error(s"$name is not Boolean")
        case None =>
          if !macros.contains(name) then Pred(declared(name))
          else
            expand(name, Nil, env) match
              case Value.F(f, _) => f
              case Value.T(_, _, _) => throw Smt2Error(s"$name is not Boolean")
    case SList(SAtom("not") :: arg :: Nil) => Not(formula(arg, env))
    case SList(SAtom("and") :: args) => And(args.map(formula(_, env)))
    case SList(SAtom("or") :: args) => Or(args.map(formula(_, env)))
    case SList(SAtom("=>") :: args) => args.map(formula(_, env)).reduceRight(Implies(_, _))
    case SList(SAtom("xor") :: lhs :: rhs :: Nil) => Not(Iff(formula(lhs, env), formula(rhs, env)))
    case SList(SAtom("=") :: args) =>
      sortOf(args.head, env) match
        case "Bool" =>
          val fs = args.map(formula(_, env))
          chain(fs.zip(fs.tail).map(Iff(_, _)))
        case sort =>
          val ts = args.map(term(_, env))
          chain(ts.zip(ts.tail).map(equal(sort, _, _)))
    case SList(SAtom("distinct") :: args) =>
      val ts = args.map(term(_, env))
      if isArithmetic(sortOf(args.head, env)) then IntDistinct(ts) else Distinct(ts)
    case SList(SAtom(BitVectors.Comparison(op)) :: a :: b :: Nil) =>
      val width = BitVectors.width(sortOf(a, env))
      BitVectors.compare(op, term(a, env), term(b, env), width, this)
    case SList(SList(SAtom("_") :: SAtom("is") :: SAtom(constructor) :: Nil) :: arg :: Nil) =>
      Pred(Term.App(declared(s"(_ is $constructor)"), List(term(arg, env))))
    case SList(SAtom(op @ ("<=" | "<" | ">=" | ">")) :: args) =>
      for a <- args if !isArithmetic(sortOf(a, env)) do throw Smt2Error(s"$op on ${sortOf(a, env)} is not supported")
      val ts = args.map(term(_, env))
      val real = args.exists(a => sortOf(a, env) == "Real") // any side: numerals alone say Int
      val compare: (Term, Term) => Formula = op match
        case "<=" => Le(_, _)
        case "<" => if real then Less(_, _) else Lt(_, _)
        case ">=" => Ge(_, _)
        case _ => if real then (lhs, rhs) => Less(rhs, lhs) else Gt(_, _)
      chain(ts.zip(ts.tail).map(compare(_, _)))
    case SList(SAtom("ite") :: cond :: thn :: els :: Nil) =>
      Ite(formula(cond, env), formula(thn, env), formula(els, env))
    case SList(SAtom("let") :: SList(bindings) :: body :: Nil) => formula(body, bind(bindings, env))
    case SList(SAtom("!") :: body :: _) => formula(body, env) // an annotated term
    case SList(SAtom(name) :: args) if macros.contains(name) =>
      expand(name, args, env) match
        case Value.F(f, _) => f
        case Value.T(_, _, _) => throw Smt2Error(s"$name is not Boolean")
    case SList(SAtom(name) :: args) => Pred(Term.App(declared(name), args.map(term(_, env))))
    case other => throw Smt2Error(s"not a formula: $other")

  /** Equality of two terms of `sort`. */
  private def equal(sort: String, lhs: Term, rhs: Term): Formula =
    if isArithmetic(sort) then IntEq(lhs, rhs) else Eq(lhs, rhs)

  /** A fresh `Int` constant in `[lo, hi]`, declared and bounded before the
   *  current assertion (for the lowering of bit-vectors).
   */
  def freshInt(prefix: String, lo: BigInt, hi: BigInt): Term =
    if depth > 0 then throw Smt2Error("bit-vector operations under a lambda are not supported")
    fresh += 1
    val name = s"$prefix!$fresh"
    introduced += ((name, "Int"))
    functions(name) = (Nil, "Int")
    val k = Term.Const(name)
    definitions += Ge(k, Term.Num(lo))
    definitions += Le(k, Term.Num(hi))
    k

  /** Asserts `formula` before the current assertion. */
  def define(formula: Formula): Unit = definitions += formula

  private def term(sexpr: SExpr, env: Map[String, Value]): Term = sexpr match
    case SAtom("true") => Term.boolean(Formula.True)
    case SAtom("false") => Term.boolean(Formula.False)
    case SAtom(name) if isNumeral(name) => Term.Num(BigInt(name))
    case SAtom(name) if isDecimal(name) => decimalTerm(name)
    case SAtom(name) if name.startsWith("#b") => Term.Num(BigInt(name.drop(2), 2))
    case SAtom(name) if name.startsWith("#x") => Term.Num(BigInt(name.drop(2), 16))
    case SList(SAtom("_") :: SAtom(literal) :: SAtom(width) :: Nil) if literal.startsWith("bv") =>
      Term.Num(BigInt(literal.drop(2)).mod(BigInt(2).pow(width.toInt)))
    case SList(SAtom(BitVectors.Arithmetic(op)) :: args) =>
      val width = BitVectors.width(sortOf(args.head, env))
      BitVectors.arithmetic(op, args.map(term(_, env)), width, this)
    case SList(SList(SAtom("_") :: SAtom("extract") :: SAtom(i) :: SAtom(j) :: Nil) :: a :: Nil) =>
      BitVectors.extract(i.toInt, j.toInt, term(a, env), BitVectors.width(sortOf(a, env)), this)
    case SList(SList(SAtom("_") :: SAtom("zero_extend") :: SAtom(_) :: Nil) :: a :: Nil) => term(a, env)
    case SList(SList(SAtom("_") :: SAtom("sign_extend") :: SAtom(k) :: Nil) :: a :: Nil) =>
      BitVectors.signExtend(k.toInt, term(a, env), BitVectors.width(sortOf(a, env)), this)
    case SList(SAtom("concat") :: args) =>
      args.map(a => (term(a, env), BitVectors.width(sortOf(a, env)))).reduceLeft { case ((hi, wh), (lo, wl)) =>
        (Term.linear(List((BigInt(2).pow(wl), hi), (BigInt(1), lo))), wh + wl)
      }._1
    case SAtom(name) =>
      env.get(name) match
        case Some(Value.T(t, _, d)) => Term.shift(t, depth - d)
        case Some(Value.Bound(level, _)) => Term.Var(depth - 1 - level)
        case Some(Value.F(f, d)) => Term.boolean(f.mapTerms(Term.shift(_, depth - d)))
        case None =>
          if !macros.contains(name) then declared(name)
          else
            expand(name, Nil, env) match
              case Value.T(t, _, _) => t
              case Value.F(f, _) => Term.boolean(f)
    case SList(SAtom("lambda") :: SList(params) :: body :: Nil) =>
      val ps = parameters(params)
      val inner = bindParameters(ps, env)
      depth += ps.length
      try Term.Lambda(ps.map(_._2), term(body, inner))
      finally depth -= ps.length
    case SList(SAtom("-") :: arg :: Nil) => Term.linear(List((-1, term(arg, env))))
    case SList(SAtom("-") :: first :: rest) =>
      Term.linear((BigInt(1), term(first, env)) :: rest.map(a => (BigInt(-1), term(a, env))))
    case SList(SAtom("+") :: args) => Term.linear(args.map(a => (BigInt(1), term(a, env))))
    case SList(SAtom("*") :: args) =>
      // Linear: all factors but at most one are literals, rationals included.
      val (literals, others) = args.map(term(_, env)).partitionMap { t =>
        literalValue(t).toLeft(t)
      }
      val (p, q) = literals.foldLeft((BigInt(1), BigInt(1))) { case ((p, q), (a, b)) => (p * a, q * b) }
      others match
        case Nil => rationalConst(p, q)
        case t :: Nil => rationalProduct(p, q, t)
        case _ => throw Smt2Error(s"non-linear multiplication: $sexpr")
    case SList(SAtom("/") :: a :: b :: Nil) =>
      // Real division by a rational literal, `(/ 1 3)` and `(/ x 2)` alike; the rest is nonlinear.
      val divisor = literalValue(term(b, env)).getOrElse(throw Smt2Error(s"nonlinear division: $sexpr"))
      val numerator = term(a, env)
      literalValue(numerator) match
        case Some((p, q)) => rationalConst(p * divisor._2, q * divisor._1)
        case None => rationalProduct(divisor._2, divisor._1, numerator)
    case SList(SAtom("ite") :: cond :: thn :: els :: Nil) =>
      // A fresh constant `k` with `cond => k = thn` and `not cond => k = els`, asserted before the current assertion.
      if depth > 0 then throw Smt2Error("ite under a lambda is not supported")
      fresh += 1
      val name = s"ite!$fresh"
      val k = Term.Const(name)
      val sort = sortOf(thn, env)
      introduced +=
        ((
          name,
          if sort == "Real" then "Real" else if isArithmetic(sort) then "Int" else sort
        )) // declared as it is printed: lowered
      functions(name) = (Nil, sort)
      val c = formula(cond, env)
      definitions += Implies(c, equal(sort, k, term(thn, env)))
      definitions += Implies(Not(c), equal(sort, k, term(els, env)))
      k
    case SList(SAtom("let") :: SList(bindings) :: body :: Nil) => term(body, bind(bindings, env))
    case SList(SAtom("!") :: body :: _) => term(body, env)
    case SList(SAtom(op) :: _)
        if !functions.contains(op) && !macros.contains(op) && !env.contains(op) && sortOf(sexpr, env) == "Bool" =>
      Term.boolean(formula(sexpr, env)) // a formula as a term: a connective, a comparison, an equality
    case SList(SList(SAtom("_") :: SAtom("is") :: _) :: _ :: Nil) => Term.boolean(formula(sexpr, env)) // a tester
    case SList(SAtom(name) :: args) if env.contains(name) =>
      apply(term(SAtom(name), env), sortOf(SAtom(name), env), args, env)
    case SList(SAtom(name) :: args) if macros.contains(name) =>
      expand(name, args, env) match
        case Value.T(t, _, _) => t
        case Value.F(f, _) => Term.boolean(f)
    case SList(SAtom(name) :: args) => apply(declared(name), functionSort(functions(name)), args, env)
    case SList((fun @ SList(_)) :: args) => apply(term(fun, env), sortOf(fun, env), args, env)
    case other => throw Smt2Error(s"not a term: $other")

  /** `fun`, of sort `sort`, applied to `args`: to as many as its sort takes,
   *  then the result to the rest (`(h 1 2)` is `((h 1) 2)` for
   *  `h : (-> Int (-> Int Int))`).
   */
  private def apply(fun: Term, sort: String, args: List[SExpr], env: Map[String, Value]): Term = arrow(sort) match
    case Some((params, res)) if args.length > params.length =>
      apply(Term.app(fun, args.take(params.length).map(term(_, env))), res, args.drop(params.length), env)
    case _ => Term.app(fun, args.map(term(_, env)))

  /** The conjunction of `conditions`, itself when there is only one. */
  private def chain(conditions: List[Formula]): Formula = conditions match
    case single :: Nil => single
    case _ => And(conditions)

  /** A declared symbol, or an error. */
  private def declared(name: String): Term =
    if functions.contains(name) then Term.Const(name) else throw Smt2Error(s"unknown symbol $name")

  /** Expands the macro `name` applied to `args` (evaluated in `env`). */
  private def expand(name: String, args: List[SExpr], env: Map[String, Value]): Value.T | Value.F =
    val (params, sort, body) = macros(name)
    if params.length != args.length then throw Smt2Error(s"$name expects ${params.length} arguments")
    val inner = params.zip(args).foldLeft(env) { case (e, ((p, s), a)) =>
      e + (p -> (if s == "Bool" then Value.F(formula(a, env), depth) else Value.T(term(a, env), s, depth)))
    }
    if sort == "Bool" then Value.F(formula(body, inner), depth) else Value.T(term(body, inner), sort, depth)

object Parser:
  def parse(input: String): Script = Parser().parse(input)
