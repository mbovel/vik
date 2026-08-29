package net.bovel.smt2

import scala.collection.mutable.{ArrayBuffer, HashMap}

import net.bovel.sexpr.{SExpr, SExprParser}
import net.bovel.sexpr.SExpr.{Atom, SList}
import net.bovel.smt.{Formula, Term}

/** Reads an SMT-LIB 2 script (QF_UF, QF_LIA, and the arithmetic fragment of
 *  QF_BV) into a [[Script]], translating terms of uninterpreted sorts and of
 *  `Int` into [[Term]]s and Boolean terms into [[Formula]]s. Bit-vectors are
 *  lowered to integers (see [[BitVectors]]). Sorts of declared symbols decide
 *  which is which: `=` on Booleans is `Iff`, on integers two bounds; a
 *  Boolean-sorted constant or application is a `Pred`; `+`, `-` and `*` by a
 *  numeral build linear combinations, `<=`, `<`, `>=` and `>` bounds. `let`
 *  binds names to translated values; `define-fun` is a macro; `distinct` is
 *  expanded to pairwise disequalities; `ite` on terms is lowered to a fresh
 *  constant with two defining assertions placed before the assertion that uses
 *  it. Functions with `Int` in their signature are rejected: the solver does
 *  not combine the two theories yet.
 */
