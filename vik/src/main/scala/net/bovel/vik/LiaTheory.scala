package net.bovel.vik

import scala.collection.mutable.ArrayBuffer

import net.bovel.cdclt.{Lit, Theory, Var}
import net.bovel.lia

/** Linear integer arithmetic as a theory of the CDCL(T) solver: the atom of
 *  variable `v` is `atoms(v)`, on a variable `x` of the LIA solver standing for
 *  an `Int` term or a linear combination: `Bound(x, c)` is `x ≤ c`, its
 *  negation `x ≥ c + 1`; `Equal(x, c)` is `x = c`, its negation the clause of
 *  `Solver.shareIntegers` (an equality of shared terms is such an atom, of the
 *  UF theory as well); or none when `null`. A literal asserts its bounds with
 *  the literal (its index) as reason; `propagate` decides the rational
 *  relaxation (bound propagation and simplex) and `finalCheck` the integers and
 *  the disequalities (branch and bound), both reporting a conflict as the
 *  literals whose bounds are inconsistent. What the bounds already decide is
 *  implied rather than left to the search: an atom whose bounds force it, or
 *  forbid it, is propagated with those bounds as its explanation — which is
 *  what keeps the arrangement of the shared terms of a combination out of the
 *  decisions.
 */
final class LiaTheory(
    solver: lia.Solver,
    atoms: ArrayBuffer[LiaTheory.Atom | Null],
    guided: scala.collection.Set[Var] = Set.empty
) extends Theory:
  /** A conflict found while asserting, reported by the next `propagate`. */
  private var conflict: ArrayBuffer[Lit] | Null = null

  /** Atoms asserted so far, with the level marks for `pop`; an implied atom is
   *  asserted by the solver in turn, so this is also what `propagate` skips.
   *  `atoms` may grow between solves, so these grow with it.
   */
  private val assigned = ArrayBuffer.empty[Boolean]
  private val assignedTrail = ArrayBuffer.empty[Int]
  private val marks = ArrayBuffer.empty[Int]

  /** For each atom implied, the reasons of the bounds that implied it. */
  private val reasonsOf = ArrayBuffer.empty[Set[Int] | Null]

  private def grown(): Unit =
    while assigned.length < atoms.length do
      assigned += false
      reasonsOf += null

  private def lits(reasons: Set[Int]): ArrayBuffer[Lit] = ArrayBuffer.from(reasons.map(Lit.fromIndex))

  /** Whether any bound moved since the last check: a decision that concerns no
   *  atom costs nothing.
   */
  private var touched = true

  def assertLit(lit: Lit): Unit =
    grown()
    val v = lit.variable
    val atom = if v < atoms.length then atoms(v) else null
    if atom != null then
      touched = true
      assigned(v) = true
      assignedTrail += v
    if atom != null && conflict == null then
      val result = atom match
        case LiaTheory.Atom.Bound(x, c, strict) =>
          if lit.positive then solver.assertUpper(x, c, strict, lit.index)
          else solver.assertLower(x, c, !strict, lit.index) // not (x ≤ c) is x > c; not (x < c) is x ≥ c
        case LiaTheory.Atom.Equal(x, c) =>
          // False, the atom asserts nothing: the clause of `Solver.shareIntegers` makes the search take
          // one of the two sides, each a bound atom.
          if !lit.positive then lia.Solver.Result.Sat
          else
            solver.assertUpper(x, c, lit.index) match
              case lia.Solver.Result.Sat => solver.assertLower(x, c, lit.index)
              case unsat => unsat
      result match
        case lia.Solver.Result.Unsat(reasons) => conflict = lits(reasons)
        // Asserting a bound never runs the search, so it cannot be told to stop: Unknown is unreachable.
        case lia.Solver.Result.Sat | lia.Solver.Result.Unknown => ()

  def propagate(): Theory.Result =
    if !touched && conflict == null then Theory.ok
    else
      touched = false
      report(solver.checkRelaxation())

  override def finalCheck(): Theory.Result = report(solver.check())

  /** A bound atom as the relaxation stands to it, for its first decision, on a
   *  problem whose search is an arrangement of shared terms (`guided` holds the
   *  sides of its disequality clauses): the side of a clause the search takes
   *  is the one already true, and the other bounds start where the relaxation
   *  is. Nothing on a problem without shared terms, where the conflicts of an
   *  unsat search would then all wait for the final check (measured 200× slower
   *  on a bit-vector file); nothing past the first decision, where phase
   *  saving's locality matters more (measured on `Hash`).
   */
  override def phase(v: Var): Option[Boolean] =
    if guided.nonEmpty && v < atoms.length then
      atoms(v) match
        case LiaTheory.Atom.Bound(x, c, strict) =>
          val value = solver.value(x)
          Some(if strict then value < c else value <= c)
        case _ => None
    else None

  /** The atoms the bounds decide, and not yet assigned. */
  private def implied(): ArrayBuffer[Lit] =
    grown()
    val out = ArrayBuffer.empty[Lit]
    var v = 0
    while v < atoms.length do
      val atom = atoms(v)
      if atom != null && !assigned(v) then
        // Only the bounds: implying an equality of the arrangement was measured to be far worse (see the
        // results of 2026-08-31), because it merges classes that the search then has to take apart again.
        val decided = atom match
          case LiaTheory.Atom.Bound(x, c, false) => solver.decidesUpper(x, c)
          case _ => None
        decided match
          case Some((holds, reasons)) =>
            reasonsOf(v) = reasons
            out += Lit(v, holds)
          case None => ()
      v += 1
    out

  private def report(check: => lia.Solver.Result): Theory.Result =
    val pending = conflict
    if pending != null then
      conflict = null
      Theory.Result.Conflict(pending)
    else
      check match
        case lia.Solver.Result.Sat => Theory.Result.Ok(implied())
        case lia.Solver.Result.Unknown => Theory.Result.Unknown
        case lia.Solver.Result.Unsat(reasons) => Theory.Result.Conflict(lits(reasons))

  def explain(lit: Lit): ArrayBuffer[Lit] = lits(reasonsOf(lit.variable).nn)

  def push(): Unit =
    marks += assignedTrail.length
    solver.push()

  def pop(): Unit =
    touched = true // undone bounds: atoms may need re-implying
    conflict = null
    val mark = marks.remove(marks.length - 1)
    while assignedTrail.length > mark do assigned(assignedTrail.remove(assignedTrail.length - 1)) = false
    solver.pop()

object LiaTheory:
  enum Atom:
    case Bound(x: lia.Var, c: BigInt, strict: Boolean = false)
    case Equal(x: lia.Var, c: BigInt)
