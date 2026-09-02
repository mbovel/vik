package net.bovel.vik

import scala.collection.immutable.ArraySeq
import scala.collection.mutable.{ArrayBuffer, HashMap}
import scala.util.boundary
import scala.util.boundary.break

import net.bovel.cdclt
import net.bovel.cdclt.{Lit, Theory}
import net.bovel.egraph.{App, EClassId, EGraph, NoRules}
import net.bovel.lia
import net.bovel.smt2.{Datatype, Sorts}

/** An SMT solver for quantifier-free formulas over equality with uninterpreted
 *  functions and linear integer arithmetic: `assert` formulas, `push` and `pop`
 *  levels of assertions, and `solve`. Each `solve` simplifies the assertions of
 *  all levels (see [[Simplifier]], unless `simplify` is false) and encodes them
 *  afresh into a [[cdclt.Solver]] (Tseitin encoding, one CDCL(T) variable per
 *  distinct atom and per compound subformula) with a [[UfTheory]] over an
 *  [[net.bovel.egraph.EGraph]] for the equalities and a [[LiaTheory]] over a
 *  [[lia.Solver]] for the bounds. The theories are combined over the shared
 *  numeric terms — the ones under uninterpreted functions or compared by
 *  equality: every pair of shared terms that can meet gets its equality as an
 *  atom of both theories, and the SAT solver decides the arrangement (Nelson
 *  and Oppen; see `Encoder.shareIntegers`).
 */
final class Solver(val simplify: Boolean = true):
  enum Result:
    case Sat
    case Unsat

    /** No answer, for a [[Reason]] (SMT-LIB's `unknown` and `:reason-unknown`).
     */
    case Unknown(reason: Reason)

  enum Reason:
    /** The fuel of [[solve]] is spent (`resourceout`). */
    case Fuel

    /** The caller's `keepGoing` of [[solve]] said to stop: a deadline, a
     *  cancellation.
     */
    case Stopped

  private val levels = ArrayBuffer(ArrayBuffer.empty[Formula])

  /** The persistent encoder, built at the first [[solve]] — so that the first
   *  batch is simplified as a whole — and kept across `check-sat`s: terms,
   *  clauses and everything the search learned survive. A formula asserted at a
   *  level above the first is guarded by the level's selector literal, and
   *  [[pop]] retires the selector; [[solve]] assumes the open ones.
   */
  private var encoder: Encoder | Null = null
  private val selectors = ArrayBuffer.empty[Lit]

  /** The constants the first batch's simplification eliminated, and whether the
   *  encoder must be rebuilt (a new assertion mentions one — the elimination
   *  was only sound without such a mention — or a datatype arrived after
   *  encoding began). The rebuild re-encodes [[levels]] and drops what was
   *  learned.
   */
  private var eliminated: Map[String, Term] = Map.empty
  private var dirty = false

  /** The declared function symbols (argument sorts, result sort) and datatypes.
   *  Declarations are optional for uninterpreted functions and integers, and
   *  needed for datatypes: they tell which terms are of a datatype sort.
   */
  private val declarations = HashMap.empty[String, (List[String], String)]
  private val datatypes = ArrayBuffer.empty[Datatype]

  def declare(name: String, argSorts: List[String], sort: String): Unit = declarations(name) = (argSorts, sort)

  /** Declares a datatype: its constructors, selectors and testers become known
   *  function symbols.
   */
  def declare(datatype: Datatype): Unit =
    if encoder != null then dirty = true // its rules and exhaustiveness need an encoder that knows it
    datatypes += datatype
    for c <- datatype.constructors do
      declarations(c.name) = (c.selectors.map(_._2.text), datatype.name)
      declarations(c.tester) = (List(datatype.name), "Bool")
      for (selector, sort) <- c.selectors do declarations(selector) = (List(datatype.name), sort.text)

  def push(): Unit =
    levels += ArrayBuffer.empty
    named += ArrayBuffer.empty
    val e = encoder
    if e != null && !dirty then selectors += e.newSelector()

  def pop(): Unit =
    require(levels.length > 1, "pop without a matching push")
    levels.remove(levels.length - 1)
    named.remove(named.length - 1)
    val e = encoder
    if e != null && !dirty && selectors.length >= levels.length then
      e.retire(selectors.remove(selectors.length - 1))

  /** The named assertions (SMT-LIB `:named`) per level — each one's activation
   *  literal is assumed at every solve while its level is open — and the names
   *  of the last `Unsat`'s core.
   */
  private val named = ArrayBuffer(ArrayBuffer.empty[(String, Formula)])
  private var lastCore: List[String] = Nil

  /** Whether a `Sat` takes its model (see [[model]]): taking one walks the
   *  declarations and the classes, which a caller that never asks should not
   *  pay for — the SMT-LIB player turns this on only when the script asks.
   */
  var produceModels: Boolean = true

  /** The model of the last [[solve]]'s `Sat`, or `None` after any other answer:
   *  values for the constants, a finite graph per declared function (see
   *  [[Model]]).
   */
  def model(): Option[Model] =
    Option(encoder).flatMap(e => Option(e.lastModel)).map { m =>
      // The constants the simplification defined away get their definitions' values: the
      // definitions mention only constants the model already has.
      m.copy(constants = m.constants ++ eliminated.map((name, d) => name -> m.value(d)))
    }

  /** The named assertions among the causes of the last [[solve]]'s `Unsat`:
   *  re-asserting just these (with the unnamed assertions) stays `Unsat`. Empty
   *  when the unnamed assertions alone are already `Unsat`.
   */
  def unsatCore: List[String] = lastCore

  /** Asserts `(! formula :named name)`: a part of [[unsatCore]]. */
  def assert(formula: Formula, name: String): Unit =
    named.last += ((name, formula))
    if mentionsEliminated(formula) then dirty = true

  def assert(formula: Formula): Unit =
    levels.last += formula
    val e = encoder
    if mentionsEliminated(formula) then dirty = true
    else if e != null && !dirty then
      e.assert(formula, if levels.length == 1 then null else selectors(levels.length - 2))

  // The elimination assumed the constant stayed local; a mention breaks it: start over.
  private def mentionsEliminated(formula: Formula): Boolean = encoder != null && !dirty
    && eliminated.nonEmpty && formula.terms.exists(Term.constants(_).exists(eliminated.contains))

  /** Decides the assertions with at most `fuel` steps — a decision or a
   *  conflict of the SAT search, a pivot, a row propagation or a branch of the
   *  LIA solver — `Unknown` beyond: deterministic, unlike a deadline, which
   *  [[solve(keepGoing)]] allows.
   */
  def solve(fuel: Long = Long.MaxValue): Result =
    var left = fuel
    solve(() =>
      left -= 1; left >= 0
    ) match
      case Result.Unknown(_) => Result.Unknown(Reason.Fuel)
      case result => result

  /** Decides the assertions while `keepGoing` says so, asked at every step. */
  def solve(keepGoing: () => Boolean): Result =
    if encoder == null || dirty then
      val e = Encoder(declarations, datatypes.toList)
      encoder = e
      dirty = false
      selectors.clear()
      val constructors = datatypes.flatMap(_.constructors.map(_.name)).toSet
      if levels.length == 1 && simplify then
        // One level: the batch is the whole set, so the global simplification is sound to keep.
        val (simplified, gone) = Simplifier.simplify(levels.head.toList, constructors, named.head.map(_._2).toList)
        eliminated = gone
        for formula <- simplified do e.assert(formula, null)
      else
        eliminated = Map.empty
        for (level, i) <- levels.zipWithIndex do
          val selector = if i == 0 then null
          else
            selectors += e.newSelector(); selectors.last
          for formula <- level do e.assert(formula, selector)
    val e = encoder.nn
    e.produceModels = produceModels
    val activations = named.flatten.toList.map((name, formula) => (name, e.activate(formula)))
    // The passes feed one another — a shared array pair gets a witness, the witness an index, the index
    // new store lemmas and new shared selects — so they run to a fixpoint of the emitted clauses.
    var emitted = -1
    while emitted != e.clauseCount do
      emitted = e.clauseCount
      e.assertExtensionality()
      e.applicationLemmas()
      e.arrayLemmas()
      e.assertExhaustiveness()
      e.shareIntegers()
    e.decideBooleans()
    val assumed = ArrayBuffer.from(selectors)
    assumed ++= activations.map(_._2)
    e.solve(assumed, keepGoing) match
      case cdclt.Solver.Result.Sat(model) =>
        // A satisfying assignment that equates entangled lambdas proves nothing: no finite witness
        // justifies a pointwise identity like g(g x) = x + 1, which no total g satisfies.
        if e.entangledLambdas.exists((f, sel) =>
            e.litOf(f).exists(l => model(l.variable) == l.positive)
              && (sel == null || model(sel.nn.variable) == sel.nn.positive)
          )
        then
          Result.Unknown(Reason.Stopped)
        else
          lastCore = Nil
          Result.Sat
      case cdclt.Solver.Result.Unsat =>
        val core = e.core().toSet
        lastCore = activations.collect { case (name, a) if core(a) => name }
        Result.Unsat
      case cdclt.Solver.Result.Unknown => Result.Unknown(Reason.Stopped)