final class Parser:
  import Formula.*

  private enum Value:
    case T(term: Term, sort: String)
    case F(formula: Formula)

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

  def parse(input: String): Script = Script(SExprParser.parseAll(input).flatMap(command))

  private def command(sexpr: SExpr): List[Command] = sexpr match
    // Bit-vectors are lowered to integers, so the script that comes out is in the corresponding integer logic.
    case SList(Atom("set-logic") :: Atom(logic) :: Nil) => List(Command.SetLogic(logic.replace("BV", "LIA")))
    case SList(Atom("set-info") :: Atom(key) :: value :: Nil) => List(Command.SetInfo(key, value.toString))
    case SList(Atom("declare-sort") :: Atom(name) :: Atom(arity) :: Nil) => List(Command.DeclareSort(name, arity.toInt))
    case SList(Atom("declare-sort") :: Atom(name) :: Nil) => List(Command.DeclareSort(name, 0))
    case SList(Atom("declare-fun") :: Atom(name) :: SList(args) :: sort :: Nil) =>
      declare(name, args.map(_.toString), sort.toString)
    case SList(Atom("declare-const") :: Atom(name) :: sort :: Nil) => declare(name, Nil, sort.toString)
    case SList(Atom("define-fun") :: Atom(name) :: SList(params) :: sort :: body :: Nil) =>
      val ps = params.map {
        case SList(Atom(p) :: s :: Nil) => (p, s.toString)
        case other => throw Smt2Error(s"bad parameter $other")
      }
      macros(name) = (ps, sort.toString, body)
      List(Command.DefineFun(name, ps, sort.toString, body))
    case SList(Atom("assert") :: body :: Nil) =>
      introduced.clear()
      definitions.clear()
      val f = formula(body, Map.empty)
      introduced.toList.map((k, sort) => Command.DeclareFun(k, Nil, sort)) ++
        definitions.toList.map(Command.Assert(_)) :+ Command.Assert(f)
    case SList(Atom("check-sat") :: Nil) => List(Command.CheckSat)
    case SList(Atom("push") :: Atom(n) :: Nil) => List(Command.Push(n.toInt))
    case SList(Atom("push") :: Nil) => List(Command.Push(1))
    case SList(Atom("pop") :: Atom(n) :: Nil) => List(Command.Pop(n.toInt))
    case SList(Atom("pop") :: Nil) => List(Command.Pop(1))
    case SList(Atom("get-model") :: Nil) => List(Command.GetModel)
    case SList(Atom("exit") :: Nil) => List(Command.Exit)
    case SList(Atom(_) :: _) => List(Command.Other(sexpr))
    case other => throw Smt2Error(s"not a command: $other")

  /** Declares `name`: a bit-vector constant becomes an `Int` constant with its
   *  range asserted; functions over `Int` or bit-vectors are rejected (no
   *  theory combination yet).
   */
  private def declare(name: String, argSorts: List[String], sort: String): List[Command] =
    if argSorts.nonEmpty && (sort :: argSorts).exists(isArithmetic) then
      throw Smt2Error(s"$name: functions over Int or bit-vectors are not supported (no theory combination yet)")
    functions(name) = (argSorts, sort)
    val width = BitVectors.width(sort)
    if argSorts.isEmpty && width >= 0 then
      List(Command.DeclareFun(name, Nil, "Int"), Command.Assert(BitVectors.range(Term.Const(name), width)))
    else List(Command.DeclareFun(name, argSorts, sort))

  /** Whether terms of `sort` are integers after lowering: `Int` or a bit-vector
   *  sort.
   */
  private def isArithmetic(sort: String): Boolean = sort == "Int" || BitVectors.width(sort) >= 0

  private def isNumeral(name: String): Boolean = name.nonEmpty && name.forall(_.isDigit)

  private def sortOf(sexpr: SExpr, env: Map[String, Value]): String = sexpr match
    case Atom("true" | "false") => "Bool"
    case Atom(name) if isNumeral(name) => "Int"
    case Atom(name) if name.startsWith("#b") => BitVectors.sort(name.length - 2)
    case Atom(name) if name.startsWith("#x") => BitVectors.sort(4 * (name.length - 2))
    case SList(Atom("_") :: Atom(literal) :: Atom(width) :: Nil) if literal.startsWith("bv") =>
      BitVectors.sort(width.toInt)
    case SList(Atom(BitVectors.Arithmetic(_)) :: a :: _) => sortOf(a, env)
    case SList(Atom(BitVectors.Comparison(_)) :: _) => "Bool"
    case SList(SList(Atom("_") :: Atom("extract") :: Atom(i) :: Atom(j) :: Nil) :: _ :: Nil) =>
      BitVectors.sort(i.toInt - j.toInt + 1)
    case SList(SList(Atom("_") :: Atom("zero_extend" | "sign_extend") :: Atom(k) :: Nil) :: a :: Nil) =>
      BitVectors.sort(BitVectors.width(sortOf(a, env)) + k.toInt)
    case SList(Atom("concat") :: args) => BitVectors.sort(args.map(a => BitVectors.width(sortOf(a, env))).sum)
    case Atom(name) =>
      env.get(name) match
        case Some(Value.F(_)) => "Bool"
        case Some(Value.T(_, sort)) => sort
        case None => functions.get(name).map(_._2).orElse(macros.get(name).map(_._2)).getOrElse(throw Smt2Error(
            s"unknown symbol $name"
          ))
    case SList(Atom("not" | "and" | "or" | "=>" | "xor" | "=" | "distinct" | "<=" | "<" | ">=" | ">") :: _) => "Bool"
    case SList(Atom("+" | "-" | "*") :: _) => "Int"
    case SList(Atom("ite") :: _ :: thn :: _ :: Nil) => sortOf(thn, env)
    case SList(Atom("let") :: SList(bindings) :: body :: Nil) => sortOf(body, bind(bindings, env))
    case SList(Atom(name) :: _) => functions.get(
        name
      ).map(_._2).orElse(macros.get(name).map(_._2)).getOrElse(throw Smt2Error(s"unknown function $name"))
    case other => throw Smt2Error(s"cannot type $other")

  private def bind(bindings: List[SExpr], env: Map[String, Value]): Map[String, Value] =
    env ++ bindings.map {
      case SList(Atom(name) :: value :: Nil) =>
        val sort = sortOf(value, env)
        name -> (if sort == "Bool" then Value.F(formula(value, env)) else Value.T(term(value, env), sort))
      case other => throw Smt2Error(s"bad binding $other")
    }

  private def formula(sexpr: SExpr, env: Map[String, Value]): Formula = sexpr match
    case Atom("true") => True
    case Atom("false") => False
    case Atom(name) =>
      env.get(name) match
        case Some(Value.F(f)) => f
        case Some(Value.T(_, _)) => throw Smt2Error(s"$name is not Boolean")
        case None =>
          if !macros.contains(name) then Pred(declared(name))
          else
            expand(name, Nil, env) match
              case Value.F(f) => f
              case Value.T(_, _) => throw Smt2Error(s"$name is not Boolean")
    case SList(Atom("not") :: arg :: Nil) => Not(formula(arg, env))
    case SList(Atom("and") :: args) => And(args.map(formula(_, env)))
    case SList(Atom("or") :: args) => Or(args.map(formula(_, env)))
    case SList(Atom("=>") :: args) => args.map(formula(_, env)).reduceRight(Implies(_, _))
    case SList(Atom("xor") :: lhs :: rhs :: Nil) => Not(Iff(formula(lhs, env), formula(rhs, env)))
    case SList(Atom("=") :: args) =>
      sortOf(args.head, env) match
        case "Bool" =>
          val fs = args.map(formula(_, env))
          chain(fs.zip(fs.tail).map(Iff(_, _)))
        case sort =>
          val ts = args.map(term(_, env))
          chain(ts.zip(ts.tail).map(equal(sort, _, _)))
    case SList(Atom("distinct") :: args) =>
      val ts = args.map(term(_, env))
      if isArithmetic(sortOf(args.head, env)) then IntDistinct(ts) else Distinct(ts)
    case SList(Atom(BitVectors.Comparison(op)) :: a :: b :: Nil) =>
      val width = BitVectors.width(sortOf(a, env))
      BitVectors.compare(op, term(a, env), term(b, env), width, this)
    case SList(Atom(op @ ("<=" | "<" | ">=" | ">")) :: args) =>
      val ts = args.map(term(_, env))
      val compare: (Term, Term) => Formula = op match
        case "<=" => Le(_, _)
        case "<" => Lt(_, _)
        case ">=" => Ge(_, _)
        case _ => Gt(_, _)
      chain(ts.zip(ts.tail).map(compare(_, _)))
    case SList(Atom("ite") :: cond :: thn :: els :: Nil) =>
      Ite(formula(cond, env), formula(thn, env), formula(els, env))
    case SList(Atom("let") :: SList(bindings) :: body :: Nil) => formula(body, bind(bindings, env))
    case SList(Atom("!") :: body :: _) => formula(body, env) // an annotated term
    case SList(Atom(name) :: args) if macros.contains(name) =>
      expand(name, args, env) match
        case Value.F(f) => f
        case Value.T(_, _) => throw Smt2Error(s"$name is not Boolean")
    case SList(Atom(name) :: args) => Pred(Term.App(declared(name), args.map(term(_, env))))
    case other => throw Smt2Error(s"not a formula: $other")

  /** Equality of two terms of `sort`. */
  private def equal(sort: String, lhs: Term, rhs: Term): Formula =
    if isArithmetic(sort) then IntEq(lhs, rhs) else Eq(lhs, rhs)

  /** A fresh `Int` constant in `[lo, hi]`, declared and bounded before the
   *  current assertion (for the lowering of bit-vectors).
   */
  def freshInt(prefix: String, lo: BigInt, hi: BigInt): Term =
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
    case Atom(name) if isNumeral(name) => Term.Num(BigInt(name))
    case Atom(name) if name.startsWith("#b") => Term.Num(BigInt(name.drop(2), 2))
    case Atom(name) if name.startsWith("#x") => Term.Num(BigInt(name.drop(2), 16))
    case SList(Atom("_") :: Atom(literal) :: Atom(width) :: Nil) if literal.startsWith("bv") =>
      Term.Num(BigInt(literal.drop(2)).mod(BigInt(2).pow(width.toInt)))
    case SList(Atom(BitVectors.Arithmetic(op)) :: args) =>
      val width = BitVectors.width(sortOf(args.head, env))
      BitVectors.arithmetic(op, args.map(term(_, env)), width, this)
    case SList(SList(Atom("_") :: Atom("extract") :: Atom(i) :: Atom(j) :: Nil) :: a :: Nil) =>
      BitVectors.extract(i.toInt, j.toInt, term(a, env), BitVectors.width(sortOf(a, env)), this)
    case SList(SList(Atom("_") :: Atom("zero_extend") :: Atom(_) :: Nil) :: a :: Nil) => term(a, env)
    case SList(SList(Atom("_") :: Atom("sign_extend") :: Atom(k) :: Nil) :: a :: Nil) =>
      BitVectors.signExtend(k.toInt, term(a, env), BitVectors.width(sortOf(a, env)), this)
    case SList(Atom("concat") :: args) =>
      args.map(a => (term(a, env), BitVectors.width(sortOf(a, env)))).reduceLeft { case ((hi, wh), (lo, wl)) =>
        (Term.linear(List((BigInt(2).pow(wl), hi), (BigInt(1), lo))), wh + wl)
      }._1
    case Atom(name) =>
      env.get(name) match
        case Some(Value.T(t, _)) => t
        case Some(Value.F(_)) => throw Smt2Error(s"$name is Boolean, not a term")
        case None =>
          if !macros.contains(name) then declared(name)
          else
            expand(name, Nil, env) match
              case Value.T(t, _) => t
              case Value.F(_) => throw Smt2Error(s"$name is Boolean, not a term")
    case SList(Atom("-") :: arg :: Nil) => Term.linear(List((-1, term(arg, env))))
    case SList(Atom("-") :: first :: rest) =>
      Term.linear((BigInt(1), term(first, env)) :: rest.map(a => (BigInt(-1), term(a, env))))
    case SList(Atom("+") :: args) => Term.linear(args.map(a => (BigInt(1), term(a, env))))
    case SList(Atom("*") :: args) =>
      // Linear: all factors but at most one are numerals.
      val (numerals, others) = args.map(term(_, env)).partitionMap {
        case Term.Num(v) => Left(v)
        case t => Right(t)
      }
      val k = numerals.product
      others match
        case Nil => Term.Num(k)
        case t :: Nil => Term.linear(List((k, t)))
        case _ => throw Smt2Error(s"non-linear multiplication: $sexpr")
    case SList(Atom("ite") :: cond :: thn :: els :: Nil) =>
      // A fresh constant `k` with `cond => k = thn` and `not cond => k = els`, asserted before the current assertion.
      fresh += 1
      val name = s"ite!$fresh"
      val k = Term.Const(name)
      val sort = sortOf(thn, env)
      introduced += ((name, if isArithmetic(sort) then "Int" else sort)) // declared as it is printed: lowered
      functions(name) = (Nil, sort)
      val c = formula(cond, env)
      definitions += Implies(c, equal(sort, k, term(thn, env)))
      definitions += Implies(Not(c), equal(sort, k, term(els, env)))
      k
    case SList(Atom("let") :: SList(bindings) :: body :: Nil) => term(body, bind(bindings, env))
    case SList(Atom("!") :: body :: _) => term(body, env)
    case SList(Atom(name) :: args) if macros.contains(name) =>
      expand(name, args, env) match
        case Value.T(t, _) => t
        case Value.F(_) => throw Smt2Error(s"$name is Boolean, not a term")
    case SList(Atom(name) :: args) => Term.App(declared(name), args.map(term(_, env)))
    case other => throw Smt2Error(s"not a term: $other")

  /** The conjunction of `conditions`, itself when there is only one. */
  private def chain(conditions: List[Formula]): Formula = conditions match
    case single :: Nil => single
    case _ => And(conditions)

  /** A declared symbol, or an error. */
  private def declared(name: String): Term =
    if functions.contains(name) then Term.Const(name) else throw Smt2Error(s"unknown symbol $name")

  /** Expands the macro `name` applied to `args` (evaluated in `env`). */
  private def expand(name: String, args: List[SExpr], env: Map[String, Value]): Value =
    val (params, sort, body) = macros(name)
    if params.length != args.length then throw Smt2Error(s"$name expects ${params.length} arguments")
    val inner = params.zip(args).foldLeft(env) { case (e, ((p, s), a)) =>
      e + (p -> (if s == "Bool" then Value.F(formula(a, env)) else Value.T(term(a, env), s)))
    }
    if sort == "Bool" then Value.F(formula(body, inner)) else Value.T(term(body, inner), sort)

object Parser:
  def parse(input: String): Script = Parser().parse(input)
