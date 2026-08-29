package net.bovel.smt

import scala.collection.immutable.ArraySeq
import scala.collection.mutable.{ArrayBuffer, HashMap}
import scala.util.boundary
import scala.util.boundary.break

import net.bovel.cdclt
import net.bovel.cdclt.{Lit, Theory}
import net.bovel.egraph.{App, EClassId, EGraph}
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

  def push(): Unit = levels += ArrayBuffer.empty

  def pop(): Unit =
    require(levels.length > 1, "pop without a matching push")
    levels.remove(levels.length - 1)

  def assert(formula: Formula): Unit = levels.last += formula

  def solve(): Result =
    val assertions = levels.flatten.toList
    val encoder = Encoder()
    for formula <- (if simplify then Simplifier.simplify(assertions) else assertions) do encoder.assert(formula)
    encoder.solve() match
      case cdclt.Solver.Result.Sat(_) => Result.Sat
      case cdclt.Solver.Result.Unsat => Result.Unsat

/** Encodes formulas into clauses over CDCL(T) variables, terms into e-classes
 *  of an e-graph and equalities into atoms of the UF theory, `Int` terms into
 *  variables of a LIA solver and `Le` atoms into bounds on them.
 */
private final class Encoder:
  private val eg = EGraph()
  private val classes = HashMap.empty[Term, EClassId]
  private val trueTerm = eg.newClass() // the class of Boolean-sorted terms that hold

  private val liaSolver = lia.Solver()
  private val intVars = HashMap.empty[Term, lia.Var] // `Int`-sorted constants and applications
  private val slacks = HashMap.empty[List[(BigInt, Term)], lia.Var] // linear combinations of them

  private val clauses = ArrayBuffer.empty[Array[Lit]]
  private val atoms = ArrayBuffer.empty[(EClassId, EClassId) | Null] // per variable, its UF atom `t = u`
  private val bounds = ArrayBuffer.empty[(lia.Var, BigInt) | Null] // per variable, its LIA atom `x ≤ c`
  private val lits = HashMap.empty[Formula, Lit]
  private val trueLit = freshVar() // a variable asserted true, for `True`
  clauses += Array(trueLit)

  private def freshVar(): Lit =
    atoms += null
    bounds += null
    Lit(atoms.length - 1)

  private def classOf(term: Term): EClassId = classes.getOrElseUpdate(
    term,
    term match
      case Term.Const(_) => eg.newClass()
      case Term.App(fun, args) => eg.add(App(classOf(fun), ArraySeq.from(args.map(classOf))))
      case Term.Num(_) | Term.Linear(_, _) => throw IllegalArgumentException(s"$term: an Int term compared by =")
  )

  private def atom(t: EClassId, u: EClassId): Lit =
    atoms += ((t, u))
    bounds += null
    Lit(atoms.length - 1)

  private def bound(x: lia.Var, c: BigInt): Lit =
    atoms += null
    bounds += ((x, c))
    Lit(atoms.length - 1)

  private def intVar(term: Term): lia.Var = intVars.getOrElseUpdate(term, liaSolver.newVar())

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
    val liaAtoms = bounds.toArray
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