/** Encodes formulas into clauses over CDCL(T) variables, terms into e-classes
 *  of an e-graph and equalities into atoms of the UF theory, `Int` terms into
 *  variables of a LIA solver and `Le` atoms into bounds on them. Datatypes are
 *  rules of the e-graph ([[AdtRules]]): the encoder registers their symbols'
 *  classes and asserts, for every term of a datatype sort, that one of its
 *  testers holds (and, for a term of a finite datatype sort, does the same for
 *  its selector terms, recursively, so that the search is complete on finite
 *  sorts).
 */
private final class Encoder(declarations: HashMap[String, (List[String], String)], datatypes: List[Datatype]):
  private val classes = HashMap.empty[Term, EClassId]
  private val sorts = HashMap.empty[Term, String] // the sorts of terms, memoized: asked for every term, many times
  private val adt = if datatypes.isEmpty then None else Some(AdtRules())

  private val eg = EGraph(adt.getOrElse(NoRules))
  private val trueTerm = eg.newClass() // the class of Boolean-sorted terms that hold
  private val falseTerm = eg.newClass() // and of those that do not (see [[decideBooleans]])

  /** The symbol applied to a body to make a lambda of `params` parameters. */
  private val lambdaSymbols = HashMap.empty[Int, EClassId]
  private def lambdaSymbol(params: Int): EClassId = lambdaSymbols.getOrElseUpdate(params, eg.newClass())

  adt match
    case Some(adt) =>
      adt.truth(trueTerm)
      for d <- datatypes; c <- d.constructors do
        val constructor = classOf(Term.Const(c.name))
        adt.register(constructor, c.selectors.map((s, _) => classOf(Term.Const(s))), classOf(Term.Const(c.tester)))
        if c.selectors.isEmpty then adt.constant(constructor)
    case _ => ()

  /** The constants this encoder introduces (the witnesses of extensionality),
   *  with their sorts.
   */
  private val introduced = HashMap.empty[String, String]

  /** The sort of a term, from the declarations, a function sort for a function
   *  symbol, a lambda or a partial application (see [[Sorts]]); `?` when
   *  unknown.
   */
  private def sortOf(term: Term): String = sorts.getOrElseUpdate(
    term,
    term match
      case Term.Const(name) =>
        declarations.get(
          name
        ).map((args, sort) => Sorts.function(args, sort)).orElse(introduced.get(name)).getOrElse("?")
      case Term.App(fun, args) => Sorts.applied(sortOf(fun), args.length)
      case Term.Lambda(sorts, body) => Sorts.function(sorts, sortOf(body))
      case Term.Num(_) => "Int"
      case Term.Linear(terms, _) => if terms.exists((_, t) => sortOf(t) == "Real") then "Real" else "Int"
      case Term.Bool(_) => "Bool"
      case Term.Var(_) => "?"
  )

  private val datatypeNames = datatypes.map(_.name).toSet

  /** The datatypes with finitely many values: no constructor field of an
   *  uninterpreted sort or of an infinite (e.g. recursive) datatype, computed
   *  as a least fixpoint.
   */
  private val finite: Set[String] =
    var known = Set("Bool")
    var changed = true
    while changed do
      val more = datatypes.filter(d =>
        !known(d.name) && d.constructors.forall(_.selectors.forall((_, s) => known(s.text)))
      ).map(_.name)
      changed = more.nonEmpty
      known ++= more
    known

  /** The terms known to be `Int`-sorted from their use in arithmetic (a summand
   *  of a combination, a side of `Le`), for undeclared constants.
   */
  private val intTerms = scala.collection.mutable.HashSet.empty[Term]

  private def noteInts(t: Term): Unit = t match
    case Term.Linear(terms, _) => terms.foreach((_, u) => intTerms += u)
    case Term.Num(_) => ()
    case _ => intTerms += t

  private def isInt(t: Term): Boolean = // the sort first: `closed` walks the term
    (t match
      case Term.Num(_) | Term.Linear(_, _) => true
      case _ => sortOf(t) == "Int" || sortOf(t) == "Real" || intTerms(t)
    ) && Term.closed(t)

  /** Theory combination: an `Int` term gets an e-class only when it occurs
   *  under an uninterpreted function or a constructor, or is compared by `Eq`;
   *  such shared terms are leaves for the e-graph and variables for the LIA
   *  solver. For every pair of them that can meet — arguments of the same
   *  function at the same position, applications of the same function, the
   *  sides of an equality atom, a selector's applications and its field in
   *  constructor applications: the only equalities the e-graph uses or makes —
   *  this makes their equality atom (the one the e-graph propagates by
   *  congruence) an atom of the LIA theory too, the equality of their
   *  difference to 0: the SAT solver then decides an arrangement of the shared
   *  terms that both theories accept, which is a model of both (Nelson and
   *  Oppen; both theories are stably infinite).
   */
  private val sharedPairs = scala.collection.mutable.HashSet.empty[(Term, Term)]

  def shareIntegers(): Unit = if anyInt || intTerms.nonEmpty then
    val pairs = ArrayBuffer.empty[(Term, Term)] // the pairs this call is first to meet
    val arrayPairs = ArrayBuffer.empty[(Term, Term)] // function-sorted pairs: their equality atom, for extensionality
    def pair(a: Term, b: Term): Unit =
      if a != b && isInt(a) && isInt(b) && Sorts.arrow(sortOf(a)).isEmpty && Sorts.arrow(sortOf(b)).isEmpty
        && !sharedPairs((b, a)) && sharedPairs.add((a, b))
      then pairs += ((a, b))
      else if a != b && Sorts.arrow(sortOf(a)).isDefined && Sorts.arrow(sortOf(b)).isDefined
        && Term.closed(a) && Term.closed(b) && !sharedPairs((b, a)) && sharedPairs.add((a, b))
      then arrayPairs += ((a, b))
    val applications = classes.keys.collect { case a: Term.App => a }.toList.groupBy(_.fun)
    for (_, group) <- applications do
      val relevant = group.filter(a => isInt(a) || a.args.exists(isInt)) // the others have nothing to share
      for (a, i) <- relevant.zipWithIndex; b <- relevant.drop(i + 1) do
        pair(a, b)
        for (x, y) <- a.args.zip(b.args) do pair(x, y)
    // Applications of different function terms meet too when the functions themselves can be equated —
    // two arrays, or a store and the array it may equal: group by the function's sort instead of the
    // function.
    // Only functions the search may actually equate: the sides of a function equality (recorded or
    // shared), and store applications. Pairing every same-sorted application would be quadratic on
    // ordinary uninterpreted functions.
    val equatableFuns = scala.collection.mutable.HashSet.empty[Term]
    for case Formula.Eq(f, g) <- functionEqualities do
      equatableFuns += f
      equatableFuns += g
    val byFunctionSort = classes.keys.collect {
      case a: Term.App
          if Sorts.arrow(sortOf(a.fun)).isDefined
            && (equatableFuns(a.fun) ||
              (a.fun match
                case Term.App(Term.Const(n), _) => n.startsWith("store!")
                case _ => false)) =>
        a
    }.toList.groupBy(a => (sortOf(a.fun), a.args.length))
    for (_, group) <- byFunctionSort do
      val relevant = group.filter(a => isInt(a) || a.args.exists(isInt))
      for (a, i) <- relevant.zipWithIndex; b <- relevant.drop(i + 1) if a.fun != b.fun do
        pair(a, b)
        for (x, y) <- a.args.zip(b.args) do pair(x, y)
    for case Formula.Eq(a, b) <- lits.keys do pair(a, b)
    // A selector's applications meet the arguments of its constructor at its position (the selector rule).
    for d <- datatypes; c <- d.constructors; ((selector, sort), i) <- c.selectors.zipWithIndex if sort.text == "Int" do
      for s <- applications.getOrElse(Term.Const(selector), Nil); k <- applications.getOrElse(Term.Const(c.name), Nil)
      do
        if k.args.length > i then pair(s, k.args(i))
    for (a, b) <- pairs do
      val e = lit(Formula.Eq(a, b))
      Term.linear(List((1, a), (-1, b))) match
        case Term.Num(v) => clauses += Array(if v.signum == 0 then e else e.neg) // two numerals
        case Term.Linear(terms, c) =>
          liaAtoms(e.variable) = LiaTheory.Atom.Equal(slack(terms), -c)
          anySharedAtom = true
        case difference =>
          liaAtoms(e.variable) = LiaTheory.Atom.Equal(intVar(difference), 0)
          anySharedAtom = true
    // An equality atom per pair of function-sorted shared terms — two arrays under the same function can
    // meet, and the atom's extensionality witness (with the store lemmas at it) decides whether they do:
    // the entailed disjunction of the classic Stump-Barrett-Dill-Levitt example.
    for (a, b) <- arrayPairs do lit(Formula.Eq(a, b))

  /** Applications of the functions the search may equate, as lemmas: for every
   *  closed application `h(args)` and closed function-sorted term `q` other
   *  than `h` that is a lambda or a partial application, of compatible sorts
   *  when known, the clause `h ≠ q ∨ h(args) = q(args)`, `q(args)` in canonical
   *  form ([[Term.app]]: the beta-reduced body, or the application of the
   *  partial application's function to all the arguments); repeated for the
   *  terms this makes, to a fixpoint. It ends for well-sorted terms, whose
   *  reductions are finite (simply typed lambda terms normalize), and is
   *  bounded anyway. A function symbol needs no such lemma: congruence relates
   *  its applications. Lambdas need nothing else: alpha-equivalent ones are the
   *  same term (de Bruijn indices), congruence relates those with equal bodies,
   *  and no atom is ever on an open term (see [[Term.instantiate]]).
   */
  /** The literals of function equalities that no finite witness can justify
   *  accepting: a lambda equated with a lambda, or with a symbol occurring in
   *  its own body (`g ∘ g = succ` has no total solution over the integers, yet
   *  a finite graph looks consistent). A `Sat` with such a literal true is
   *  reported `Unknown` instead.
   */
  val entangledLambdas = ArrayBuffer.empty[(Formula, Lit | Null)] // with the script level's selector, if any

  private def entangled(l: Term, r: Term): Boolean =
    def lam(t: Term): Boolean = t.isInstanceOf[Term.Lambda]
    def occursIn(c: Term, t: Term): Boolean = t match
      case Term.Lambda(_, body) => occursIn(c, body)
      case Term.App(f, args) => occursIn(c, f) || args.exists(occursIn(c, _))
      case Term.Linear(terms, _) => terms.exists((_, u) => occursIn(c, u))
      case Term.Bool(f) => f.terms.exists(occursIn(c, _))
      case _ => t == c
    (lam(l) && lam(r)) || (lam(r) && occursIn(l, r)) || (lam(l) && occursIn(r, l))

  private val applicationsDone = scala.collection.mutable.HashSet.empty[(Term.App, Term)]
  private val equatedApplications = ArrayBuffer.empty[(Term, Term)]

  def applicationLemmas(): Unit = if anyFunction then
    val done = applicationsDone // across solves: the encoder persists, a lemma is emitted once
    val equated = equatedApplications
    var more = true
    while more do
      more = false
      val functions = classes.keys.collect {
        case q @ (Term.Lambda(_, _) | Term.App(_, _)) if Sorts.arrow(sortOf(q)).isDefined && Term.closed(q) => q
      }.toList
      val apps =
        if functions.isEmpty then Nil else classes.keys.collect { case a: Term.App if Term.closed(a) => a }.toList
      for q <- functions; a <- apps if applicable(a, q) && done.add((a, q)) do
        require(done.size <= 100000, "too many applications of lambdas: are the terms well sorted?")
        more = true
        val applied = Term.app(q, a.args)
        equated += ((a, applied))
        recordFunctionEqualities = false // a derived equality gets no witness (see assertExtensionality)
        clauses += Array(lit(Formula.Eq(a.fun, q)).neg, lit(Formula.Eq(a, applied)))
        recordFunctionEqualities = true
    // An application equated with an integer term is one, and conversely (for the sharing).
    var changed = true
    while changed do
      changed = false
      for (a, b) <- equated if isInt(a) != isInt(b) do
        noteInts(a)
        noteInts(b)
        changed = true

  private def applicable(a: Term.App, q: Term): Boolean =
    a != q && // a term is not applied to itself: the lemma would equate it with its own application
      a.fun != q &&
      (Sorts.arrow(sortOf(q)) match
        case Some((params, _)) =>
          a.args.length <= params.length &&
          a.args.zip(params).forall((arg, sort) => sortOf(arg) == "?" || sortOf(arg) == sort)
        case None => false)

  /** Extensionality: for every equality atom between function-sorted terms, the
   *  lemma `f = g ∨ f(c) ≠ g(c)` with fresh constants `c` (the witnesses of the
   *  difference), repeated while the new equality is between functions (a
   *  curried result). So `unsat` and `sat` follow the usual semantics of
   *  functions on the equalities of the assertions; an equality between
   *  functions that the search derives, such as from the lemmas of
   *  [[applicationLemmas]], gets no witness.
   */
  private val extensionalityDone = scala.collection.mutable.HashSet.empty[Formula]

  /** The function equalities that may need a witness: the ones of encoded
   *  formulas — never the ones the application lemmas manufacture, whose
   *  cascade (a lemma's equality begets witnesses beget applications beget
   *  lemmas) is exactly what the doc below rules out.
   */
  private val functionEqualities = ArrayBuffer.empty[Formula]
  private var recordFunctionEqualities = true

  def assertExtensionality(): Unit = if anyFunctionEquality then
    val done = extensionalityDone // across solves: one witness pair per equality, ever
    var more = true
    while more do
      more = false
      for case eq @ Formula.Eq(f, g) <- functionEqualities.toList if !done(eq) do
        done += eq
        if !assertedEqualities(eq) then
          Sorts.arrow(sortOf(f)).orElse(Sorts.arrow(sortOf(g))) match
            case Some((params, _)) if Term.closed(f) && Term.closed(g) =>
              more = true
              val witnesses = params.map { sort =>
                val name = s"ext!${introduced.size}"
                introduced(name) = sort
                Term.Const(name)
              }
              clauses += Array(lit(eq), lit(Formula.Eq(applyAll(f, witnesses), applyAll(g, witnesses))).neg)
            case _ => ()

  /** Asserts, for every term of a datatype sort seen so far, that one of its
   *  testers holds; for a term of a finite sort, also for its selector terms,
   *  recursively (see the class documentation).
   */
  private val exhaustivenessSeen = scala.collection.mutable.HashSet.empty[Term]

  def assertExhaustiveness(): Unit =
    if datatypes.nonEmpty then
      val seen = exhaustivenessSeen // across solves: one tester disjunction per term
      val todo = ArrayBuffer.from(classes.keys.filter(t => datatypeNames(sortOf(t)) && Term.closed(t)))
      while todo.nonEmpty do
        val t = todo.remove(todo.length - 1)
        if seen.add(t) then
          val datatype = datatypes.find(_.name == sortOf(t)).get
          assert(
            Formula.Or(datatype.constructors.map(c => Formula.Pred(Term.App(Term.Const(c.tester), List(t))))),
            null
          )
          if finite(datatype.name) then
            for c <- datatype.constructors; (selector, sort) <- c.selectors if datatypeNames(sort.text) do
              todo += Term.App(Term.Const(selector), List(t))

  private val liaSolver = lia.Solver()
  private val intVars = HashMap.empty[Term, lia.Var] // `Int`-sorted constants and applications
  private val slacks = HashMap.empty[List[(BigInt, Term)], lia.Var] // linear combinations of them

  private val clauses = ArrayBuffer.empty[Array[Lit]]
  private val atoms = ArrayBuffer.empty[(EClassId, EClassId) | Null] // per variable, its UF atom `t = u`
  private val liaAtoms = ArrayBuffer.empty[LiaTheory.Atom | Null] // per variable, its LIA atom, if any
  private val lits = HashMap.empty[Formula, Lit]
  private val trueLit = freshVar() // a variable asserted true, for `True`
  clauses += Array(trueLit)
  eg.disequate(trueTerm, falseTerm, trueLit.index)

  private def freshVar(): Lit =
    atoms += null
    liaAtoms += null
    Lit(atoms.length - 1)

  /** The Boolean-sorted terms that are arguments of applications or sides of an
   *  equality (see [[decideBooleans]]), and their classes.
   */
  private val booleans = ArrayBuffer.empty[Term]
  private val booleanClasses = scala.collection.mutable.HashSet.empty[EClassId]

  private def boolean(term: Term): Unit =
    if sortOf(term) == "Bool" && Term.closed(term) && booleanClasses.add(classOf(term)) then booleans += term

  /** Whether some term is an integer, a function (a lambda or a partial
   *  application), or an equality between functions: the passes over all the
   *  terms of [[shareIntegers]], [[applicationLemmas]] and
   *  [[assertExtensionality]] are skipped otherwise (many queries are small).
   */
  private var anyInt = false
  private var anyFunction = false
  private var anyFunctionEquality = false

  def classOf(term: Term): EClassId = classes.getOrElseUpdate(
    term, {
      sortOf(term) match
        case "Int" | "Real" => anyInt = true
        case sort if sort.startsWith("(->") =>
          anyFunction |= term.isInstanceOf[Term.Lambda] || term.isInstanceOf[Term.App]
        case _ => ()
      classOfNew(term)
    }
  )

  private def classOfNew(term: Term): EClassId =
    term match
      case Term.Const(_) => eg.newClass()
      case Term.App(fun, args) =>
        args.foreach(boolean)
        eg.add(App(classOf(fun), ArraySeq.from(args.map(classOf))))
      case Term.Num(_) | Term.Linear(_, _) => eg.newClass() // a leaf for the e-graph, a variable for LIA (shared)
      case Term.Var(_) => eg.newClass() // a bound variable: a leaf shared by every lambda, never merged
      case Term.Lambda(sorts, body) => eg.add(App(lambdaSymbol(sorts.length), ArraySeq(classOf(body))))
      case Term.Bool(_) => eg.newClass() // a leaf, tied to its formula by `Pred` (see `encode`)

  /** A Boolean-sorted term that is an argument or a side of an equality is true
   *  or false: its atom `t = true` is decided by the SAT solver, and the UF
   *  theory merges `t` with `true` or with `false` accordingly — so that two
   *  Boolean arguments with the same truth value are equal (congruence), and a
   *  Boolean term's class has a truth value. The atom of a [[Term.Bool]] holds
   *  iff its formula does. A predicate application only used as an atom needs
   *  neither: its disequality with `true` is enough.
   */
  /** The array axioms, as lemmas over the finite product of the `store!`
   *  applications and the index terms of their array sort (`store` symbols come
   *  from the parser, one per array sort): for `s = store(a, i, v)`, the write
   *  is read back — `select(s, i) = v` — and for every index `j` of the sort,
   *  `i = j ∨ select(s, j) = select(a, j)`. Selection is application, so
   *  congruence and extensionality (the witnesses join the index set) come from
   *  the function machinery; each lemma is emitted once, ever. Nested arrays
   *  would need the pass and the extensionality pass iterated; the
   *  quantifier-free array logics keep them flat.
   */
  /** Applies an array to an index. A store application is already a whole
   *  array, so applying it must not flatten into the store symbol — the
   *  higher-order merging of [[Term.app]] is for partial applications.
   */
  private def applyAll(f: Term, args: List[Term]): Term = args.foldLeft(f)(selectTerm)

  def clauseCount: Int = clauses.length

  def litOf(f: Formula): Option[Lit] = lits.get(f)

  private def selectTerm(array: Term, index: Term): Term = array match
    case Term.App(Term.Const(n), _) if n.startsWith("store!") => Term.App(array, List(index))
    case _ => Term.app(array, List(index))

  private val arrayWritesDone = scala.collection.mutable.HashSet.empty[Term]
  private val arrayReadsDone = scala.collection.mutable.HashSet.empty[(Term, Term)]
  private lazy val anyStore = declarations.keys.exists(_.startsWith("store!"))

  def arrayLemmas(): Unit = if anyStore then
    val stores = classes.keys.collect {
      case s @ Term.App(Term.Const(name), List(_, _, _)) if name.startsWith("store!") => s
    }.toList
    val indices = scala.collection.mutable.HashMap.empty[String, scala.collection.mutable.LinkedHashSet[Term]]
    def index(sort: String, j: Term): Unit =
      indices.getOrElseUpdate(sort, scala.collection.mutable.LinkedHashSet.empty) += j
    for a <- classes.keys do
      a match
        case Term.App(f, List(j)) if Sorts.arrow(sortOf(f)).isDefined => index(sortOf(f), j)
        case _ => ()
    for s <- stores do index(sortOf(s), s.args(1))
    def equalAt(sort: String, l: Term, r: Term): Formula =
      if sort == "Int" || sort == "Real" then Formula.IntEq(l, r) else Formula.Eq(l, r)
    for case s @ Term.App(_, List(a, i, v)) <- stores do
      val sort = sortOf(s)
      val (indexSort :: _, elementSort) = Sorts.arrow(sort).get: @unchecked
      if arrayWritesDone.add(s) then clauses += Array(lit(equalAt(elementSort, selectTerm(s, i), v)))
      for j <- indices.getOrElse(sort, Nil) if j != i && arrayReadsDone.add((s, j)) do
        clauses += Array(
          lit(equalAt(indexSort, i, j)),
          lit(equalAt(elementSort, selectTerm(s, j), selectTerm(a, j)))
        )

  def decideBooleans(): Unit =
    var i = 0
    while i < booleans.length do
      lit(Formula.Pred(booleans(i)))
      i += 1

  private def atom(t: EClassId, u: EClassId): Lit =
    atoms += ((t, u))
    liaAtoms += null
    Lit(atoms.length - 1)

  private def bound(x: lia.Var, c: BigInt, strict: Boolean): Lit =
    atoms += null
    liaAtoms += LiaTheory.Atom.Bound(x, c, strict)
    Lit(atoms.length - 1)

  /** `lhs − rhs ≤ 0` (`< 0` when strict), as a bound on the combination
   *  `Σ a × t` (`+ c`) or on a lone term.
   */
  private def comparison(lhs: Term, rhs: Term, strict: Boolean): Lit =
    noteInts(lhs)
    noteInts(rhs)
    Term.linear(List((1, lhs), (-1, rhs))) match
      case Term.Num(v) => if if strict then v.signum < 0 else v.signum <= 0 then trueLit else trueLit.neg
      case Term.Linear(terms, c) => bound(slack(terms), -c, strict)
      case term => bound(intVar(term), 0, strict)

  /** The LIA variable of an atomic `Int` term. An application is a node of the
   *  e-graph as well, so that congruence relates it to other applications (a
   *  shared term, see [[shareIntegers]]).
   */
  private def intVar(term: Term): lia.Var =
    term match
      case Term.App(_, _) => classOf(term)
      case _ => ()
    intVars.getOrElseUpdate(term, liaSolver.newVar(integer = sortOf(term) != "Real"))

  /** The LIA variable standing for a linear combination of `Int` terms. */
  private def slack(terms: List[(BigInt, Term)]): lia.Var =
    slacks.getOrElseUpdate(
      terms,
      liaSolver.newSlack(terms.map((a, t) => (a, intVar(t))), integer = !terms.exists((_, t) => sortOf(t) == "Real"))
    )

  /** The literal standing for `formula`, defining it by clauses if needed. */
  private def lit(formula: Formula): Lit = lits.getOrElseUpdate(formula, encode(formula))

  private def encode(formula: Formula): Lit =
    import Formula.*
    formula match
      case True => trueLit
      case False => trueLit.neg
      case Not(arg) => lit(arg).neg
      case Eq(lhs, rhs) =>
        if lhs == rhs then trueLit
        else
          boolean(lhs)
          boolean(rhs)
          if sortOf(lhs).startsWith("(->") || sortOf(rhs).startsWith("(->") then
            anyFunctionEquality = true
            if recordFunctionEqualities then functionEqualities += formula
            formula match
              case Formula.Eq(l, r) if entangled(l, r) => entangledLambdas += ((formula, currentSelector))
              case _ => ()
          atom(classOf(lhs), classOf(rhs))
      case Pred(term) =>
        val e = atom(classOf(term), trueTerm)
        term match
          case Term.Bool(inner) => // `t = true` iff the formula holds
            val l = lit(inner)
            clauses += Array(e.neg, l)
            clauses += Array(l.neg, e)
          case _ => ()
        e
      case Le(lhs, rhs) => comparison(lhs, rhs, strict = false)
      case Less(lhs, rhs) => comparison(lhs, rhs, strict = true)
      case Implies(lhs, rhs) => lit(Or(List(Not(lhs), rhs)))
      case And(args) =>
        val v = freshVar()
        val as = args.map(lit)
        for a <- as do clauses += Array(v.neg, a)
        clauses += (v +: as.map(_.neg)).toArray
        v
      case Or(args) =>
        val v = freshVar()
        val as = args.map(lit)
        for a <- as do clauses += Array(v, a.neg)
        clauses += (v.neg +: as).toArray
        v
      case Iff(lhs, rhs) =>
        val v = freshVar()
        val (a, b) = (lit(lhs), lit(rhs))
        clauses += Array(v.neg, a.neg, b)
        clauses += Array(v.neg, a, b.neg)
        clauses += Array(v, a, b)
        clauses += Array(v, a.neg, b.neg)
        v
      case Ite(cond, thn, els) =>
        val v = freshVar()
        val (c, t, e) = (lit(cond), lit(thn), lit(els))
        clauses += Array(v.neg, c.neg, t)
        clauses += Array(v.neg, c, e)
        clauses += Array(v, c.neg, t.neg)
        clauses += Array(v, c, e.neg)
        v

  /** The assertions that are equality atoms: facts, which need no witness of
   *  extensionality.
   */
  private val assertedEqualities = scala.collection.mutable.HashSet.empty[Formula]

  /** The selector of the assertion being encoded, recorded with its entangled
   *  equalities.
   */
  private var currentSelector: Lit | Null = null

  /** Asserts `formula`, guarded by the selector literal `under` when it is not
   *  null: the clause holds only while the selector does — a script level's
   *  assertion (see [[Solver.push]]).
   */
  def assert(formula: Formula, under: Lit | Null): Unit =
    formula match
      case Formula.Eq(_, _) => assertedEqualities += formula
      case _ => ()
    currentSelector = under
    val l = lit(formula)
    currentSelector = null
    clauses += (if under == null then Array(l) else Array(under.nn.neg, l))

  /** A fresh selector literal for a script level. */
  def newSelector(): Lit = freshVar()

  /** The activation literal of a named assertion: the formula holds while the
   *  literal does, and the literal is assumed at every solve, so an `Unsat`
   *  core can name it (see [[core]]).
   */
  private val activations = HashMap.empty[Formula, Lit]

  def activate(formula: Formula): Lit =
    activations.getOrElseUpdate(
      formula, {
        val a = freshVar()
        clauses += Array(a.neg, lit(formula))
        a
      }
    )

  /** The failed assumptions of the last `Unsat`, for [[Solver.unsatCore]]. */
  def core(): Array[Lit] = sat.unsatCore

  /** Retires a popped level's selector: false for good, its clauses inert. */
  def retire(selector: Lit): Unit = clauses += Array(selector.neg)

  /** The theories and the SAT solver, alive across [[solve]]s: everything
   *  learned stays. Both theories are always attached — their atom buffers grow
   *  as terms arrive, possibly only after the first solve.
   */
  /** Whether some variable is an atom of both theories (a shared equality):
   *  only then does the combination need to remember impliers.
   */
  private var anySharedAtom = false

  private val theories = List(UfTheory(eg, atoms, trueTerm, falseTerm, booleanClasses), LiaTheory(liaSolver, liaAtoms))
  private val sat = cdclt.Solver(
    0,
    Theories(
      theories,
      v => if atoms(v) != null then theories.head else theories.last,
      v => atoms(v) != null && liaAtoms(v) != null, // only a shared equality needs its implier remembered
      () => anySharedAtom
    )
  )
  private var clauseCursor = 0
  private var satVars = 0 // variables handed to `sat` so far; `atoms` is the source of truth

  /** The model of the last `Sat`, taken while the engines still held the answer
   *  (the unwind below discards it).
   */
  var lastModel: Model | Null = null
  var produceModels: Boolean = true

  def solve(assumptions: ArrayBuffer[Lit], keepGoing: () => Boolean): cdclt.Solver.Result =
    liaSolver.keepGoing = keepGoing // the fuel: conflicts of the search, pivots, propagations and branches of LIA
    while satVars < atoms.length do
      sat.newVar()
      satVars += 1
    while clauseCursor < clauses.length do
      sat.addClause(clauses(clauseCursor))
      clauseCursor += 1
    lastModel = null
    val result = sat.solve(assumptions.toArray, keepGoing)
    result match
      case cdclt.Solver.Result.Sat(_) => if produceModels then lastModel = takeModel()
      case _ => ()
    sat.unwind() // the next assert must find the engines at level 0, not on the trail of this search
    result

  /** Builds the [[Model]] from the live engines: the value of every declared
   *  constant, and the graph of every declared first-order function from the
   *  applications the problem mentions. Uninterpreted sorts get an abstract
   *  element per e-class; a datatype class becomes its constructor term.
   */
  private def takeModel(): Model =
    val elements = HashMap.empty[(String, EClassId), Term] // sort and class to element
    val classValues = HashMap.empty[EClassId, BigInt] // the simplex value of an Int class, via any of its terms
    val realClassValues = HashMap.empty[EClassId, lia.Rational]
    for (t, x) <- intVars do
      classes.get(t).foreach { c =>
        if sortOf(t) == "Real" then realClassValues(eg.find(c)) = liaSolver.realValue(x)
        else classValues(eg.find(c)) = liaSolver.value(x)
      }
    def elementOf(sort: String, cls: EClassId): Term =
      val root = eg.find(cls)
      elements.getOrElseUpdate((sort, root), Term.Const(s"@$sort!${elements.count(_._1._1 == sort)}"))
    def valueOfClass(cls: EClassId, sort: String, seen: Set[EClassId]): Term =
      datatypes.find(_.name == sort) match
        case Some(datatype) =>
          val node = adt.map(_.node(eg, cls)).orNull
          if node == null || seen(eg.find(cls)) then elementOf(sort, cls)
          else
            val constructor = datatype.constructors.find(c => eg.find(classes(Term.Const(c.name))) == eg.find(node.fun))
            constructor match
              case Some(c) =>
                val args = node.args.toList.zip(c.selectors).map((a, s) =>
                  valueOfClass(a, s._2.text, seen + eg.find(cls))
                )
                if args.isEmpty then Term.Const(c.name) else Term.App(Term.Const(c.name), args)
              case None => elementOf(sort, cls)
        case None =>
          sort match
            case "Int" => Term.Num(classValues.getOrElse(eg.find(cls), BigInt(0)))
            case "Real" => Model.rationalTerm(realClassValues.getOrElse(eg.find(cls), lia.Rational.zero))
            case "Bool" => Term.boolean(if eg.find(cls) == eg.find(trueTerm) then Formula.True else Formula.False)
            case _ => elementOf(sort, cls)
    def valueOfTerm(t: Term, sort: String): Term =
      (t, sort) match
        case (Term.Num(_), _) => t // a literal argument of an application
        case (_, "Int") => intVars.get(t).map(x => Term.Num(liaSolver.value(x))).getOrElse(Term.Num(0))
        case (_, "Real") => intVars.get(t).map(x => Model.rationalTerm(liaSolver.realValue(x))).getOrElse(Term.Num(0))
        case _ =>
          classes.get(t) match
            case Some(cls) => valueOfClass(cls, sort, Set.empty)
            case None => if sort == "Bool" then Term.boolean(Formula.False) else elementOf(sort, eg.newClass())
    val constants = Map.newBuilder[String, Term]
    val functions = Map.newBuilder[String, Model.Function]
    for (name, (argSorts, sort)) <- declarations if !sort.startsWith("(->") do
      if argSorts.isEmpty then constants += name -> valueOfTerm(Term.Const(name), sort)
      else
        val rows = classes.keys.collect {
          case a @ Term.App(Term.Const(n), args) if n == name && args.length == argSorts.length =>
            (args.zip(argSorts).map(valueOfTerm), valueOfTerm(a, sort))
        }.toList
        val default = rows.headOption.map(_._2).getOrElse(valueOfTerm(Term.Const("@default"), sort))
        functions += name -> Model.Function(argSorts, sort, rows, default)
    Model(constants.result(), functions.result())

