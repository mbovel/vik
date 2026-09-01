package net.bovel.smt2

import scala.collection.mutable.HashMap

import net.bovel.sexpr.{SExpr, SExprParser}
import net.bovel.sexpr.SExpr.{SAtom, SList}

/** Reads an SMT-LIB 2 script (QF_UF, QF_LIA, QF_AX, the bit-vectors and VIK's
 *  lambdas) into a [[Script]], as written: terms of uninterpreted, numeric and
 *  bit-vector sorts become [[Term]]s, Boolean ones [[Formula]]s, and nothing is
 *  rewritten — `let` and `lambda` keep their names, `(= a b c)` stays chained,
 *  `(+ a b c)` n-ary. The solver's `Lowering` compiles the result. The parser's
 *  own work is reading and sorts: sorts of declared and bound symbols decide
 *  which tree a subterm belongs to (`=` on Booleans is equivalence, a Boolean
 *  application a [[Formula.Pred]]) and reject what no theory here speaks
 *  (strings, floats, ...). An unknown symbol is an error.
 */
/** One parse: the class carries the declarations and macros the commands build
 *  up, so [[Parser.parse]] makes a fresh one per input.
 */
private final class Parser:
  import Formula.*

  /** The declared symbols (functions with their whole function sort) and the
   *  macros of `define-fun`, with parameters, result sort and typed body.
   */
  private val functions = HashMap.empty[String, Sort]
  private val macros = HashMap.empty[String, (List[(String, Sort)], Sort, Term | Formula)]

  def parse(input: String): Script = Script(SExprParser.parse(input).flatMap(command))

  private def command(sexpr: SExpr): List[Command] = sexpr match
    case SList(SAtom("set-logic") :: SAtom(logic) :: Nil) => List(Command.SetLogic(logic))
    case SList(SAtom("set-info") :: SAtom(key) :: value :: Nil) => List(Command.SetInfo(key, value.toString))
    case SList(SAtom("declare-sort") :: SAtom(name) :: SAtom(arity) :: Nil) =>
      List(Command.DeclareSort(name, arity.toInt))
    case SList(SAtom("declare-sort") :: SAtom(name) :: Nil) => List(Command.DeclareSort(name, 0))
    case SList(SAtom("declare-fun") :: SAtom(name) :: SList(args) :: s :: Nil) =>
      declare(name, args.map(sort), sort(s))
    case SList(SAtom("declare-const") :: SAtom(name) :: s :: Nil) => declare(name, Nil, sort(s))
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
    case SList(SAtom("define-fun") :: SAtom(name) :: SList(params) :: s :: body :: Nil) =>
      val ps = parameters(params)
      val result = sort(s)
      val env = ps.toMap
      val typed: Term | Formula = if result == Sort.Bool then formula(body, env) else term(body, env)
      macros(name) = (ps, result, typed)
      List(Command.DefineFun(name, ps, result, typed))
    case SList(SAtom("assert") :: SList(SAtom("!") :: body :: attributes) :: Nil)
        if attributes.sliding(2).exists { case Seq(SAtom(":named"), _) => true; case _ => false } =>
      val name = attributes.sliding(2).collectFirst { case Seq(SAtom(":named"), SAtom(n)) => n }.get
      List(Command.AssertNamed(name, formula(body, Map.empty)))
    case SList(SAtom("assert") :: body :: Nil) => List(Command.Assert(formula(body, Map.empty)))
    case SList(SAtom("check-sat") :: Nil) => List(Command.CheckSat)
    case SList(SAtom("check-sat-using") :: _) => List(Command.CheckSat) // z3's, with a tactic: ignored
    case SList(SAtom("check-sat-assuming") :: SList(assumptions) :: Nil) =>
      List(Command.CheckSatAssuming(assumptions.map(formula(_, Map.empty))))
    case SList(SAtom("check-sat") :: assumptions) => // z3's shorthand
      List(Command.CheckSatAssuming(assumptions.map(formula(_, Map.empty))))
    case SList(SAtom("reset-assertions") :: Nil) => List(Command.ResetAssertions)
    case SList(SAtom("reset") :: Nil) =>
      functions.clear()
      macros.clear()
      List(Command.Reset)
    case SList(SAtom("get-unsat-core") :: Nil) => List(Command.GetUnsatCore)
    case SList(SAtom("get-assertions") :: Nil) => List(Command.GetAssertions)
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
          s.toString -> (if sortOf(s, Map.empty) == Sort.Bool then formula(s, Map.empty) else term(s, Map.empty))
        )))
      catch case _: Smt2Error => List(Command.Other(sexpr))
    case SList(SAtom("set-option") :: SAtom(key) :: value :: Nil) => List(Command.SetOption(key, value.toString))
    case SList(SAtom("get-info") :: SAtom(key) :: Nil) => List(Command.GetInfo(key))
    case SList(SAtom("exit") :: Nil) => List(Command.Exit)
    case SList(SAtom(_) :: _) => List(Command.Other(sexpr))
    case other => throw Smt2Error(s"not a command: $other")

  /** Declares `name`, with its sort as written. */
  private def declare(name: String, argSorts: List[Sort], s: Sort): List[Command] =
    (s :: argSorts).foreach(check)
    functions(name) = Sort.fun(argSorts, s)
    List(Command.DeclareFun(name, argSorts, s))

  /** Declares datatypes: their constructors, selectors and testers become
   *  function symbols (a constructor `C` of datatype `D` with selectors
   *  `(s_i S_i)` is `C : S_1 ... S_n -> D`, `s_i : D -> S_i` and the tester
   *  `(_ is C)` : `D -> Bool`).
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
                case SList(SAtom(selector) :: s :: Nil) => (selector, sort(s))
                case other => throw Smt2Error(s"bad selector $other")
              }
            )
          case other => throw Smt2Error(s"bad constructor $other")
        }
      )
    }
    for datatype <- datatypes; constructor <- datatype.constructors do
      val d = Sort.Named(datatype.name)
      functions(constructor.name) = Sort.fun(constructor.selectors.map(_._2), d)
      functions(constructor.tester) = Sort.fun(List(d), Sort.Bool)
      for (selector, s) <- constructor.selectors do functions(selector) = Sort.fun(List(d), s)
    Command.DeclareDatatypes(datatypes)

  private def sort(s: SExpr): Sort = s match
    case SAtom("Bool") => Sort.Bool
    case SAtom("Int") => Sort.Int
    case SAtom("Real") => Sort.Real
    case SAtom(name) => Sort.Named(name)
    case SList(SAtom("_") :: SAtom("BitVec") :: SAtom(width) :: Nil) => Sort.BitVec(width.toInt)
    case SList(SAtom("Array") :: args) if args.length >= 2 =>
      val sorts = args.map(sort)
      sorts.init.foldRight(sorts.last)(Sort.Array(_, _))
    case SList(SAtom("->") :: args) if args.length >= 2 =>
      val sorts = args.map(sort)
      Sort.fun(sorts.init, sorts.last)
    case other => Sort.Named(other.toString) // opaque: rejected by `check` where a symbol is declared

  /** Rejects, at a declaration or a binder, the sorts of theories that are not
   *  spoken here, which would otherwise pass for uninterpreted sorts: strings,
   *  floats, sets, ... (a datatype field is not checked, as before: an unused
   *  one costs nothing).
   */
  private def check(s: Sort): Unit = s match
    case Sort.Named(name) =>
      val unsupported =
        List("String", "RegLan", "RoundingMode", "UnitTuple", "(Set ", "(Bag ", "(Seq ", "(Tuple ", "(_ FloatingPoint")
      if unsupported.exists(u => name == u || name.startsWith(u)) || name.startsWith("Float") then
        throw Smt2Error(s"the sort $name is not supported")
    case Sort.Array(index, element) =>
      check(index)
      check(element)
    case Sort.Fun(args, result) =>
      args.foreach(check)
      check(result)
    case _ => ()

  /** The parameters of a lambda or a macro, `((x S) ...)`, with their sorts.
   */
  private def parameters(params: List[SExpr]): List[(String, Sort)] = params.map {
    case SList(SAtom(p) :: s :: Nil) =>
      val parsed = sort(s)
      check(parsed)
      (p, parsed)
    case other => throw Smt2Error(s"bad parameter $other")
  }

  private def isNumeral(name: String): Boolean = name.nonEmpty && name.forall(_.isDigit)

  private def isDecimal(name: String): Boolean =
    val i = name.indexOf('.')
    i > 0 && i < name.length - 1 && name.take(i).forall(_.isDigit) && name.drop(i + 1).forall(_.isDigit)

  /** Whether terms of `sort` are numbers or bit-vectors: where `=` and
   *  `distinct` mean arithmetic equality after the lowering.
   */
  private def arithmetic(s: Sort): Boolean = s match
    case Sort.Int | Sort.Real | Sort.BitVec(_) => true
    case _ => false

  /** The sort of an expression: what decides between the two trees. `env` binds
   *  the `let`- and `lambda`-bound names in scope.
   */
  private def sortOf(sexpr: SExpr, env: Map[String, Sort]): Sort = sexpr match
    case SAtom("true" | "false") => Sort.Bool
    case SAtom(name) if isNumeral(name) => Sort.Int
    case SAtom(name) if isDecimal(name) => Sort.Real
    case SAtom(name) if name.startsWith("#b") => Sort.BitVec(name.length - 2)
    case SAtom(name) if name.startsWith("#x") => Sort.BitVec(4 * (name.length - 2))
    case SList(SAtom("_") :: SAtom(literal) :: SAtom(width) :: Nil) if literal.startsWith("bv") =>
      Sort.BitVec(width.toInt)
    case SAtom(name) =>
      env.get(name).orElse(functions.get(name)).orElse(macros.get(name).map(_._2)).getOrElse(
        throw Smt2Error(s"unknown symbol $name")
      )
    case SList(SAtom("bvcomp") :: _) => Sort.BitVec(1)
    case SList(SAtom(op) :: a :: _) if BvOp.named.contains(op) && op != "concat" => sortOf(a, env)
    case SList(SAtom("concat") :: args) =>
      Sort.BitVec(args.map(a => width(sortOf(a, env))).sum)
    case SList(SAtom(op) :: _) if BvCmpOp.named.contains(op) => Sort.Bool
    case SList(SList(SAtom("_") :: SAtom("extract") :: SAtom(i) :: SAtom(j) :: Nil) :: _ :: Nil) =>
      Sort.BitVec(i.toInt - j.toInt + 1)
    case SList(SList(SAtom("_") :: SAtom("zero_extend" | "sign_extend") :: SAtom(k) :: Nil) :: a :: Nil) =>
      Sort.BitVec(width(sortOf(a, env)) + k.toInt)
    case SList(SList(SAtom("_") :: SAtom("repeat") :: SAtom(k) :: Nil) :: a :: Nil) =>
      Sort.BitVec(width(sortOf(a, env)) * k.toInt)
    case SList(SList(SAtom("_") :: SAtom("rotate_left" | "rotate_right") :: SAtom(_) :: Nil) :: a :: Nil) =>
      sortOf(a, env)
    case SList(SAtom("not" | "and" | "or" | "=>" | "xor" | "=" | "distinct" | "<=" | "<" | ">=" | ">") :: _) =>
      Sort.Bool
    case SList(SAtom("/") :: _) => Sort.Real
    case SList(SAtom("div" | "mod" | "abs") :: _) => Sort.Int
    case SList(SAtom("select") :: a :: _ :: Nil) =>
      sortOf(a, env) match
        case Sort.Array(_, element) => element
        case s => applied(s, 1)
    case SList(SAtom("store") :: a :: _ :: _ :: Nil) => sortOf(a, env)
    case SList(SAtom("+" | "-" | "*") :: args) =>
      if args.exists(a => sortOf(a, env) == Sort.Real) then Sort.Real else Sort.Int
    case SList(SAtom("lambda") :: SList(params) :: body :: Nil) =>
      val ps = parameters(params)
      Sort.fun(ps.map(_._2), sortOf(body, env ++ ps))
    case SList(SList(SAtom("_") :: SAtom("is") :: SAtom(_) :: Nil) :: _ :: Nil) => Sort.Bool
    case SList(SAtom("ite") :: _ :: thn :: _ :: Nil) => sortOf(thn, env)
    case SList(SAtom("let") :: SList(bindings) :: body :: Nil) =>
      sortOf(body, env ++ bindings.map { case SList(SAtom(name) :: value :: Nil) => name -> sortOf(value, env) })
    case SList(SAtom(name) :: args) if macros.contains(name) => macros(name)._2
    case SList(SAtom(name) :: args) => applied(sortOf(SAtom(name), env), args.length)
    case SList((fun @ SList(_)) :: args) => applied(sortOf(fun, env), args.length)
    case other => throw Smt2Error(s"cannot type $other")

  private def width(s: Sort): Int = s match
    case Sort.BitVec(w) => w
    case _ => throw Smt2Error(s"a bit-vector was expected, not $s")

  private def applied(s: Sort, n: Int): Sort =
    s.applied(n).getOrElse(throw Smt2Error(s"$s applied to $n arguments"))

  private def formula(sexpr: SExpr, env: Map[String, Sort]): Formula = sexpr match
    case SAtom("true") => True
    case SAtom("false") => False
    case SList(SAtom(q @ ("forall" | "exists")) :: _) => throw Smt2Error(s"$q: quantifiers are not supported")
    case SList(SAtom("not") :: arg :: Nil) => Not(formula(arg, env))
    case SList(SAtom("and") :: args) => And(args.map(formula(_, env)))
    case SList(SAtom("or") :: args) => Or(args.map(formula(_, env)))
    case SList(SAtom("=>") :: args) if args.length >= 2 => Implies(args.map(formula(_, env)))
    case SList(SAtom("xor") :: args) if args.length >= 2 => Xor(args.map(formula(_, env)))
    case SList(SAtom("=") :: args) if args.length >= 2 =>
      if sortOf(args.head, env) == Sort.Bool then Iff(args.map(formula(_, env)))
      else Eq(args.map(term(_, env)))
    case SList(SAtom("distinct") :: args) => Distinct(args.map(term(_, env)))
    case SList(SAtom(op) :: a :: b :: Nil) if BvCmpOp.named.contains(op) =>
      BvCmp(BvCmpOp.named(op), term(a, env), term(b, env))
    case SList(SList(SAtom("_") :: SAtom("is") :: SAtom(constructor) :: Nil) :: arg :: Nil) =>
      declared(s"(_ is $constructor)", env)
      IsConstructor(constructor, term(arg, env))
    case SList(SAtom(op @ ("<=" | "<" | ">=" | ">")) :: args) if args.length >= 2 =>
      for a <- args if !arithmetic(sortOf(a, env)) do
        throw Smt2Error(s"$op on ${sortOf(a, env)} is not supported")
      Cmp(CmpOp.values.find(_.name == op).get, args.map(term(_, env)))
    case SList(SAtom("ite") :: cond :: thn :: els :: Nil) =>
      Ite(formula(cond, env), formula(thn, env), formula(els, env))
    case SList(SAtom("let") :: SList(bs) :: body :: Nil) =>
      val (bound, inner) = bindings(bs, env)
      Let(bound, formula(body, inner))
    case SList(SAtom("!") :: body :: _) => formula(body, env) // an annotated term
    case _ => // a Boolean constant or application
      if sortOf(sexpr, env) != Sort.Bool then throw Smt2Error(s"not a formula: $sexpr")
      Formula.Pred(term(sexpr, env))
  end formula

  /** The bindings of a `let`, each typed by its own sort, and the environment
   *  for the body.
   */
  private def bindings(bs: List[SExpr], env: Map[String, Sort]): (List[(String, Term | Formula)], Map[String, Sort]) =
    val bound = bs.map {
      case SList(SAtom(name) :: value :: Nil) =>
        val s = sortOf(value, env)
        val v: Term | Formula = if s == Sort.Bool then formula(value, env) else term(value, env)
        (name, s, v)
      case other => throw Smt2Error(s"bad binding $other")
    }
    (bound.map((name, _, v) => (name, v)), env ++ bound.map((name, s, _) => name -> s))

  private def term(sexpr: SExpr, env: Map[String, Sort]): Term = sexpr match
    case SAtom("true") => Term.Bool(True)
    case SAtom("false") => Term.Bool(False)
    case SList(SAtom(q @ ("forall" | "exists")) :: _) => throw Smt2Error(s"$q: quantifiers are not supported")
    // A formula-shaped tree where a term is expected (an argument, a field, a lambda body).
    case SList(SAtom("not" | "and" | "or" | "=>" | "xor" | "distinct" | "<=" | "<" | ">=" | ">") :: _) =>
      Term.Bool(formula(sexpr, env))
    case SList(SAtom("=") :: _ :: _ :: _) => Term.Bool(formula(sexpr, env))
    case SList(SAtom(op) :: _ :: _ :: Nil) if BvCmpOp.named.contains(op) => Term.Bool(formula(sexpr, env))
    case SList(SList(SAtom("_") :: SAtom("is") :: SAtom(_) :: Nil) :: _ :: Nil) => Term.Bool(formula(sexpr, env))
    case SAtom(name) if isNumeral(name) => Term.Numeral(BigInt(name))
    case SAtom(name) if isDecimal(name) =>
      val i = name.indexOf('.')
      Term.Decimal(BigInt(name.take(i) + name.drop(i + 1)), name.length - i - 1)
    case SAtom(name) if name.startsWith("#b") => Term.Bits(BigInt(name.drop(2), 2), name.length - 2)
    case SAtom(name) if name.startsWith("#x") => Term.Bits(BigInt(name.drop(2), 16), 4 * (name.length - 2))
    case SList(SAtom("_") :: SAtom(literal) :: SAtom(width) :: Nil) if literal.startsWith("bv") =>
      Term.Bits(BigInt(literal.drop(2)).mod(BigInt(2).pow(width.toInt)), width.toInt)
    case SAtom(name) =>
      declared(name, env)
      macros.get(name).foreach((params, _, _) =>
        if params.nonEmpty then throw Smt2Error(s"$name expects ${params.length} arguments")
      )
      Term.Const(name)
    case SList(SAtom(op) :: args) if BvOp.named.contains(op) => Term.BvApp(BvOp.named(op), args.map(term(_, env)))
    case SList(SList(SAtom("_") :: SAtom(op) :: indices) :: args)
        if BvOp.indexed(op, indices.collect { case SAtom(i) if isNumeral(i) => i.toInt }).isDefined =>
      val parsed = BvOp.indexed(op, indices.collect { case SAtom(i) if isNumeral(i) => i.toInt }).get
      Term.BvApp(parsed, args.map(term(_, env)))
    case SList(SAtom("select") :: a :: i :: Nil) if selectable(a, env) =>
      Term.Select(term(a, env), term(i, env))
    case SList(SAtom("store") :: a :: i :: v :: Nil) if selectable(a, env) =>
      Term.Store(term(a, env), term(i, env), term(v, env))
    case SList(SAtom("-") :: arg :: Nil) => Term.ArithApp(ArithOp.Neg, List(term(arg, env)))
    case SList(SAtom("-") :: args) if args.length >= 2 => Term.ArithApp(ArithOp.Sub, args.map(term(_, env)))
    case SList(SAtom("+") :: args) => Term.ArithApp(ArithOp.Add, args.map(term(_, env)))
    case SList(SAtom("*") :: args) => Term.ArithApp(ArithOp.Mul, args.map(term(_, env)))
    case SList(SAtom("/") :: args) if args.length >= 2 => Term.ArithApp(ArithOp.Div, args.map(term(_, env)))
    case SList(SAtom("div") :: args) if args.length >= 2 => Term.ArithApp(ArithOp.IntDiv, args.map(term(_, env)))
    case SList(SAtom("mod") :: a :: b :: Nil) => Term.ArithApp(ArithOp.Mod, List(term(a, env), term(b, env)))
    case SList(SAtom("abs") :: a :: Nil) => Term.ArithApp(ArithOp.Abs, List(term(a, env)))
    case SList(SAtom("lambda") :: SList(params) :: body :: Nil) =>
      val ps = parameters(params)
      Term.Lambda(ps, term(body, env ++ ps))
    case SList(SAtom("ite") :: cond :: thn :: els :: Nil) =>
      Term.Ite(formula(cond, env), term(thn, env), term(els, env))
    case SList(SAtom("let") :: SList(bs) :: body :: Nil) =>
      val (bound, inner) = bindings(bs, env)
      Term.Let(bound, term(body, inner))
    case SList(SAtom("!") :: body :: _) => term(body, env) // an annotated term
    case SList(SAtom(name) :: args) =>
      declared(name, env)
      Term.App(Term.Const(name), args.map(term(_, env)))
    case SList((fun @ SList(_)) :: args) => Term.App(term(fun, env), args.map(term(_, env)))
    case other => throw Smt2Error(s"not a term: $other")
  end term

  /** Whether `a` is an array or a function: what `select` and `store` accept
   *  (an array is a function to the solver).
   */
  private def selectable(a: SExpr, env: Map[String, Sort]): Boolean = sortOf(a, env) match
    case Sort.Array(_, _) | Sort.Fun(_, _) => true
    case _ => false

  /** Checks that `name` is declared, bound or a macro. */
  private def declared(name: String, env: Map[String, Sort]): Unit =
    if !env.contains(name) && !functions.contains(name) && !macros.contains(name) then
      throw Smt2Error(s"unknown symbol $name")

object Parser:
  def parse(input: String): Script = Parser().parse(input)
