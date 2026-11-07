package io.bovel.smtlib

import java.security.MessageDigest

import scala.collection.mutable
import scala.util.hashing.MurmurHash3

import io.bovel.smtlib.sexpr.SExpr
import io.bovel.smtlib.sexpr.SExpr.{SKeyword, SList, SSymbol}

/** How alike two scripts are, at increasing levels of abstraction. Each exact
 *  level does what the one before does, and more:
 *
 *  - `Text`: the S-expressions, without comments, layout or `set-info`;
 *  - `Names`: and the script's own symbols numbered (`v0`, `S0` for sorts), the
 *    bound variables named by the position of their binder (`b!depth!k`);
 *  - `Order`: and the order — associative operations flattened, the arguments
 *    of commutative ones sorted, a top-level `and` split into assertions, and
 *    between two commands that are not declarations or assertions, the
 *    declarations and the assertions sorted (`declare-const` read as the
 *    `declare-fun` it is), as are a `let`'s bindings, which are simultaneous.
 *
 *  A canonical form is the script itself, renamed and reordered without
 *  changing its meaning. So two files with the same form are the same problem,
 *  and where the form is not quite canonical, a likeness is missed, never made
 *  up.
 *
 *  `Order` numbers symbols by first use, so it must sort before it can name
 *  them: it sorts by colors. A symbol's color starts as its kind and is refined
 *  by where the symbol occurs, until the colors stop splitting. While symbols
 *  still share a color, the first one gets a color of its own and the
 *  refinement resumes. When those symbols are interchangeable, as in
 *  `(+ a b c)`, the choice does not matter. The colors only decide the order:
 *  printing them instead of names would merge different scripts, since a cycle
 *  of six `distinct`s (sat) and two triangles of three (unsat) color alike.
 *
 *  Known misses: the colors do not see how a `let`'s variables are used, and
 *  ties left after 128 choices keep the script's order. On the corpus
 *  (2026-11-07), 412 of 5 462 forms differ from their own form, none of them a
 *  Scala 3 dump. z3 answers every form as it does its file, except one file
 *  whose `List` datatype z3 takes for its built-in one, and VIK's parser reads
 *  every form.
 *
 *  Beyond the exact levels, [[similarity]] compares the shapes of two scripts:
 *  the multisets of their subtrees cut at depths one to three, names left out,
 *  by weighted Jaccard index.
 */
