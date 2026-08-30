package net.bovel.smt

import scala.collection.immutable.ArraySeq
import scala.collection.mutable.{ArrayBuffer, HashMap}
import scala.util.boundary
import scala.util.boundary.break

import net.bovel.cdclt
import net.bovel.cdclt.{Lit, Theory}
import net.bovel.egraph.{App, EClassId, EGraph, NoRules}
import net.bovel.lia

/** An SMT solver for quantifier-free formulas over equality with uninterpreted
 *  functions and linear integer arithmetic: `assert` formulas, `push` and `pop`
 *  levels of assertions, and `solve`. Each `solve` simplifies the assertions of
 *  all levels (see [[Simplifier]], unless `simplify` is false) and encodes them
 *  afresh into a [[cdclt.Solver]] (Tseitin encoding, one CDCL(T) variable per
 *  distinct atom and per compound subformula) with a [[UfTheory]] over an
 *  [[EGraph]] for the equalities and a [[LiaTheory]] over a [[lia.Solver]] for
 *  the bounds. The two theories do not exchange equalities yet: a term of sort
 *  `Int` under an uninterpreted function is not supported (the SMT-LIB front
 *  end rejects such declarations).
 */
final class Solver(simplify: Boolean = true):
  enum Result:
    case Sat
    case Unsat

  private val levels = ArrayBuffer(ArrayBuffer.empty[Formula])

  /** The declared function symbols (argument sorts, result sort) and datatypes.
   *  Declarations are optional for uninterpreted functions and integers, and
   *  needed for datatypes: they tell which terms are of a datatype sort.
   */
  private val declarations = HashMap.empty[String, (List[String], String)]
  private val datatypes = ArrayBuffer.empty[Datatype]

  def declare(name: String, argSorts: List[String], sort: String): Unit = declarations(name) = (argSorts, sort)

  /** Declares a datatype: its constructors, selectors and testers become known
   *  function symbols. Fields of sort `Int`, `Bool` or bit-vectors need an
   *  exchange of equalities between theories, which does not exist yet.
   */
  def declare(datatype: Datatype): Unit =
    for c <- datatype.constructors; (_, sort) <- c.selectors do
      if sort == "Bool" then
        throw UnsupportedOperationException(s"datatype ${datatype.name}: fields of sort Bool are not supported")
    datatypes += datatype
    for c <- datatype.constructors do
      declarations(c.name) = (c.selectors.map(_._2), datatype.name)
      declarations(c.tester) = (List(datatype.name), "Bool")
      for (selector, sort) <- c.selectors do declarations(selector) = (List(datatype.name), sort)

  def push(): Unit = levels += ArrayBuffer.empty

  def pop(): Unit =
    require(levels.length > 1, "pop without a matching push")
    levels.remove(levels.length - 1)

  def assert(formula: Formula): Unit = levels.last += formula

  def solve(): Result =
    val assertions = levels.flatten.toList
    val encoder = Encoder(declarations, datatypes.toList)
    val constructors = datatypes.flatMap(_.constructors.map(_.name)).toSet
    for formula <- (if simplify then Simplifier.simplify(assertions, constructors) else assertions) do
      encoder.assert(formula)
    encoder.instantiateLambdas()
    encoder.assertExhaustiveness()
    encoder.shareIntegers()
    encoder.solve() match
      case cdclt.Solver.Result.Sat(_) => Result.Sat
      case cdclt.Solver.Result.Unsat => Result.Unsat

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
  private val adt = if datatypes.isEmpty then None else Some(AdtRules())
  private val eg = EGraph(adt.getOrElse(NoRules))
  private val trueTerm = eg.newClass() // the class of Boolean-sorted terms that hold

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

  /** The sort of a term, from the declarations; `?` when unknown, and for a
   *  function symbol (a constant with argument sorts), which is not a value.
   */
  private def sortOf(term: Term): String = term match
    case Term.Const(name) => declarations.get(name).filter(_._1.isEmpty).map(_._2).getOrElse("?")
    case Term.App(Term.Const(name), _) => declarations.get(name).map(_._2).getOrElse("?")
    case Term.Num(_) | Term.Linear(_, _) => "Int"
    case _ => "?"

  private val datatypeNames = datatypes.map(_.name).toSet

  /** The datatypes with finitely many values: no constructor field of an
   *  uninterpreted sort or of an infinite (e.g. recursive) datatype, computed
   *  as a least fixpoint.
   */
  private val finite: Set[String] =
    var known = Set.empty[String]
    var changed = true
    while changed do
      val more = datatypes.filter(d =>
        !known(d.name) && d.constructors.forall(_.selectors.forall((_, s) => known(s)))
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

  private def isInt(t: Term): Boolean = Term.closed(t) &&
    (t match
      case Term.Num(_) | Term.Linear(_, _) => true
      case _ => sortOf(t) == "Int" || intTerms(t))

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
  def shareIntegers(): Unit =
    val pairs = scala.collection.mutable.LinkedHashSet.empty[(Term, Term)]
    def pair(a: Term, b: Term): Unit = if a != b && isInt(a) && isInt(b) && !pairs((b, a)) then pairs += ((a, b))
    val applications = classes.keys.collect { case a: Term.App => a }.toList.groupBy(_.fun)
    for (_, group) <- applications do
      for (a, i) <- group.zipWithIndex; b <- group.drop(i + 1) do
        pair(a, b)
        for (x, y) <- a.args.zip(b.args) do pair(x, y)
    for case Formula.Eq(a, b) <- lits.keys do pair(a, b)
    // A selector's applications meet the arguments of its constructor at its position (the selector rule).
    for d <- datatypes; c <- d.constructors; ((selector, _), i) <- c.selectors.zipWithIndex do
      for s <- applications.getOrElse(Term.Const(selector), Nil); k <- applications.getOrElse(Term.Const(c.name), Nil)
      do
        if k.args.length > i then pair(s, k.args(i))
    for (a, b) <- pairs do
      val e = lit(Formula.Eq(a, b))
      Term.linear(List((1, a), (-1, b))) match
        case Term.Num(v) => clauses += Array(if v.signum == 0 then e else e.neg) // two numerals
        case Term.Linear(terms, c) => liaAtoms(e.variable) = LiaTheory.Atom.Equal(slack(terms), -c)
        case difference => liaAtoms(e.variable) = LiaTheory.Atom.Equal(intVar(difference), 0)

  /** Beta-reduction of the redexes the search can make, as lemmas: for every
   *  closed application `f(args)` and closed lambda `l` other than `f`, with as
   *  many parameters, of the sorts of the arguments when known, the clause
   *  `f ≠ l ∨ f(args) = l(args)`, `l(args)` being the body instantiated
   *  ([[Term.instantiate]]); repeated for the terms this makes, to a fixpoint.
   *  It ends for well-sorted terms, whose reductions are finite (simply typed
   *  lambda terms normalize), and is bounded anyway. Syntactic redexes are
   *  reduced at construction ([[Term.app]]). Lambdas need nothing else:
   *  alpha-equivalent ones are the same term (de Bruijn indices), congruence
   *  relates those with equal bodies, and no atom is ever on an open term (see
   *  [[Term.instantiate]]).
   */
  def instantiateLambdas(): Unit =
    val done = scala.collection.mutable.HashSet.empty[(Term.App, Term.Lambda)]
    val equated = ArrayBuffer.empty[(Term, Term)]
    var more = true
    while more do
      more = false
      val lambdas = classes.keys.collect { case l: Term.Lambda if Term.closed(l) => l }.toList
      val apps = classes.keys.collect { case a: Term.App if Term.closed(a) => a }.toList
      for l <- lambdas; a <- apps if applicable(a, l) && done.add((a, l)) do
        require(done.size <= 100000, "too many lambda instantiations: are the lambdas well sorted?")
        more = true
        val instance = Term.instantiate(l, a.args)
        equated += ((a, instance))
        clauses += Array(lit(Formula.Eq(a.fun, l)).neg, lit(Formula.Eq(a, instance)))
    // An application equated with an integer term is one, and conversely (for the sharing).
    var changed = true
    while changed do
      changed = false
      for (a, b) <- equated if isInt(a) != isInt(b) do
        noteInts(a)
        noteInts(b)
        changed = true

  private def applicable(a: Term.App, l: Term.Lambda): Boolean =
    a.fun != l && a.args.length == l.sorts.length &&
      a.args.zip(l.sorts).forall((arg, sort) => sortOf(arg) == "?" || sortOf(arg) == sort)

  /** Asserts, for every term of a datatype sort seen so far, that one of its
   *  testers holds; for a term of a finite sort, also for its selector terms,
   *  recursively (see the class documentation).
   */
  def assertExhaustiveness(): Unit =
    if datatypes.nonEmpty then
      val seen = scala.collection.mutable.HashSet.empty[Term]
      val todo = ArrayBuffer.from(classes.keys.filter(t => datatypeNames(sortOf(t)) && Term.closed(t)))
      while todo.nonEmpty do
        val t = todo.remove(todo.length - 1)
        if seen.add(t) then
          val datatype = datatypes.find(_.name == sortOf(t)).get
          assert(Formula.Or(datatype.constructors.map(c => Formula.Pred(Term.App(Term.Const(c.tester), List(t))))))
          if finite(datatype.name) then
            for c <- datatype.constructors; (selector, sort) <- c.selectors if datatypeNames(sort) do
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

  private def freshVar(): Lit =
    atoms += null
    liaAtoms += null
    Lit(atoms.length - 1)

  def classOf(term: Term): EClassId = classes.getOrElseUpdate(term, classOfNew(term))

  private def classOfNew(term: Term): EClassId =
    term match
      case Term.Const(_) => eg.newClass()
      case Term.App(fun, args) => eg.add(App(classOf(fun), ArraySeq.from(args.map(classOf))))
      case Term.Num(_) | Term.Linear(_, _) => eg.newClass() // a leaf for the e-graph, a variable for LIA (shared)
      case Term.Var(_) => eg.newClass() // a bound variable: a leaf shared by every lambda, never merged
      case Term.Lambda(sorts, body) => eg.add(App(lambdaSymbol(sorts.length), ArraySeq(classOf(body))))

  private def atom(t: EClassId, u: EClassId): Lit =
    atoms += ((t, u))
    liaAtoms += null
    Lit(atoms.length - 1)

  private def bound(x: lia.Var, c: BigInt): Lit =
    atoms += null
    liaAtoms += LiaTheory.Atom.Bound(x, c)
    Lit(atoms.length - 1)

  /** The LIA variable of an atomic `Int` term. An application is a node of the
   *  e-graph as well, so that congruence relates it to other applications (a
   *  shared term, see [[shareIntegers]]).
   */
  private def intVar(term: Term): lia.Var =
    term match
      case Term.App(_, _) => classOf(term)
      case _ => ()
    intVars.getOrElseUpdate(term, liaSolver.newVar())

  /** The LIA variable standing for a linear combination of `Int` terms. */
  private def slack(terms: List[(BigInt, Term)]): lia.Var =
    slacks.getOrElseUpdate(terms, liaSolver.newSlack(terms.map((a, t) => (a, intVar(t)))))

  /** The literal standing for `formula`, defining it by clauses if needed. */
  private def lit(formula: Formula): Lit = lits.getOrElseUpdate(formula, encode(formula))

  private def encode(formula: Formula): Lit =
    import Formula.*
    formula match
      case True => trueLit
      case False => trueLit.neg
      case Not(arg) => lit(arg).neg
      case Eq(lhs, rhs) => if lhs == rhs then trueLit else atom(classOf(lhs), classOf(rhs))
      case Pred(term) => atom(classOf(term), trueTerm)
      case Le(lhs, rhs) =>
        noteInts(lhs)
        noteInts(rhs)
        // `lhs − rhs ≤ 0`, as a bound on the combination `Σ a × t` (`+ c ≤ 0`) or on a lone term.
        Term.linear(List((1, lhs), (-1, rhs))) match
          case Term.Num(v) => if v.signum <= 0 then trueLit else trueLit.neg
          case Term.Linear(terms, c) => bound(slack(terms), -c)
          case term => bound(intVar(term), 0)
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

  def assert(formula: Formula): Unit = clauses += Array(lit(formula))

  def solve(): cdclt.Solver.Result =
    val ufAtoms = atoms.toArray
    val liaAtoms = this.liaAtoms.toArray
    val theories = List(
      Option.when(ufAtoms.exists(_ != null))(UfTheory(eg, ufAtoms)),
      Option.when(liaAtoms.exists(_ != null))(LiaTheory(liaSolver, liaAtoms))
    ).flatten
    val theory = theories match
      case Nil => cdclt.NoTheory
      case single :: Nil => single
      case _ => Theories(theories, v => if ufAtoms(v) != null then theories.head else theories.last)
    val solver = cdclt.Solver(atoms.length, theory)
    for c <- clauses do solver.addClause(c)
    solver.solve()

/** Several theories over disjoint sets of atoms, asserted and checked in turn,
 *  without exchange of equalities between them; `owner(v)` is the theory of the
 *  atom of variable `v`, for explanations.
 */
private final class Theories(theories: List[Theory], owner: cdclt.Var => Theory) extends Theory:
  def assertLit(lit: Lit): Unit = theories.foreach(_.assertLit(lit))
  def propagate(): Theory.Result = combine(_.propagate())
  override def finalCheck(): Theory.Result = combine(_.finalCheck())
  def explain(lit: Lit): ArrayBuffer[Lit] = owner(lit.variable).explain(lit)
  def push(): Unit = theories.foreach(_.push())
  def pop(): Unit = theories.foreach(_.pop())

  private def combine(check: Theory => Theory.Result): Theory.Result = boundary:
    val implied = ArrayBuffer.empty[Lit]
    for theory <- theories do
      check(theory) match
        case Theory.Result.Ok(lits) => implied ++= lits
        case conflict => break(conflict)
    Theory.Result.Ok(implied)
