package net.bovel.smt2

import net.bovel.smt.{Formula, Term}

/** Prints scripts, formulas and terms back to SMT-LIB 2 text (one command per
 *  line), e.g. to hand a parsed script to another solver.
 */
object Printer:
  /** The number of parameters of the lambdas being printed. */
  private var lambdaDepth = 0

  def print(script: Script): String = script.commands.map(print).mkString("", "\n", "\n")

  def print(command: Command): String = command match
    case Command.SetLogic(logic) => s"(set-logic $logic)"
    case Command.SetInfo(key, value) => s"(set-info $key $value)"
    case Command.DeclareSort(name, arity) => s"(declare-sort $name $arity)"
    case Command.DeclareFun(name, argSorts, sort) => s"(declare-fun $name (${argSorts.mkString(" ")}) $sort)"
    case Command.DeclareDatatypes(datatypes) =>
      val sorts = datatypes.map(d => s"(${d.name} 0)").mkString(" ")
      val declarations = datatypes.map { d =>
        d.constructors.map { c =>
          if c.selectors.isEmpty then s"(${c.name})"
          else s"(${c.name} ${c.selectors.map((s, sort) => s"($s $sort)").mkString(" ")})"
        }.mkString("(", " ", ")")
      }.mkString(" ")
      s"(declare-datatypes ($sorts) ($declarations))"
    case Command.DefineFun(name, params, sort, body) =>
      s"(define-fun $name (${params.map((p, s) => s"($p $s)").mkString(" ")}) $sort $body)"
    case Command.Assert(formula) => s"(assert ${print(formula)})"
    case Command.CheckSat => "(check-sat)"
    case Command.CheckSatAssuming(assumptions) => assumptions.map(print).mkString("(check-sat-assuming (", " ", "))")
    case Command.ResetAssertions => "(reset-assertions)"
    case Command.Reset => "(reset)"
    case Command.Push(n) => s"(push $n)"
    case Command.Pop(n) => s"(pop $n)"
    case Command.GetModel => "(get-model)"
    case Command.Exit => "(exit)"
    case Command.SetOption(key, value) => s"(set-option $key $value)"
    case Command.GetInfo(key) => s"(get-info $key)"
    case Command.Other(sexpr) => sexpr.toString

  def print(formula: Formula): String = formula match
    case Formula.True => "true"
    case Formula.False => "false"
    case Formula.Not(arg) => s"(not ${print(arg)})"
    case Formula.And(Nil) => "true"
    case Formula.And(args) => args.map(print).mkString("(and ", " ", ")")
    case Formula.Or(Nil) => "false"
    case Formula.Or(args) => args.map(print).mkString("(or ", " ", ")")
    case Formula.Implies(lhs, rhs) => s"(=> ${print(lhs)} ${print(rhs)})"
    case Formula.Iff(lhs, rhs) => s"(= ${print(lhs)} ${print(rhs)})"
    case Formula.Ite(cond, thn, els) => s"(ite ${print(cond)} ${print(thn)} ${print(els)})"
    case Formula.Eq(lhs, rhs) => s"(= ${print(lhs)} ${print(rhs)})"
    case Formula.Pred(term) => print(term)
    case Formula.Le(lhs, rhs) => s"(<= ${print(lhs)} ${print(rhs)})"

  def print(term: Term): String = term match
    case Term.Const(name) => name
    case Term.App(fun, args) => args.map(print).mkString(s"(${print(fun)} ", " ", ")")
    case Term.Num(value) => if value.signum < 0 then s"(- ${-value})" else value.toString
    case Term.Lambda(sorts, body) =>
      // Parameters named by their de Bruijn level, which is unique across the nesting.
      val names = sorts.indices.map(i => s"%${lambdaDepth + i}")
      lambdaDepth += sorts.length
      val printed = try print(body)
      finally lambdaDepth -= sorts.length
      s"(lambda (${names.zip(sorts).map((n, s) => s"($n $s)").mkString(" ")}) $printed)"
    case Term.Var(index) => s"%${lambdaDepth - 1 - index}"
    case Term.Bool(formula) => print(formula)
    case Term.Linear(terms, constant) =>
      val summands = terms.map((a, t) => if a == 1 then print(t) else s"(* ${print(Term.Num(a))} ${print(t)})") ++
        Option.when(constant.signum != 0)(print(Term.Num(constant)))
      summands match
        case single :: Nil => single
        case _ => summands.mkString("(+ ", " ", ")")
