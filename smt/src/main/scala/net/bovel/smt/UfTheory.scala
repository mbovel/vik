package net.bovel.smt

import scala.collection.mutable.ArrayBuffer

import net.bovel.cdclt.{Lit, Theory, Var}
import net.bovel.egraph.{EClassId, EGraph}

/** Equality with uninterpreted functions over an [[EGraph]], as a theory of the
 *  CDCL(T) solver: the atom of variable `v` is `t = u` for `atoms(v) = (t, u)`,
 *  e-classes of `eg`, or none when `atoms(v)` is `null`. Terms are added to
 *  `eg` before solving.
 *
 *  A true atom merges its sides, a false one adds a disequality — or, for the
 *  atom `t = true` of a Boolean-sorted term in `twoValued`, merges `t` with
 *  `false`, so that such terms are two-valued — both with the literal (its
 *  index) as reason; `propagate` rebuilds the congruence closure, reports the
 *  violated disequality's explanation as a conflict, and implies every
 *  unassigned atom whose sides became equal (or disequal). Explanations come
 *  from `EGraph.explain`, whose reasons are the literals asserted.
 */
final class UfTheory(
    eg: EGraph,
    atoms: Array[(EClassId, EClassId) | Null],
    trueClass: EClassId,
    falseClass: EClassId,
    twoValued: EClassId => Boolean
) extends Theory:
  /** Atoms asserted so far, with the level marks for `pop`. */
  private val assigned = new Array[Boolean](atoms.length)
  private val assignedTrail = ArrayBuffer.empty[Var]
  private val marks = ArrayBuffer.empty[Int]

  /** For each atom implied false, the disequality that implied it: its side in
   *  `t`'s class, its side in `u`'s class, and its reason.
   */
  private val impliedBy = new Array[(EClassId, EClassId, Int) | Null](atoms.length)

  private def lits(reasons: ArrayBuffer[Int]): ArrayBuffer[Lit] = reasons.map(Lit.fromIndex)

  def assertLit(lit: Lit): Unit =
    val v = lit.variable
    val atom = if v < atoms.length then atoms(v) else null
    if atom != null then
      val (t, u) = atom
      if lit.positive then eg.merge(t, u, lit.index)
      else if u == trueClass && twoValued(t) then eg.merge(t, falseClass, lit.index)
      else eg.disequate(t, u, lit.index)
      assigned(v) = true
      assignedTrail += v

  def propagate(): Theory.Result =
    eg.rebuild()
    eg.conflict match
      case Some(reasons) => Theory.Result.Conflict(lits(reasons))
      case None =>
        val implied = ArrayBuffer.empty[Lit]
        var v = 0
        while v < atoms.length do
          val atom = atoms(v)
          if atom != null && !assigned(v) then
            val (t, u) = atom
            if eg.find(t) == eg.find(u) then implied += Lit(v)
            else
              eg.disequality(t, u) match
                case Some(diseq) =>
                  impliedBy(v) = diseq
                  implied += Lit(v, false)
                case None => ()
          v += 1
        Theory.Result.Ok(implied)

  override def finalCheck(): Theory.Result = propagate() match
    case Theory.Result.Ok(implied) if implied.isEmpty =>
      eg.finalCheck() match
        case Some(reasons) => Theory.Result.Conflict(lits(reasons))
        case None => Theory.ok
    case other => other

  def explain(lit: Lit): ArrayBuffer[Lit] =
    val (t, u) = atoms(lit.variable).nn
    if lit.positive then lits(eg.explain(t, u))
    else
      val (s, s2, reason) = impliedBy(lit.variable).nn
      lits(eg.explain(t, s) ++= eg.explain(u, s2) += reason)

  def push(): Unit =
    eg.push()
    marks += assignedTrail.length

  def pop(): Unit =
    eg.pop()
    val mark = marks.remove(marks.length - 1)
    while assignedTrail.length > mark do assigned(assignedTrail.remove(assignedTrail.length - 1)) = false