object Alike:
  enum Level:
    case Text, Names, Order

  /** The canonical form of the script at a level, as a digest. */
  def digest(commands: List[SExpr], level: Level): String =
    val bytes = MessageDigest.getInstance("SHA-256").digest(canonical(commands, level).getBytes("UTF-8"))
    bytes.map(b => f"$b%02x").mkString

  /** The canonical form of the script at a level. */
  def canonical(commands: List[SExpr], level: Level): String =
    val script = commands.filter(!isSetInfo(_))
    level match
      case Level.Text => script.mkString("\n")
      case Level.Names =>
        val form = Form(script, order = false, Map.empty)
        form.text(numbering(form.commands))
      case Level.Order =>
        val seen = numbering(Form(script, order = false, Map.empty).commands)
        var colors = stable(script, Map.empty)
        var steps = 0
        var tied = ties(colors)
        while tied.nonEmpty && steps < 128 do
          val first = tied.minBy(seen)
          colors = stable(script, colors.updated(first, mix(colors(first) + 1)))
          tied = ties(colors)
          steps += 1
        val form = Form(script, order = true, colors)
        form.text(numbering(form.commands))

  /** The status before each `check-sat` (`none` without one), as the corpus
   *  tests read them: the last `(set-info :status ...)` since the previous
   *  `check-sat`.
   */
  def statuses(commands: List[SExpr]): List[String] =
    var last = "none"
    commands.flatMap {
      case SList(SSymbol("set-info") :: SKeyword("status") :: value :: Nil) =>
        last = value.toString
        Nil
      case SList(SSymbol(command) :: _) if command.startsWith("check-sat") =>
        val status = last
        last = "none"
        List(status)
      case _ => Nil
    }

  /** The theory operations a script applies: the heads of its applications
   *  that are not its own symbols (`bvadd`, `select`, the `extract` of
   *  `(_ extract 7 0)`); a literal such as `(_ bv5 32)` applies none.
   */
  def operations(commands: List[SExpr]): Set[String] =
    val found = mutable.Set.empty[String]
    def visit(node: Node): Unit = node match
      case Items(items) =>
        items.headOption.foreach {
          case Atom(op) => found += op
          case Items(Atom("_" | "as") :: Atom(op) :: _) => found += op
          case _ => ()
        }
        items.foreach(visit)
      case _ => ()
    Form(commands.filter(!isSetInfo(_)), order = false, Map.empty).commands.foreach(visit)
    found.toSet

  /** The shape of a script: how many times each subtree, cut at depths one to
   *  three and with its names left out, occurs in it once sorted.
   */
  final class Shape private[Alike] (private[Alike] val keys: Array[Long], private[Alike] val counts: Array[Int])

  def shape(commands: List[SExpr]): Shape =
    val bag = mutable.HashMap.empty[Long, Int]
    def visit(node: Node): Array[Long] = node match
      case Items(items) =>
        val below = items.map(visit)
        // The cut at depth d + 1 combines the children's cuts at depth d (their labels at 0).
        val cut = Array.tabulate(3)(d => mix(below.foldLeft(0x51ed27L + d)((h, b) => h * 31 + b(d))))
        cut.foreach(h => bag(h) = bag.getOrElse(h, 0) + 1)
        node.label +: cut
      case _ => Array.fill(4)(node.label)
    Form(commands.filter(!isSetInfo(_)), order = true, Map.empty).commands.foreach(visit)
    val keys = bag.keys.toArray.sorted
    Shape(keys, keys.map(bag))

  /** The weighted Jaccard index of two shapes: the size of the intersection of
   *  the multisets over that of their union, 1 for the same shape.
   */
  def similarity(a: Shape, b: Shape): Double =
    var i = 0
    var j = 0
    var both = 0L
    var either = 0L
    while i < a.keys.length || j < b.keys.length do
      if j == b.keys.length || i < a.keys.length && a.keys(i) < b.keys(j) then
        either += a.counts(i); i += 1
      else if i == a.keys.length || b.keys(j) < a.keys(i) then
        either += b.counts(j); j += 1
      else
        both += math.min(a.counts(i), b.counts(j))
        either += math.max(a.counts(i), b.counts(j))
        i += 1; j += 1
    if either == 0 then 1.0 else both.toDouble / either

  private def isSetInfo(command: SExpr) = command match
    case SList(SSymbol("set-info") :: _) => true
    case _ => false

  // The canonical tree: theory symbols, literals and bound variables are atoms; the script's own symbols are
  // `Global`s, which hash as their color. Each node carries its hash, so that sorting by it is linear in the
  // script.
  private sealed trait Node:
    val hash: Long
    /** The hash with the names left out, for the shapes. */
    def label: Long = hash

  private final case class Atom(text: String) extends Node:
    val hash: Long = mix(MurmurHash3.stringHash(text).toLong << 32 | MurmurHash3.stringHash(text, 7) & 0xffffffffL)

  /** A symbol the script declares: a sort, a function or a constant, or a
   *  `:named` term; `declares` at its declaration.
   */
  private final case class Global(name: String, sort: Boolean, declares: Boolean, color: Long) extends Node:
    val hash: Long = color
    override def label: Long = kind(sort)

  private def kind(sort: Boolean): Long = if sort then 0x5011L else 0xf011L

  private final case class Items(items: List[Node]) extends Node:
    val hash: Long = mix(items.foldLeft(0x17e45L)((h, n) => h * 31 + n.hash))
    override def label: Long = 0x115L

  // splitmix64's finalizer.
  private def mix(x: Long): Long =
    var z = x * 0x9e3779b97f4a7c15L
    z = (z ^ (z >>> 30)) * 0xbf58476d1ce4e5b9L
    z = (z ^ (z >>> 27)) * 0x94d049bb133111ebL
    z ^ (z >>> 31)

  /** The script's own symbols, numbered by first use in the form (sorts and
   *  the others apart), then the unused ones by declaration.
   */
  private def numbering(commands: List[Node]): Map[(Boolean, String), Int] =
    val names = mutable.HashMap.empty[(Boolean, String), Int]
    val next = Array(0, 0)
    def visit(node: Node, declarations: Boolean): Unit = node match
      case Global(name, sort, declares, _) if declares == declarations && !names.contains((sort, name)) =>
        val k = if sort then 1 else 0
        names((sort, name)) = next(k)
        next(k) += 1
      case Items(items) => items.foreach(visit(_, declarations))
      case _ => ()
    commands.foreach(visit(_, declarations = false))
    commands.foreach(visit(_, declarations = true))
    names.toMap

  /** The colors refined from those given until no class of them parts. */
  private def stable(script: List[SExpr], start: Map[(Boolean, String), Long]): Map[(Boolean, String), Long] =
    var colors = start
    var classes = start.values.toSet.size
    var rounds = 0
    var refining = true
    while refining && rounds < 8 do
      colors = refine(Form(script, order = true, colors), colors)
      refining = colors.values.toSet.size > classes
      classes = colors.values.toSet.size
      rounds += 1
    colors

  /** The symbols of the first color (by value) two or more share, if any. */
  private def ties(colors: Map[(Boolean, String), Long]): List[(Boolean, String)] =
    colors.groupBy(_._2).filter(_._2.size > 1).minByOption(_._1).toList.flatMap(_._2.keys)

  /** Each symbol's color, refined by where it occurs: for each occurrence, its
   *  command's run (the runs keep their order, the commands in one sort), the
   *  hashes of the subtrees on the path down to it and its position in each
   *  (but not among the arguments of a commutative operation, which sort).
   */
  private def refine(form: Form, colors: Map[(Boolean, String), Long]): Map[(Boolean, String), Long] =
    val places = mutable.HashMap.empty[(Boolean, String), mutable.ArrayBuffer[Long]]
    def visit(node: Node, path: Long): Unit = node match
      case Global(name, sort, _, _) => places.getOrElseUpdate((sort, name), mutable.ArrayBuffer.empty) += path
      case Items(items) =>
        val symmetric = items.headOption.exists { case Atom(op) => commutative(op); case _ => false }
        for (n, i) <- items.zipWithIndex do
          visit(n, mix(mix(path * 31 + node.hash) * 31 + (if symmetric && i > 0 then 1 else i)))
      case _ => ()
    form.commands.zip(form.runs).foreach((c, run) => visit(c, mix(run + 1)))
    places.map((g, ps) => g -> mix(ps.sorted.foldLeft(colors.getOrElse(g, kind(g._1)))((h, p) => h * 31 + p))).toMap

  private val associative = Set("and", "or", "xor", "+", "*", "bvadd", "bvmul", "bvand", "bvor", "bvxor")
  private val commutative =
    associative ++ Set("=", "distinct", "bvnand", "bvnor", "bvxnor", "bvcomp")
  private val sortDeclarations = Set("declare-sort", "define-sort", "declare-datatype", "declare-datatypes")
  private val functionDeclarations = Set("declare-fun", "declare-const")
  private val definitions = Set("define-fun", "define-fun-rec", "define-funs-rec", "define-const")

  /** Where a name stands for a bound variable or sort parameter, and how deep
   *  in binders.
   */
  private final case class Scope(terms: Map[String, String], sorts: Map[String, String], depth: Int):
    def bind(vars: List[String]): (Scope, List[String]) =
      val names = vars.indices.map(k => s"b!$depth!$k").toList
      (copy(terms = terms ++ vars.zip(names), depth = depth + 1), names)
    def bindSorts(params: List[String]): (Scope, List[String]) =
      val names = params.indices.map(k => s"T!$depth!$k").toList
      (copy(sorts = sorts ++ params.zip(names), depth = depth + 1), names)

  private val top = Scope(Map.empty, Map.empty, 0)

  /** The script as a canonical tree, reordered if `order` by the colors given
   *  (a symbol without one has its kind's).
   */
  private final class Form(script: List[SExpr], order: Boolean, colors: Map[(Boolean, String), Long]):
    private val sorts = mutable.Set.empty[String]
    private val functions = mutable.Set.empty[String]
    script.foreach(declared)

    /** The commands, and for each the index of its run: a command alone, or
     *  declarations and assertions in a row, which sort among themselves.
     */
    val (commands: List[Node], runs: List[Int]) =
      if !order then (script.map(command), script.indices.toList)
      else
        val out = mutable.ListBuffer.empty[(Node, Int)]
        var rest = script
        var index = 0
        while rest.nonEmpty do
          val (run, after) = rest.span(c => movable(head(c)))
          if run.isEmpty then
            out += command(rest.head) -> index
            rest = rest.tail
          else
            out ++= reordered(run).map(_ -> index)
            rest = after
          index += 1
        out.toList.unzip

    /** The tree printed, the script's own symbols named by their numbers. */
    def text(numbers: Map[(Boolean, String), Int]): String =
      val out = StringBuilder()
      def write(node: Node): Unit = node match
        case Atom(text) => out ++= text
        case g: Global => out ++= (if g.sort then "S" else "v") ++= numbers((g.sort, g.name)).toString
        case Items(items) =>
          out += '('
          items.zipWithIndex.foreach((n, i) => { if i > 0 then out += ' '; write(n) })
          out += ')'
      commands.zipWithIndex.foreach((c, i) => { if i > 0 then out += '\n'; write(c) })
      out.toString

    private def head(command: SExpr): String = command match
      case SList(SSymbol(h) :: _) => h
      case _ => ""

    private def movable(h: String) =
      h == "assert" || sortDeclarations(h) || functionDeclarations(h) || definitions(h)

    // A `:named` term is a name later commands of the run may use: its definitions and assertions keep their order.
    private def named(run: List[SExpr]): Boolean =
      def has(e: SExpr): Boolean = e match
        case SKeyword("named") => true
        case SList(items) => items.exists(has)
        case _ => false
      run.exists(has)

    /** The run as sort declarations sorted, the other sort declarations (in
     *  their order: one may use another), function declarations sorted,
     *  definitions (in their order), then assertions sorted — or, with a
     *  `:named` term, the definitions and assertions in their order.
     */
    private def reordered(run: List[SExpr]): List[Node] =
      val (sortsDeclared, rest0) = run.partition(c => head(c) == "declare-sort")
      val (types, others) = rest0.partition(c => sortDeclarations(head(c)))
      val (decls, rest) = others.partition(c => functionDeclarations(head(c)))
      val start = sorted(sortsDeclared.map(command)) ++ types.map(command) ++ sorted(decls.map(command))
      if named(run) then return start ++ rest.map(command)
      val (defs, asserts) = rest.partition(c => definitions(head(c)))
      val assertions = asserts.map(command).flatMap {
        case Items(Atom("assert") :: Items(Atom("and") :: conjuncts) :: Nil) =>
          conjuncts.map(c => Items(List(Atom("assert"), c)))
        case other => List(other)
      }
      start ++ defs.map(command) ++ sorted(assertions)

    private def sorted(nodes: List[Node]): List[Node] = nodes.sortBy(_.hash)

    private def global(name: String, sort: Boolean, declares: Boolean = false) =
      Global(name, sort, declares, colors.getOrElse((sort, name), kind(sort)))

    private def declared(command: SExpr): Unit =
      declaredHead(command)
      namedTerms(command)

    private def declaredHead(command: SExpr): Unit = command match
      case SList(SSymbol("declare-sort" | "define-sort") :: SSymbol(s) :: _) => sorts += s
      case SList(SSymbol("declare-fun" | "declare-const" | "define-fun" | "define-fun-rec" | "define-const") ::
            SSymbol(f) :: _) => functions += f
      case SList(SSymbol("define-funs-rec") :: SList(decls) :: _) =>
        decls.foreach { case SList(SSymbol(f) :: _) => functions += f; case _ => () }
      case SList(SSymbol("declare-datatype") :: SSymbol(d) :: decl :: Nil) =>
        sorts += d
        constructors(decl).foreach(declaredConstructor)
      case SList(SSymbol("declare-datatypes") :: SList(heads) :: SList(decls) :: Nil) =>
        if heads.nonEmpty && heads.forall(isArity) then
          heads.foreach { case SList(SSymbol(d) :: _) => sorts += d; case _ => () }
          decls.foreach(constructors(_).foreach(declaredConstructor))
        else // SMT-LIB 2.5: `(declare-datatypes (params) ((D constructors...) ...))`
          decls.foreach {
            case SList(SSymbol(d) :: cs) => sorts += d; cs.foreach(declaredConstructor)
            case _ => ()
          }
      case _ => ()

    private def namedTerms(e: SExpr): Unit = e match
      case SList(items) =>
        items.sliding(2).foreach {
          case SKeyword("named") :: SSymbol(n) :: Nil => functions += n
          case _ => ()
        }
        items.foreach(namedTerms)
      case _ => ()

    private def isArity(e: SExpr) = e match
      case SList(SSymbol(_) :: SExpr.SNumeral(_) :: Nil) => true
      case _ => false

    private def constructors(decl: SExpr): List[SExpr] = decl match
      case SList(SSymbol("par") :: SList(_) :: SList(cs) :: Nil) => cs
      case SList(cs) => cs
      case _ => Nil

    private def declaredConstructor(c: SExpr): Unit = c match
      case SSymbol(name) => functions += name
      case SList(SSymbol(name) :: selectors) =>
        functions += name
        selectors.foreach { case SList(SSymbol(s) :: _) => functions += s; case _ => () }
      case _ => ()

    private def command(c: SExpr): Node = c match
      case SList(SSymbol("declare-fun") :: SSymbol(f) :: SList(args) :: result :: Nil) =>
        Items(List(Atom("declare-fun"), global(f, false, true), Items(args.map(sort(_, top))), sort(result, top)))
      case SList(SSymbol("declare-const") :: SSymbol(f) :: result :: Nil) =>
        if order then Items(List(Atom("declare-fun"), global(f, false, true), Items(Nil), sort(result, top)))
        else Items(List(Atom("declare-const"), global(f, false, true), sort(result, top)))
      case SList(SSymbol(d @ ("define-fun" | "define-fun-rec")) :: SSymbol(f) :: SList(params) :: result :: body :: Nil)
          if params.forall(isBinding) =>
        val (inner, vars) = parameters(params, top)
        Items(List(Atom(d), global(f, false, true), vars, sort(result, top), term(body, inner)))
      case SList(SSymbol("define-funs-rec") :: SList(decls) :: SList(bodies) :: Nil)
          if decls.length == bodies.length && decls.forall {
            case SList(SSymbol(_) :: SList(ps) :: _ :: Nil) => ps.forall(isBinding)
            case _ => false
          } =>
        val (heads, terms) = decls.zip(bodies).map {
          case (SList(SSymbol(f) :: SList(params) :: result :: Nil), body) =>
            val (inner, vars) = parameters(params, top)
            (Items(List(global(f, false, true), vars, sort(result, top))), term(body, inner))
          case _ => throw IllegalStateException("checked above")
        }.unzip
        Items(List(Atom("define-funs-rec"), Items(heads), Items(terms)))
      case SList(SSymbol("define-const") :: SSymbol(f) :: s :: t :: Nil) =>
        Items(List(Atom("define-const"), global(f, false, true), sort(s, top), term(t, top)))
      case SList(SSymbol("declare-sort") :: SSymbol(s) :: rest) =>
        Items(Atom("declare-sort") :: global(s, true, true) :: rest.map(a => Atom(a.toString)))
      case SList(SSymbol("define-sort") :: SSymbol(s) :: SList(params) :: body :: Nil)
          if params.forall(_.isInstanceOf[SSymbol]) =>
        val (inner, ps) = top.bindSorts(params.collect { case SSymbol(p) => p })
        Items(List(Atom("define-sort"), global(s, true, true), Items(ps.map(Atom(_))), sort(body, inner)))
      case SList(SSymbol("declare-datatype") :: SSymbol(d) :: decl :: Nil) =>
        Items(List(Atom("declare-datatype"), global(d, true, true), datatype(decl)))
      case SList(SSymbol("declare-datatypes") :: SList(heads) :: SList(decls) :: Nil)
          if heads.nonEmpty && heads.forall(isArity) =>
        val ds = heads.collect { case SList(SSymbol(d) :: n :: Nil) => Items(List(global(d, true, true), Atom(n.toString))) }
        Items(List(Atom("declare-datatypes"), Items(ds), Items(decls.map(datatype))))
      case SList(SSymbol(h) :: args) => Items(Atom(h) :: args.map(term(_, top)))
      case other => term(other, top)

    private def isBinding(e: SExpr) = e match
      case SList(SSymbol(_) :: _ :: Nil) => true
      case _ => false

    /** Sorted variables bound, `((x Int) (y U))`: the scope inside, and the
     *  variables canonically named.
     */
    private def parameters(params: List[SExpr], scope: Scope): (Scope, Node) =
      val (inner, vars) = scope.bind(params.collect { case SList(SSymbol(x) :: _) => x })
      val sorts = params.collect { case SList(_ :: s :: Nil) => sort(s, scope) }
      (inner, Items(vars.zip(sorts).map((v, s) => Items(List(Atom(v), s)))))

    private def datatype(decl: SExpr): Node = decl match
      case SList(SSymbol("par") :: SList(params) :: SList(cs) :: Nil) if params.forall(_.isInstanceOf[SSymbol]) =>
        val (inner, ps) = top.bindSorts(params.collect { case SSymbol(p) => p })
        Items(List(Atom("par"), Items(ps.map(Atom(_))), Items(cs.map(constructor(_, inner)))))
      case SList(cs) => Items(cs.map(constructor(_, top)))
      case other => term(other, top)

    private def constructor(c: SExpr, scope: Scope): Node = c match
      case SSymbol(name) => global(name, false, true)
      case SList(SSymbol(name) :: selectors) =>
        Items(global(name, false, true) :: selectors.map {
          case SList(SSymbol(s) :: field :: Nil) => Items(List(global(s, false, true), sort(field, scope)))
          case other => term(other, scope)
        })
      case other => term(other, scope)

    private def sort(s: SExpr, scope: Scope): Node = s match
      case SSymbol(n) =>
        scope.sorts.get(n).map(Atom(_)).getOrElse(if sorts(n) then global(n, true) else Atom(s.toString))
      case SList(items) => Items(items.map(sort(_, scope)))
      case other => Atom(other.toString)

    private def term(t: SExpr, scope: Scope): Node = t match
      case SSymbol(n) =>
        scope.terms.get(n).map(Atom(_)).getOrElse(if functions(n) then global(n, false) else Atom(t.toString))
      case SList(SSymbol("let") :: SList(bindings) :: body :: Nil) if bindings.nonEmpty && bindings.forall(isBinding) =>
        // Simultaneous: every bound term is read outside the let; so the bindings sort freely.
        val bound = bindings.collect { case SList(SSymbol(x) :: e :: Nil) => (x, term(e, scope)) }
        val ordered = if order then bound.sortBy(_._2.hash) else bound
        val (inner, vars) = scope.bind(ordered.map(_._1))
        val pairs = vars.zip(ordered).map((v, b) => Items(List(Atom(v), b._2)))
        Items(List(Atom("let"), Items(pairs), term(body, inner)))
      case SList(SSymbol(q @ ("forall" | "exists" | "lambda")) :: SList(params) :: body :: Nil)
          if params.nonEmpty && params.forall(isBinding) =>
        val (inner, vars) = parameters(params, scope)
        Items(List(Atom(q), vars, term(body, inner)))
      case SList(SSymbol("match") :: scrutinee :: SList(cases) :: Nil) =>
        Items(List(Atom("match"), term(scrutinee, scope), Items(cases.map(matchCase(_, scope)))))
      case SList(SSymbol("!") :: body :: attributes) =>
        Items(Atom("!") :: term(body, scope) :: annotation(attributes, scope))
      case SList(SSymbol("as") :: id :: s :: Nil) => Items(List(Atom("as"), term(id, scope), sort(s, scope)))
      case SList(items) => application(items.map(term(_, scope)))
      case other => Atom(other.toString)

    private def matchCase(c: SExpr, scope: Scope): Node = c match
      case SList(SSymbol(p) :: body :: Nil) if !functions(p) => // a variable pattern binds
        val (inner, vars) = scope.bind(List(p))
        Items(List(Atom(vars.head), term(body, inner)))
      case SList(SList(SSymbol(constructor) :: vars) :: body :: Nil) if vars.forall(_.isInstanceOf[SSymbol]) =>
        val (inner, names) = scope.bind(vars.collect { case SSymbol(x) => x })
        Items(List(Items(global(constructor, false) :: names.map(Atom(_))), term(body, inner)))
      case other => term(other, scope)

    private def annotation(attributes: List[SExpr], scope: Scope): List[Node] = attributes match
      case SKeyword("named") :: SSymbol(n) :: rest => Atom(":named") :: global(n, false, true) :: annotation(rest, scope)
      case SKeyword("pattern") :: SList(ts) :: rest => Atom(":pattern") :: Items(ts.map(term(_, scope))) :: annotation(rest, scope)
      case a :: rest => Atom(a.toString) :: annotation(rest, scope)
      case Nil => Nil

    private def application(items: List[Node]): Node = items match
      case Atom(op) :: args if order && commutative(op) =>
        val flat =
          if associative(op) then args.flatMap { case Items(Atom(`op`) :: inner) => inner; case a => List(a) }
          else args
        Items(Atom(op) :: flat.sortBy(_.hash))
      case _ => Items(items)
