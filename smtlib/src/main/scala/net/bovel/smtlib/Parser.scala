package net.bovel.smtlib

import scala.collection.mutable.HashMap

import net.bovel.sexpr.{SExpr, SExprParser}
import net.bovel.sexpr.SExpr.{SAtom, SList}

/** Reads an SMT-LIB 2 script (QF_UF, QF_LIA, QF_AX, the bit-vectors and VIK's
 *  into a [[Script]], as written: terms of uninterpreted, numeric and
 *  bit-vector sorts become [[Term]]s, Boolean ones [[Formula]]s, and nothing is
 *  rewritten — `let` keeps its names, `(= a b c)` stays chained, `(+ a b c)`
 *  n-ary. The solver's `Lowering` compiles the result. The parser's own work is
 *  reading and sorts: sorts of declared and bound symbols decide which tree a
 *  subterm belongs to (`=` on Booleans is equivalence, a Boolean application a
 *  [[Formula.Pred]]) and reject what no theory here speaks (strings, floats,
 *  ...). An unknown symbol is an error.
 */
/** One parser: it carries the declarations and macros the commands build up, so
 *  the next `parse` reads its input in their scope ([[Parser.parse]] makes a
 *  fresh one per input; a `vik.Session` keeps one).
 */
final class Parser:
  import Formula.*

  /** The declared symbols (functions with their whole function sort) and the
   *  macros of `define-fun`, with parameters, result sort and typed body.
   */
  private val functions = HashMap.empty[String, Sort]
  private val macros = HashMap.empty[String, (List[(String, Sort)], Sort, Term | Formula)]
  private val constructors = HashMap.empty[String, Constructor] // by name, for the patterns of `match`
  private var matches = 0 // the `match` scrutinees bound so far, for their names

  /** Polymorphism, compiled away here: a sort parameter
   *  (`declare-sort-parameter`, or the `par` of a datatype) makes a rank
   *  polymorphic, and every use of the symbol at a monomorphic instance — the
   *  parameters bound by unification with the arguments' sorts, or named with
   *  `(as f τ)` — is a use of a monomorphic symbol `f@τ1@…`, declared on demand
   *  ([[instance]]) ahead of the command that needed it. A parametric datatype
   *  is instantiated the same way, sort by sort: `(List Int)` is a datatype
   *  named as written, with constructors `nil@Int` and `cons@Int`
   *  ([[instantiate]]). A use whose parameters the arguments do not determine
   *  must say `(as f τ)`; an assertion cannot stand for every sort.
   */
  private enum Poly:
    /** A declared function, constructor or selector: parameters, argument sorts
     *  and result, as written.
     */
    case Fun(params: List[String], args: List[SExpr], result: SExpr)

    /** A `define-fun` whose rank has parameters: its formals, result and body
     *  are read again per instance.
     */
    case Macro(params: List[String], formals: List[(String, SExpr)], result: SExpr, body: SExpr)
  private val sortParameters = scala.collection.mutable.LinkedHashSet.empty[String]
  private val declaredSorts = scala.collection.mutable.HashSet.empty[String] // `declare-sort`, of any arity
  private val sortDefinitions = HashMap.empty[String, (List[String], SExpr)] // define-sort
  private val polymorphic = HashMap.empty[String, Poly]
  private val templates = HashMap.empty[String, (List[String], List[SExpr])] // parametric datatypes
  private val owner = HashMap.empty[String, String] // a parametric constructor or selector to its datatype
  private val instantiated = scala.collection.mutable.HashSet.empty[String] // datatype instances, by sort text
  private val pending = scala.collection.mutable.ArrayBuffer.empty[Command] // instances' declarations
  private var sortSubst: Map[String, SExpr] = Map.empty // a template's parameters, while instantiating

  def parse(input: String): Script = Script(SExprParser.parse(input).flatMap { s =>
    val commands = command(s)
    val out = pending.toList ++ commands
    pending.clear()
    out
  })

  private def command(sexpr: SExpr): List[Command] = sexpr match
    case SList(SAtom("set-logic") :: SAtom(logic) :: Nil) => List(Command.SetLogic(logic))
    case SList(SAtom("set-info") :: SAtom(key) :: value :: Nil) => List(Command.SetInfo(key, value.toString))
    case SList(SAtom("declare-sort") :: SAtom(name) :: SAtom(arity) :: Nil) =>
      declaredSorts += name
      List(Command.DeclareSort(name, arity.toInt))
    case SList(SAtom("declare-sort") :: SAtom(name) :: Nil) =>
      declaredSorts += name
      List(Command.DeclareSort(name, 0))
    case SList(SAtom("declare-sort-parameter") :: SAtom(name) :: Nil) =>
      sortParameters += name
      Nil
    case SList(SAtom("define-sort") :: SAtom(name) :: SList(params) :: body :: Nil) =>
      sortDefinitions(name) =
        (params.map { case SAtom(p) => p; case other => throw Smt2Error(s"bad parameter $other") }, body)
      Nil
    case SList(SAtom("declare-fun") :: SAtom(name) :: SList(args) :: s :: Nil) => declare(name, args, s)
    case SList(SAtom("declare-const") :: SAtom(name) :: s :: Nil) => declare(name, Nil, s)
    case SList(SAtom("declare-datatype") :: SAtom(name) ::
          SList(SAtom("par") :: SList(params) :: SList(constructors) :: Nil) :: Nil) =>
      template(name, params, constructors)
      Nil
    case SList(SAtom("declare-datatype") :: SAtom(name) :: SList(constructors) :: Nil) =>
      List(declareDatatypes(List((name, constructors))))
    case SList(SAtom("declare-datatypes") :: SList(names) :: SList(declarations) :: Nil)
        if names.exists { case SList(_ :: SAtom("0") :: Nil) => false; case _ => true } =>
      // Parametric: each declaration is `(par (params) (constructors))`, a template instantiated on use.
      for (n, d) <- names.zip(declarations) do
        (n, d) match
          case (
                SList(SAtom(name) :: SAtom(_) :: Nil),
                SList(SAtom("par") :: SList(params) :: SList(constructors) :: Nil)
              ) =>
            template(name, params, constructors)
          case (SList(SAtom(name) :: SAtom("0") :: Nil), SList(constructors)) => // a monomorphic member of the group
            command(SList(List(SAtom("declare-datatype"), SAtom(name), SList(constructors)))).foreach(pending += _)
          case other => throw Smt2Error(s"bad datatype declaration $other")
      Nil
    case SList(SAtom("declare-datatypes") :: SList(names) :: SList(declarations) :: Nil) =>
      val sorts = names.map {
        case SList(SAtom(name) :: SAtom("0") :: Nil) => name
        case other => throw Smt2Error(s"bad datatype declaration $other")
      }
      if sorts.length != declarations.length then
        throw Smt2Error("declare-datatypes: as many declarations as sorts expected")
      List(declareDatatypes(sorts.zip(declarations.map {
        case SList(constructors) => constructors
        case other => throw Smt2Error(s"bad constructor list $other")
      })))
    case SList(SAtom("define-fun") :: SAtom(name) :: SList(params) :: s :: body :: Nil)
        if sortParameters.exists(p => mentions(s, p) || params.exists(mentions(_, p))) =>
      val formals = params.map {
        case SList(SAtom(p) :: ps :: Nil) => (p, ps)
        case other => throw Smt2Error(s"bad parameter $other")
      }
      val ps = sortParameters.filter(p => mentions(s, p) || formals.exists((_, fs) => mentions(fs, p))).toList
      polymorphic(name) = Poly.Macro(ps, formals, s, body)
      Nil
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
      constructors.clear()
      declaredSorts.clear()
      sortParameters.clear()
      sortDefinitions.clear()
      polymorphic.clear()
      templates.clear()
      owner.clear()
      instantiated.clear()
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
          s.toString ->
            (s match
              case SAtom(name) if functions.get(name).flatMap(_.arrow).exists(_._1.nonEmpty) =>
                Term.Const(name) // a function symbol: its graph in the model
              case _ => if sortOf(s, Map.empty) == Sort.Bool then formula(s, Map.empty) else term(s, Map.empty))
        )))
      catch case _: Smt2Error => List(Command.Other(sexpr))
    case SList(SAtom("set-option") :: SAtom(key) :: value :: Nil) => List(Command.SetOption(key, value.toString))
    case SList(SAtom("get-info") :: SAtom(key) :: Nil) => List(Command.GetInfo(key))
    case SList(SAtom("exit") :: Nil) => List(Command.Exit)
    case SList(SAtom(_) :: _) => List(Command.Other(sexpr))
    case other => throw Smt2Error(s"not a command: $other")

  /** Declares `name`, with its sort as written; a rank with sort parameters
   *  declares nothing yet (see [[Poly]]).
   */
  private def declare(name: String, args: List[SExpr], s: SExpr): List[Command] =
    val params = sortParameters.filter(p => (s :: args).exists(mentions(_, p))).toList
    if params.nonEmpty then
      polymorphic(name) = Poly.Fun(params, args, s)
      Nil
    else
      val (argSorts, result) = (args.map(sort), sort(s))
      (result :: argSorts).foreach(check)
      functions(name) = Sort.fun(argSorts, result)
      List(Command.DeclareFun(name, argSorts, result))

  /** Whether the symbol `p` occurs in a sort as written. */
  private def mentions(s: SExpr, p: String): Boolean = s match
    case SAtom(name) => name == p
    case SList(items) => items.exists(mentions(_, p))

  private def substitute(s: SExpr, subst: Map[String, SExpr]): SExpr = s match
    case SAtom(name) => subst.getOrElse(name, s)
    case SList(items) => SList(items.map(substitute(_, subst)))

  /** Binds the parameters of `pattern` so that it reads as `actual` (a
   *  monomorphic sort), on top of `subst`; `None` when the two disagree.
   */
  private def unify(
      pattern: SExpr,
      actual: SExpr,
      params: Set[String],
      subst: Map[String, SExpr]
  ): Option[Map[String, SExpr]] =
    (pattern, actual) match
      case (SAtom(p), _) if params(p) =>
        subst.get(p) match
          case Some(bound) => if bound == actual then Some(subst) else None
          case None => Some(subst + (p -> actual))
      case (SAtom(a), SAtom(b)) => if a == b then Some(subst) else None
      case (SList(ps), SList(as)) if ps.length == as.length =>
        ps.zip(as).foldLeft(Option(subst)) { case (acc, (p, a)) => acc.flatMap(unify(p, a, params, _)) }
      case _ => None

  private def sexprOf(s: Sort): SExpr = SExprParser.parse(s.text).head

  /** The name of a monomorphic symbol: `name` itself, or the instance of a
   *  polymorphic one at the arguments' sorts (and the sort `expected` of the
   *  result, from `(as name τ)`), declared here if new.
   */
  private def instance(name: String, argSorts: List[Sort], expected: Option[Sort]): String =
    polymorphic.get(name) match
      case None => name
      case Some(poly) =>
        val (params, args, result) = poly match
          case Poly.Fun(ps, as, r) => (ps, as, r)
          case Poly.Macro(ps, formals, r, _) => (ps, formals.map(_._2), r)
        if args.length != argSorts.length then throw Smt2Error(s"$name expects ${args.length} arguments")
        val pairs = args.zip(argSorts.map(sexprOf)) ++ expected.map(e => (result, sexprOf(e))).toList
        val subst = pairs.foldLeft(Option(Map.empty[String, SExpr])) { case (acc, (p, a)) =>
          acc.flatMap(unify(p, a, params.toSet, _))
        }.getOrElse(throw Smt2Error(
          s"$name is not applicable to ${argSorts.mkString(" ")}${expected.map(" as " + _).getOrElse("")}"
        ))
        val actual = params.map(p =>
          subst.getOrElse(
            p,
            throw Smt2Error(s"$name: the sort parameter $p is not determined here, write (as $name τ)")
          )
        )
        val actualSorts = actual.map(sort)
        val instanceName = mangle(name, actualSorts)
        owner.get(name) match
          case Some(datatype) =>
            instantiate(datatype, actualSorts) // declares the instance's constructors and selectors
          case None =>
            if !functions.contains(instanceName) && !macros.contains(instanceName) then
              val saved = sortSubst
              sortSubst = params.zip(actual).toMap
              try
                poly match
                  case Poly.Fun(_, as, r) =>
                    val (argSorts, resultSort) = (as.map(sort), sort(r))
                    (resultSort :: argSorts).foreach(check)
                    functions(instanceName) = Sort.fun(argSorts, resultSort)
                    pending += Command.DeclareFun(instanceName, argSorts, resultSort)
                  case Poly.Macro(_, formals, r, body) =>
                    val ps = formals.map((n, s) => n -> sort(s))
                    val resultSort = sort(r)
                    val env = ps.toMap
                    val typed: Term | Formula = if resultSort == Sort.Bool then formula(body, env) else term(body, env)
                    macros(instanceName) = (ps, resultSort, typed)
                    pending += Command.DefineFun(instanceName, ps, resultSort, typed)
              finally sortSubst = saved
        instanceName

  /** The name of the instance of `name` at the sorts `args`: `name@τ1@…`. */
  private def mangle(name: String, args: List[Sort]): String = s"$name@${args.map(_.text).mkString("@")}"

  /** The instance of a parametric datatype at the sorts `args`, declared with
   *  its constructors and selectors on first use: the sort `(name args)`, the
   *  symbols `c@args`.
   */
  private def instantiate(name: String, args: List[Sort]): Sort =
    val (params, decls) = templates(name)
    if args.length != params.length then throw Smt2Error(s"$name takes ${params.length} sort arguments")
    val text = s"($name ${args.map(_.text).mkString(" ")})"
    if instantiated.add(text) then
      val saved = sortSubst
      sortSubst = params.zip(args.map(sexprOf)).toMap
      try
        val datatype = Datatype(
          text,
          decls.map {
            case SList(SAtom(constructor) :: selectors) =>
              Constructor(
                mangle(constructor, args),
                selectors.map {
                  case SList(SAtom(selector) :: s :: Nil) => (mangle(selector, args), sort(s))
                  case other => throw Smt2Error(s"bad selector $other")
                }
              )
            case other => throw Smt2Error(s"bad constructor $other")
          }
        )
        register(datatype)
        pending += Command.DeclareDatatypes(List(datatype))
      finally sortSubst = saved
    Sort.Named(text)

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
    datatypes.foreach(register)
    Command.DeclareDatatypes(datatypes)

  /** Makes a datatype's constructors, selectors and testers known. */
  private def register(datatype: Datatype): Unit =
    val d = Sort.Named(datatype.name)
    for constructor <- datatype.constructors do
      constructors(constructor.name) = constructor
      functions(constructor.name) = Sort.fun(constructor.selectors.map(_._2), d)
      functions(constructor.tester) = Sort.fun(List(d), Sort.Bool)
      for (selector, s) <- constructor.selectors do functions(selector) = Sort.fun(List(d), s)

  /** Keeps a parametric datatype's declaration for its instances, and makes its
   *  constructors and selectors polymorphic symbols of the datatype.
   */
  private def template(name: String, params: List[SExpr], constructors: List[SExpr]): Unit =
    val ps = params.map { case SAtom(p) => p; case other => throw Smt2Error(s"bad sort parameter $other") }
    templates(name) = (ps, constructors)
    val self = SList(SAtom(name) :: ps.map(SAtom(_)))
    for c <- constructors do
      c match
        case SList(SAtom(constructor) :: selectors) =>
          val fields = selectors.map {
            case SList(SAtom(selector) :: s :: Nil) =>
              polymorphic(selector) = Poly.Fun(ps, List(self), s)
              owner(selector) = name
              s
            case other => throw Smt2Error(s"bad selector $other")
          }
          polymorphic(constructor) = Poly.Fun(ps, fields, self)
          owner(constructor) = name
        case other => throw Smt2Error(s"bad constructor $other")

  private def sort(s: SExpr): Sort = s match
    case SAtom(name) if sortSubst.contains(name) => sort(sortSubst(name))
    case SAtom(name) if sortDefinitions.get(name).exists(_._1.isEmpty) => sort(sortDefinitions(name)._2)
    case SList(SAtom(name) :: args) if sortDefinitions.contains(name) =>
      val (params, body) = sortDefinitions(name)
      if args.length != params.length then throw Smt2Error(s"$name takes ${params.length} sort arguments")
      sort(substitute(body, params.zip(args).toMap))
    case SList(SAtom(name) :: args) if templates.contains(name) => instantiate(name, args.map(sort))
    case SAtom("Bool") => Sort.Bool
    case SAtom("Int") => Sort.Int
    case SAtom("Real") => Sort.Real
    case SAtom(name) => Sort.Named(name)
    case SList(SAtom("_") :: SAtom("BitVec") :: SAtom(width) :: Nil) => Sort.BitVec(width.toInt)
    case SList(SAtom("Array") :: args) if args.length >= 2 =>
      val sorts = args.map(sort)
      sorts.init.foldRight(sorts.last)(Sort.Array(_, _))
    case other => Sort.Named(other.toString) // opaque: rejected by `check` where a symbol is declared

  /** Rejects, at a declaration or a binder, the sorts of theories that are not
   *  spoken here, which would otherwise pass for uninterpreted sorts: strings,
   *  floats, sets, ... (a datatype field is not checked, as before: an unused
   *  one costs nothing).
   */
  private def check(s: Sort): Unit = s match
    case Sort.Named(name) if name.startsWith("(") && declaredSorts(name.drop(1).takeWhile(_ != ' ')) => ()
    case Sort.Named(name) =>
      val unsupported =
        List("String", "RegLan", "RoundingMode", "UnitTuple", "(Set ", "(Bag ", "(Seq ", "(Tuple ", "(_ FloatingPoint")
      if unsupported.exists(u => name == u || name.startsWith(u)) || name.startsWith("Float") then
        throw Smt2Error(s"the sort $name is not supported")
    case Sort.Array(index, element) =>
      check(index)
      check(element)
    case _ => ()

  /** The parameters of a macro, `((x S) ...)`, with their sorts.
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
   *  the `let`-bound names in scope.
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
        if polymorphic.contains(name) then symbolSort(instance(name, Nil, None))
        else throw Smt2Error(s"unknown symbol $name")
      )
    case SList(SAtom("as") :: SAtom(name) :: s :: Nil) => symbolSort(instance(name, Nil, Some(sort(s))))
    case SList(SList(SAtom("as") :: SAtom(name) :: s :: Nil) :: args) =>
      resultSort(instance(name, args.map(sortOf(_, env)), Some(sort(s))), args.length)
    case SList(SAtom("bvcomp") :: _) => Sort.BitVec(1)
    case SList(SAtom("ubv_to_int" | "sbv_to_int" | "bv2nat" | "bv2int") :: _) => Sort.Int
    case SList(SList(SAtom("_") :: SAtom("int_to_bv" | "int2bv") :: SAtom(n) :: Nil) :: _) => Sort.BitVec(n.toInt)
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
    case SList(SAtom("div" | "mod" | "abs" | "to_int") :: _) => Sort.Int
    case SList(SAtom("to_real") :: _) => Sort.Real
    case SList(SAtom("is_int") :: _) => Sort.Bool
    case SList(SAtom("select") :: a :: _ :: Nil) =>
      sortOf(a, env) match
        case Sort.Array(_, element) => element
        case s => throw Smt2Error(s"select on $s")
    case SList(SAtom("store") :: a :: _ :: _ :: Nil) => sortOf(a, env)
    case SList(SAtom("+" | "-" | "*") :: args) =>
      if args.exists(a => sortOf(a, env) == Sort.Real) then Sort.Real else Sort.Int
    case SList(SList(SAtom("_") :: SAtom("is") :: SAtom(_) :: Nil) :: _ :: Nil) => Sort.Bool
    case SList(SAtom("ite") :: _ :: thn :: _ :: Nil) => sortOf(thn, env)
    case SList(SAtom("let") :: SList(bindings) :: body :: Nil) =>
      sortOf(body, env ++ bindings.map { case SList(SAtom(name) :: value :: Nil) => name -> sortOf(value, env) })
    case SList(SAtom("match") :: _) => sortOf(matchToIte(sexpr), env)
    case SList(SAtom(name) :: args) if macros.contains(name) => macros(name)._2
    case SList(SAtom(name) :: args) if polymorphic.contains(name) && !env.contains(name) =>
      resultSort(instance(name, args.map(sortOf(_, env)), None), args.length)
    case SList(SAtom(name) :: args) => applied(sortOf(SAtom(name), env), args.length)
    case other => throw Smt2Error(s"cannot type $other")

  /** The sort of a monomorphic symbol: a declared function's whole function
   *  sort, or a macro's result.
   */
  private def symbolSort(name: String): Sort =
    functions.get(name).orElse(macros.get(name).map(_._2)).getOrElse(throw Smt2Error(s"unknown symbol $name"))

  /** The sort of a monomorphic symbol applied to `n` arguments: a macro's is
   *  its result.
   */
  private def resultSort(name: String, n: Int): Sort =
    macros.get(name).map(_._2).getOrElse(applied(symbolSort(name), n))

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
      val c = if owner.contains(constructor) then instance(constructor, Nil, Some(sortOf(arg, env))) else constructor
      declared(s"(_ is $c)", env)
      IsConstructor(c, term(arg, env))
    case SList(SAtom("is_int") :: arg :: Nil) => IsInt(term(arg, env))
    case SList(SAtom(op @ ("<=" | "<" | ">=" | ">")) :: args) if args.length >= 2 =>
      for a <- args if !arithmetic(sortOf(a, env)) do
        throw Smt2Error(s"$op on ${sortOf(a, env)} is not supported")
      Cmp(CmpOp.values.find(_.name == op).get, args.map(term(_, env)))
    case SList(SAtom("ite") :: cond :: thn :: els :: Nil) =>
      Ite(formula(cond, env), formula(thn, env), formula(els, env))
    case SList(SAtom("let") :: SList(bs) :: body :: Nil) =>
      val (bound, inner) = bindings(bs, env)
      Let(bound, formula(body, inner))
    case SList(SAtom("match") :: _) => formula(matchToIte(sexpr), env)
    case SList(SAtom("!") :: body :: _) => formula(body, env) // an annotated term
    case _ => // a Boolean constant or application
      if sortOf(sexpr, env) != Sort.Bool then throw Smt2Error(s"not a formula: $sexpr")
      Formula.Pred(term(sexpr, env))
  end formula

  /** `(match t ((p1 b1) ... (pn bn)))` as the tree the rest of the parser
   *  reads: the scrutinee bound once by a `let` — so it is shared, as `let`
   *  bindings are — then an `ite` per case on the constructor's tester, its
   *  body under a `let` binding the pattern's variables to the selectors; the
   *  last case is the default (the cases are exhaustive), and a variable
   *  pattern binds the scrutinee itself and ends the cases. A pattern symbol
   *  that names a constructor is that constructor; any other is a variable.
   */
  private def matchToIte(sexpr: SExpr): SExpr = sexpr match
    case SList(SAtom("match") :: scrutinee :: SList(cases) :: Nil) if cases.nonEmpty =>
      matches += 1
      val name = s"match!$matches"
      val m = SAtom(name)
      def binding(name: String, value: SExpr): SExpr = SList(List(SAtom(name), value))
      def let(bindings: List[SExpr], body: SExpr): SExpr =
        if bindings.isEmpty then body else SList(List(SAtom("let"), SList(bindings), body))
      def isConstructor(name: String): Boolean = constructors.contains(name) ||
        owner.contains(name) && polymorphic.contains(name)
      def selectorsOf(name: String): List[String] = constructors.get(name) match
        case Some(c) => c.selectors.map(_._1)
        case None => // a parametric constructor: its declaration in the template
          templates(owner(name))._2.collectFirst {
            case SList(SAtom(`name`) :: selectors) => selectors.map { case SList(SAtom(s) :: _) => s; case _ => "?" }
          }.getOrElse(Nil)
      def branch(c: SExpr): (Option[SExpr], SExpr) = c match // the tester, or none for a variable, and the body
        case SList(SAtom(name) :: body :: Nil) if isConstructor(name) =>
          (Some(SList(List(SList(List(SAtom("_"), SAtom("is"), SAtom(name))), m))), body)
        case SList(SAtom(name) :: body :: Nil) => (None, let(List(binding(name, m)), body))
        case SList(SList(SAtom(name) :: vars) :: body :: Nil) if isConstructor(name) =>
          val selectors = selectorsOf(name)
          if vars.length != selectors.length then throw Smt2Error(s"$name takes ${selectors.length} arguments: $c")
          val bound = vars.zip(selectors).flatMap {
            case (SAtom("_"), _) => None // a wildcard binds nothing
            case (SAtom(v), selector) => Some(binding(v, SList(List(SAtom(selector), m))))
            case (other, _) => throw Smt2Error(s"bad pattern variable $other")
          }
          (Some(SList(List(SList(List(SAtom("_"), SAtom("is"), SAtom(name))), m))), let(bound, body))
        case other => throw Smt2Error(s"bad match case $other")
      val branches = cases.map(branch)
      // Up to the first variable pattern, which catches everything after it.
      val reached = branches.indexWhere(_._1.isEmpty) match
        case -1 => branches
        case i => branches.take(i + 1)
      val body = reached.init.foldRight(reached.last._2) { case ((tester, body), rest) =>
        SList(List(SAtom("ite"), tester.get, body, rest))
      }
      let(List(binding(name, scrutinee)), body)
    case other => throw Smt2Error(s"bad match $other")

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
    // A formula-shaped tree where a term is expected (an argument, a field).
    case SList(SAtom("not" | "and" | "or" | "=>" | "xor" | "distinct" | "<=" | "<" | ">=" | ">" | "is_int") :: _) =>
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
      val concrete = if !env.contains(name) then instance(name, Nil, None) else name
      if !env.contains(name) then // a function symbol is applied, never a value
        val params =
          macros.get(concrete).map(_._1.length).orElse(functions.get(concrete).flatMap(_.arrow).map(_._1.length))
        params.filter(_ > 0).foreach(n => throw Smt2Error(s"$name expects $n arguments"))
      Term.Const(concrete)
    case SList(SAtom("as") :: SAtom(name) :: s :: Nil) => Term.Const(instance(name, Nil, Some(sort(s))))
    case SList(SList(SAtom("as") :: SAtom(name) :: s :: Nil) :: args) =>
      Term.App(Term.Const(instance(name, args.map(sortOf(_, env)), Some(sort(s)))), args.map(term(_, env)))
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
    case SList(SAtom("to_real") :: a :: Nil) => Term.ArithApp(ArithOp.ToReal, List(term(a, env)))
    case SList(SAtom("to_int") :: a :: Nil) => Term.ArithApp(ArithOp.ToInt, List(term(a, env)))
    case SList(SAtom("ite") :: cond :: thn :: els :: Nil) =>
      Term.Ite(formula(cond, env), term(thn, env), term(els, env))
    case SList(SAtom("let") :: SList(bs) :: body :: Nil) =>
      val (bound, inner) = bindings(bs, env)
      Term.Let(bound, term(body, inner))
    case SList(SAtom("match") :: _) => term(matchToIte(sexpr), env)
    case SList(SAtom("!") :: body :: _) => term(body, env) // an annotated term
    case SList(SAtom(name) :: args) =>
      declared(name, env)
      val concrete = if !env.contains(name) then instance(name, args.map(sortOf(_, env)), None) else name
      Term.App(Term.Const(concrete), args.map(term(_, env)))
    case other => throw Smt2Error(s"not a term: $other")
  end term

  /** Whether `a` is an array: what `select` and `store` accept. */
  private def selectable(a: SExpr, env: Map[String, Sort]): Boolean = sortOf(a, env) match
    case Sort.Array(_, _) => true
    case _ => false

  /** Checks that `name` is declared, bound or a macro. */
  private def declared(name: String, env: Map[String, Sort]): Unit =
    if !env.contains(name) && !functions.contains(name) && !macros.contains(name) && !polymorphic.contains(name) then
      throw Smt2Error(s"unknown symbol $name")

object Parser:
  def parse(input: String): Script = Parser().parse(input)