/** Several theories over disjoint sets of atoms, asserted and checked in turn,
 *  without exchange of equalities between them; `owner(v)` is the theory of the
 *  atom of variable `v`, for explanations.
 */
private final class Theories(
    theories: List[Theory],
    owner: cdclt.Var => Theory,
    shared: cdclt.Var => Boolean,
    anyShared: () => Boolean
) extends Theory:
  /** The theory that implied a literal: the atom of a shared equality belongs
   *  to both, and only the one that implied it can explain it. Only shared
   *  atoms are remembered; every other atom's owner is its implier.
   */
  private val implier = HashMap.empty[cdclt.Var, Theory]

  def assertLit(lit: Lit): Unit = theories.foreach(_.assertLit(lit))
  def propagate(): Theory.Result = combine(_.propagate())
  override def finalCheck(): Theory.Result = combine(_.finalCheck())
  def explain(lit: Lit): ArrayBuffer[Lit] = implier.getOrElse(lit.variable, owner(lit.variable)).explain(lit)
  def push(): Unit = theories.foreach(_.push())
  def pop(): Unit = theories.foreach(_.pop())

  private def combine(check: Theory => Theory.Result): Theory.Result = boundary:
    val results = theories.map(theory =>
      check(theory) match
        case Theory.Result.Ok(lits) => (theory, lits)
        case other => break(other)
    )
    if anyShared() then
      for (theory, lits) <- results; l <- lits if shared(l.variable) do implier(l.variable) = theory
    results.filter(_._2.nonEmpty) match
      case Nil => Theory.ok
      case (_, lits) :: Nil => Theory.Result.Ok(lits) // one theory spoke: its buffer passes through
      case several => Theory.Result.Ok(several.foldLeft(ArrayBuffer.empty[Lit])(_ ++= _._2))
