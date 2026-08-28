package net.bovel.smt2

import scala.collection.mutable.{ArrayBuffer, HashMap}

import net.bovel.sexpr.{SExpr, SExprParser}
import net.bovel.sexpr.SExpr.{Atom, SList}
import net.bovel.smt.{Formula, Term}

/** Reads an SMT-LIB 2 script (QF_UF) into a [[Script]], translating terms of
 *  uninterpreted sorts into [[Term]]s and Boolean terms into [[Formula]]s.
 *  Sorts of declared symbols decide which is which: `=` on Booleans is `Iff`, a
 *  Boolean-sorted constant or application is a `Pred`. `let` binds names to
 *  translated values; `define-fun` is a macro; `distinct` is expanded to
 *  pairwise disequalities; `ite` on terms is lowered to a fresh constant with
 *  two defining assertions placed before the assertion that uses it.
 */
final class Parser:
  import Formula.*

  private enum Value:
    case T(term: Term)
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
    case SList(Atom("set-logic") :: Atom(logic) :: Nil) => List(Command.SetLogic(logic))
    case SList(Atom("set-info") :: Atom(key) :: value :: Nil) => List(Command.SetInfo(key, value.toString))
    case SList(Atom("declare-sort") :: Atom(name) :: Atom(arity) :: Nil) => List(Command.DeclareSort(name, arity.toInt))
    case SList(Atom("declare-sort") :: Atom(name) :: Nil) => List(Command.DeclareSort(name, 0))
    case SList(Atom("declare-fun") :: Atom(name) :: SList(args) :: Atom(sort) :: Nil) =>
      functions(name) = (args.map(_.toString), sort)
      List(Command.DeclareFun(name, args.map(_.toString), sort))
    case SList(Atom("declare-const") :: Atom(name) :: Atom(sort) :: Nil) =>
      functions(name) = (Nil, sort)
      List(Command.DeclareFun(name, Nil, sort))
    case SList(Atom("define-fun") :: Atom(name) :: SList(params) :: Atom(sort) :: body :: Nil) =>
      val ps = params.map {
        case SList(Atom(p) :: Atom(s) :: Nil) => (p, s)
        case other => throw Smt2Error(s"bad parameter $other")
      }
      macros(name) = (ps, sort, body)
      List(Command.DefineFun(name, ps, sort, body))
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

  private def sortOf(sexpr: SExpr, env: Map[String, Value]): String = sexpr match
    case Atom("true" | "false") => "Bool"
    case Atom(name) =>
      env.get(name) match
        case Some(Value.F(_)) => "Bool"
        case Some(Value.T(_)) => "?"
        case None => functions.get(name).map(_._2).orElse(macros.get(name).map(_._2)).getOrElse(throw Smt2Error(
            s"unknown symbol $name"
          ))
    case SList(Atom("not" | "and" | "or" | "=>" | "xor" | "=" | "distinct") :: _) => "Bool"
    case SList(Atom("ite") :: _ :: thn :: _ :: Nil) => sortOf(thn, env)
    case SList(Atom("let") :: SList(bindings) :: body :: Nil) => sortOf(body, bind(bindings, env))
    case SList(Atom(name) :: _) => functions.get(
        name
      ).map(_._2).orElse(macros.get(name).map(_._2)).getOrElse(throw Smt2Error(s"unknown function $name"))
    case other => throw Smt2Error(s"cannot type $other")

  private def bind(bindings: List[SExpr], env: Map[String, Value]): Map[String, Value] =
    env ++ bindings.map {
      case SList(Atom(name) :: value :: Nil) => name ->
          (if sortOf(value, env) == "Bool" then Value.F(formula(value, env)) else Value.T(term(value, env)))
      case other => throw Smt2Error(s"bad binding $other")
    }

  private def formula(sexpr: SExpr, env: Map[String, Value]): Formula = sexpr match
    case Atom("true") => True
    case Atom("false") => False
    case Atom(name) =>
      env.get(name) match
        case Some(Value.F(f)) => f
        case Some(Value.T(t)) => throw Smt2Error(s"$name is not Boolean")
        case None =>
          if !macros.contains(name) then Pred(declared(name))
          else
            expand(name, Nil, env) match
              case Value.F(f) => f
              case Value.T(_) => throw Smt2Error(s"$name is not Boolean")
    case SList(Atom("not") :: arg :: Nil) => Not(formula(arg, env))
    case SList(Atom("and") :: args) => And(args.map(formula(_, env)))
    case SList(Atom("or") :: args) => Or(args.map(formula(_, env)))
    case SList(Atom("=>") :: args) => args.map(formula(_, env)).reduceRight(Implies(_, _))
    case SList(Atom("xor") :: lhs :: rhs :: Nil) => Not(Iff(formula(lhs, env), formula(rhs, env)))
    case SList(Atom("=") :: args) =>
      if sortOf(args.head, env) == "Bool" then
        val fs = args.map(formula(_, env))
        chain(fs.zip(fs.tail).map(Iff(_, _)))
      else
        val ts = args.map(term(_, env))
        chain(ts.zip(ts.tail).map(Eq(_, _)))
    case SList(Atom("distinct") :: args) => Distinct(args.map(term(_, env)))
    case SList(Atom("ite") :: cond :: thn :: els :: Nil) =>
      Ite(formula(cond, env), formula(thn, env), formula(els, env))
    case SList(Atom("let") :: SList(bindings) :: body :: Nil) => formula(body, bind(bindings, env))
    case SList(Atom("!") :: body :: _) => formula(body, env) // an annotated term
    case SList(Atom(name) :: args) if macros.contains(name) =>
      expand(name, args, env) match
        case Value.F(f) => f
        case Value.T(_) => throw Smt2Error(s"$name is not Boolean")
    case SList(Atom(name) :: args) => Pred(Term.App(declared(name), args.map(term(_, env))))
    case other => throw Smt2Error(s"not a formula: $other")

  private def term(sexpr: SExpr, env: Map[String, Value]): Term = sexpr match
    case Atom(name) =>
      env.get(name) match
        case Some(Value.T(t)) => t
        case Some(Value.F(_)) => throw Smt2Error(s"$name is Boolean, not a term")
        case None =>
          if !macros.contains(name) then declared(name)
          else
            expand(name, Nil, env) match
              case Value.T(t) => t
              case Value.F(_) => throw Smt2Error(s"$name is Boolean, not a term")
    case SList(Atom("ite") :: cond :: thn :: els :: Nil) =>
      // A fresh constant `k` with `cond => k = thn` and `not cond => k = els`, asserted before the current assertion.
      fresh += 1
      val name = s"ite!$fresh"
      val k = Term.Const(name)
      introduced += ((name, sortOf(thn, env)))
      functions(name) = (Nil, sortOf(thn, env))
      val c = formula(cond, env)
      definitions += Implies(c, Eq(k, term(thn, env)))
      definitions += Implies(Not(c), Eq(k, term(els, env)))
      k
    case SList(Atom("let") :: SList(bindings) :: body :: Nil) => term(body, bind(bindings, env))
    case SList(Atom("!") :: body :: _) => term(body, env)
    case SList(Atom(name) :: args) if macros.contains(name) =>
      expand(name, args, env) match
        case Value.T(t) => t
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
      e + (p -> (if s == "Bool" then Value.F(formula(a, env)) else Value.T(term(a, env))))
    }
    if sort == "Bool" then Value.F(formula(body, inner)) else Value.T(term(body, inner))

object Parser:
  def parse(input: String): Script = Parser().parse(input)
