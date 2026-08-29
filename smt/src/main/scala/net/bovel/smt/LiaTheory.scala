package net.bovel.smt

import scala.collection.mutable.ArrayBuffer

import net.bovel.cdclt.{Lit, Theory}
import net.bovel.lia

/** Linear integer arithmetic as a theory of the CDCL(T) solver: the atom of
 *  variable `v` is `x ≤ c` for `bounds(v) = (x, c)`, `x` a variable of the LIA
 *  solver standing for an `Int` term or a linear combination, or none when
 *  `bounds(v)` is `null`; the negation of the atom is `x ≥ c + 1`. A literal
 *  asserts its bound with the literal (its index) as reason; `propagate`
 *  decides the rational relaxation (bound propagation and simplex) and
 *  `finalCheck` the integers (branch and bound), both reporting a conflict as
 *  the literals whose bounds are inconsistent. Nothing is implied yet.
 */
final class LiaTheory(solver: lia.Solver, bounds: Array[(lia.Var, BigInt) | Null]) extends Theory:
  /** A conflict found while asserting, reported by the next `propagate`. */
  private var conflict: ArrayBuffer[Lit] | Null = null

  private def lits(reasons: Set[Int]): ArrayBuffer[Lit] = ArrayBuffer.from(reasons.map(Lit.fromIndex))

  def assertLit(lit: Lit): Unit =
    val v = lit.variable
    val bound = if v < bounds.length then bounds(v) else null
    if bound != null && conflict == null then
      val (x, c) = bound
      val result = if lit.positive then solver.assertUpper(x, c, lit.index) else solver.assertLower(x, c + 1, lit.index)
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
