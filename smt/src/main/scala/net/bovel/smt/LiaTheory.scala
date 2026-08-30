package net.bovel.smt

import scala.collection.mutable.ArrayBuffer

import net.bovel.cdclt.{Lit, Theory}
import net.bovel.lia

/** Linear integer arithmetic as a theory of the CDCL(T) solver: the atom of
 *  variable `v` is `atoms(v)`, on a variable `x` of the LIA solver standing for
 *  an `Int` term or a linear combination: `Bound(x, c)` is `x ≤ c`, its
 *  negation `x ≥ c + 1`; `Equal(x, c)` is `x = c`, its negation `x ≠ c` (an
 *  equality of shared terms is such an atom, of the UF theory as well); or none
 *  when `null`. A literal asserts its bounds with the literal (its index) as
 *  reason; `propagate` decides the rational relaxation (bound propagation and
 *  simplex) and `finalCheck` the integers and the disequalities (branch and
 *  bound), both reporting a conflict as the literals whose bounds are
 *  inconsistent. Nothing is implied yet.
 */
final class LiaTheory(solver: lia.Solver, atoms: Array[LiaTheory.Atom | Null]) extends Theory:
  /** A conflict found while asserting, reported by the next `propagate`. */
  private var conflict: ArrayBuffer[Lit] | Null = null

  private def lits(reasons: Set[Int]): ArrayBuffer[Lit] = ArrayBuffer.from(reasons.map(Lit.fromIndex))

  def assertLit(lit: Lit): Unit =
    val v = lit.variable
    val atom = if v < atoms.length then atoms(v) else null
    if atom != null && conflict == null then
      val result = atom match
        case LiaTheory.Atom.Bound(x, c) =>
          if lit.positive then solver.assertUpper(x, c, lit.index) else solver.assertLower(x, c + 1, lit.index)
        case LiaTheory.Atom.Equal(x, c) =>
          if !lit.positive then solver.assertDistinct(x, c, lit.index)
          else
            solver.assertUpper(x, c, lit.index) match
              case lia.Solver.Result.Sat => solver.assertLower(x, c, lit.index)
              case unsat => unsat
      result match
        case lia.Solver.Result.Unsat(reasons) => conflict = lits(reasons)
        case lia.Solver.Result.Sat => ()

  def propagate(): Theory.Result = report(solver.checkRelaxation())

  override def finalCheck(): Theory.Result = report(solver.check())

  private def report(check: => lia.Solver.Result): Theory.Result =
    val pending = conflict
    if pending != null then
      conflict = null
      Theory.Result.Conflict(pending)
    else
      check match
        case lia.Solver.Result.Sat => Theory.ok
        case lia.Solver.Result.Unsat(reasons) => Theory.Result.Conflict(lits(reasons))

  def explain(lit: Lit): ArrayBuffer[Lit] = throw IllegalArgumentException("LiaTheory implies nothing")

  def push(): Unit = solver.push()

  def pop(): Unit =
    conflict = null
    solver.pop()

object LiaTheory:
  enum Atom:
    case Bound(x: lia.Var, c: BigInt)
    case Equal(x: lia.Var, c: BigInt)
