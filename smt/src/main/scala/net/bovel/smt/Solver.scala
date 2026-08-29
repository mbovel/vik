package net.bovel.smt

import scala.collection.immutable.ArraySeq
import scala.collection.mutable.{ArrayBuffer, HashMap}

import net.bovel.cdclt
import net.bovel.cdclt.Lit
import net.bovel.egraph.{App, EClassId, EGraph}

/** An SMT solver for quantifier-free formulas over equality and uninterpreted
 *  functions: `assert` formulas, `push` and `pop` levels of assertions, and
 *  `solve`. Each `solve` simplifies the assertions of all levels (see
 *  [[Simplifier]], unless `simplify` is false) and encodes them afresh into a
 *  [[cdclt.Solver]] with a [[UfTheory]] over an [[EGraph]] (Tseitin encoding,
 *  one CDCL(T) variable per distinct atom and per compound subformula).
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
 *  of an e-graph, and equalities into atoms of the UF theory.
 */
private final class Encoder:
  private val eg = EGraph()
  private val classes = HashMap.empty[Term, EClassId]
  private val trueTerm = eg.newClass() // the class of Boolean-sorted terms that hold

  private val clauses = ArrayBuffer.empty[Array[Lit]]
  private val atoms = ArrayBuffer.empty[(EClassId, EClassId) | Null]
  private val lits = HashMap.empty[Formula, Lit]
  private val trueLit = freshVar() // a variable asserted true, for `True`
  clauses += Array(trueLit)

  private def freshVar(): Lit =
    atoms += null
    Lit(atoms.length - 1)

  private def classOf(term: Term): EClassId = classes.getOrElseUpdate(
    term,
    term match
      case Term.Const(_) => eg.newClass()
      case Term.App(fun, args) => eg.add(App(classOf(fun), ArraySeq.from(args.map(classOf))))
  )

  private def atom(t: EClassId, u: EClassId): Lit =
    atoms += ((t, u))
    Lit(atoms.length - 1)

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
    val solver = cdclt.Solver(atoms.length, UfTheory(eg, atoms.toArray))
    for c <- clauses do solver.addClause(c)
    solver.solve()
