package io.bovel.vik

import scala.collection.mutable.ArrayBuffer

import io.bovel.cdclt.{Lit, Theory, Var}
import io.bovel.egraph.{EClassId, EGraph}

/** Equality with uninterpreted functions over an [[io.bovel.egraph.EGraph]], as
 *  a theory of the CDCL(T) solver: the atom of variable `v` is `t = u` for
 *  `atoms(v) = (t, u)`, e-classes of `eg`, or none when `atoms(v)` is `null`.
 *  Terms are added to `eg` before solving.
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
    atoms: ArrayBuffer[(EClassId, EClassId) | Null],
    trueClass: EClassId,
    falseClass: EClassId,
    twoValued: EClassId => Boolean
) extends Theory:
  /** Atoms asserted so far, with the level marks for `pop`; `atoms` may grow
   *  between solves, so these grow with it.
   */
  private val assigned = ArrayBuffer.empty[Boolean]
  private val assignedTrail = ArrayBuffer.empty[Var]
  private val marks = ArrayBuffer.empty[Int]

  /** For each atom implied false, the disequality that implied it: its side in
   *  `t`'s class, its side in `u`'s class, and its reason.
   */
  private val impliedBy = ArrayBuffer.empty[(EClassId, EClassId, Int) | Null]

  /** The atoms of each id, as either of their sides. */
  private val atomsAt = ArrayBuffer.empty[ArrayBuffer[Var] | Null]

  /** The atoms to look at whatever the e-graph says: the new ones, and the ones a pop made unassigned, whose
   *  implication may have outlived their assignment.
   */
  private val again = ArrayBuffer.empty[Var]

  /** What the last propagation implied: the search does not take all of it when it finds a conflict. */
  private var lastImplied = ArrayBuffer.empty[Lit]

  private def grown(): Unit =
    while assigned.length < atoms.length do
      val v = assigned.length
      assigned += false
      impliedBy += null
      val atom = atoms(v)
      if atom != null then
        val (t, u) = atom
        watch(t, v)
        watch(u, v)
        again += v

  private def watch(id: EClassId, v: Var): Unit =
    while atomsAt.length <= id do atomsAt += null
    if atomsAt(id) == null then atomsAt(id) = ArrayBuffer.empty
    atomsAt(id).nn += v

  private def lits(reasons: ArrayBuffer[Int]): ArrayBuffer[Lit] = reasons.map(Lit.fromIndex)

  /** Whether anything reached the e-graph since the last scan of the atoms: a
   *  decision that concerns no atom costs nothing.
   */
  private var touched = true

  def assertLit(lit: Lit): Unit =
    grown()
    val v = lit.variable
    val atom = if v < atoms.length then atoms(v) else null
    if atom != null then
      touched = true
      val (t, u) = atom
      if lit.positive then eg.merge(t, u, lit.index)
      else if u == trueClass && twoValued(t) then eg.merge(t, falseClass, lit.index)
      else eg.disequate(t, u, lit.index)
      assigned(v) = true
      assignedTrail += v

  def propagate(): Theory.Result =
    grown()
    if !touched then return Theory.ok
    if eg.touchedLimit < 0 then eg.touchedLimit = math.max(1024, atoms.length)
    eg.rebuild()
    eg.conflict match
      case Some(reasons) => Theory.Result.Conflict(lits(reasons))
      case None =>
        touched = false
        val implied = ArrayBuffer.empty[Lit]
        def look(v: Var): Unit =
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
        // Only the atoms of what changed: looking at every atom at every propagation was most of the time of
        // the large unrollings of a verification condition, where most of them never change. In order of
        // variable, as such a walk gave them.
        val ids = eg.takeTouched()
        if ids == null then
          var v = 0
          while v < atoms.length do
            look(v)
            v += 1
        else
          var i = 0
          while i < ids.length do
            val id = ids(i)
            if id < atomsAt.length then
              val at = atomsAt(id)
              if at != null then again ++= at
            i += 1
          val candidates = again.toArray
          java.util.Arrays.sort(candidates)
          i = 0
          while i < candidates.length do
            if i == 0 || candidates(i) != candidates(i - 1) then look(candidates(i))
            i += 1
          ids.clear()
        eg.touchedLimit = math.max(1024, atoms.length)
        again.clear()
        lastImplied = implied
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
    touched = true // undone merges: atoms may need re-implying
    eg.pop()
    val mark = marks.remove(marks.length - 1)
    while assignedTrail.length > mark do
      val v = assignedTrail.remove(assignedTrail.length - 1)
      assigned(v) = false
      again += v
    for l <- lastImplied do again += l.variable
    lastImplied = ArrayBuffer.empty
