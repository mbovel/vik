package net.bovel.smt

import scala.collection.mutable.ArrayBuffer

import net.bovel.cdclt.{Lit, Theory, Var}
import net.bovel.egraph.{EClassId, EGraph}
import net.bovel.egraph.EGraph.Event

/** Equality with uninterpreted functions over an [[EGraph]], as a theory of the
 *  CDCL(T) solver: the atom of variable `v` is `t = u` for `atoms(v) = (t, u)`,
 *  e-classes of `eg`, or none when `atoms(v)` is `null`. Terms are added to
 *  `eg` before solving; no class is created afterwards.
 *
 *  A true atom merges its sides, a false one adds a disequality, both with the
 *  literal (its index) as reason; `propagate` rebuilds the congruence closure,
 *  reports the violated disequality's explanation as a conflict, and implies
 *  the unassigned atoms whose sides became equal or disequal. Explanations come
 *  from `EGraph.explain`, whose reasons are the literals asserted.
 *
 *  To find the atoms to imply, each class keeps the list of the atoms with a
 *  side in it, as cvc5's equality engine does: the lists of the classes of both
 *  sides at construction, joined at each merge as the e-graph joins the
 *  classes. Following the e-graph's [[EGraph.events]], a merge checks the atoms
 *  of the absorbed class (sides now equal, or disequal through a disequality of
 *  the surviving class), and each pair of classes that became disequal
 *  (asserted, or derived by the e-graph from a merge) the atoms between the
 *  two, from the smaller list. Every atom whose sides are equal or disequal is
 *  thus implied at the `propagate` following the fact.
 *
 *  The lists are linked through arrays: entry `2v` stands for atom `v` on the
 *  side of `t` and entry `2v + 1` on the side of `u`; `head(c)` and `last(c)`
 *  bound the list of class `c` and `next` follows it, with -1 for none. A merge
 *  appends the absorbed class's list to the surviving one in constant time, and
 *  `pop` cuts it back (`moves` keeps the surviving class and its old end).
 */
final class UfTheory(eg: EGraph, atoms: Array[(EClassId, EClassId) | Null]) extends Theory:
  /** Atoms asserted so far. */
  private val assigned = new Array[Boolean](atoms.length)
  private val assignedTrail = ArrayBuffer.empty[Var]

  /** For each atom implied false, the disequality that implied it: its side in
   *  `t`'s class, its side in `u`'s class, and its reason.
   */
  private val impliedBy = new Array[(EClassId, EClassId, Int) | Null](atoms.length)

  /** The atom lists of the classes (see above) and the moves done at merges. */
  private val head = Array.fill(eg.classCount)(-1)
  private val last = Array.fill(eg.classCount)(-1)
  private val count = new Array[Int](eg.classCount)
  private val next = Array.fill(2 * atoms.length)(-1)
  private case class Move(into: EClassId, last: Int, count: Int)
  private val moves = ArrayBuffer.empty[Move]

  /** How many of `eg.events` have been processed. */
  private var processed = 0

  /** The state at each `push`. */
  private case class Mark(assigned: Int, moves: Int, processed: Int)
  private val marks = ArrayBuffer.empty[Mark]

  for v <- atoms.indices do
    val atom = atoms(v)
    if atom != null then
      val (t, u) = atom
      register(2 * v, eg.find(t))
      register(2 * v + 1, eg.find(u))

  private def register(entry: Int, cls: EClassId): Unit =
    if head(cls) == -1 then head(cls) = entry else next(last(cls)) = entry
    last(cls) = entry
    count(cls) += 1

  private def lits(reasons: ArrayBuffer[Int]): ArrayBuffer[Lit] = reasons.map(Lit.fromIndex)

  def assertLit(lit: Lit): Unit =
    val v = lit.variable
    val atom = if v < atoms.length then atoms(v) else null
    if atom != null then
      val (t, u) = atom
      if lit.positive then eg.merge(t, u, lit.index) else eg.disequate(t, u, lit.index)
      assigned(v) = true
      assignedTrail += v

  def propagate(): Theory.Result =
    eg.rebuild()
    eg.conflict match
      case Some((a, b, reason)) => Theory.Result.Conflict(lits(eg.explain(a, b) += reason))
      case None =>
        val implied = ArrayBuffer.empty[Lit]
        val events = eg.events
        while processed < events.length do
          events(processed) match
            case Event.Merged(from, into) =>
              if head(from) != -1 then
                var entry = head(from)
                while entry != -1 do
                  check(entry / 2, implied)
                  entry = next(entry)
                moves += Move(into, last(into), count(into))
                if head(into) == -1 then head(into) = head(from) else next(last(into)) = head(from)
                last(into) = last(from)
                count(into) += count(from)
                // The list of `from` is untouched, for when the merge is undone.
            case Event.Disequated(a, b) =>
              val ra = eg.find(a)
              val rb = eg.find(b)
              var entry = head(if count(a) <= count(b) then a else b)
              while entry != -1 do
                checkDisequal(entry / 2, ra, rb, implied)
                entry = next(entry)
          processed += 1
        // In variable order, as the atoms are, so that the search does not depend on the order of the events.
        implied.sortInPlaceBy(_.variable)
        Theory.Result.Ok(implied)

  /** Implies atom `v` if its sides are equal or disequal (possibly again: the
   *  solver skips implied literals that are already true).
   */
  private def check(v: Var, implied: ArrayBuffer[Lit]): Unit =
    if !assigned(v) then
      val (t, u) = atoms(v).nn
      if eg.find(t) == eg.find(u) then implied += Lit(v)
      else
        eg.disequality(t, u) match
          case Some(diseq) =>
            impliedBy(v) = diseq
            implied += Lit(v, false)
          case None => ()

  /** Implies atom `v` false if its sides are in the disequal classes `ra` and
   *  `rb`.
   */
  private def checkDisequal(v: Var, ra: EClassId, rb: EClassId, implied: ArrayBuffer[Lit]): Unit =
    if !assigned(v) then
      val (t, u) = atoms(v).nn
      val rt = eg.find(t)
      val ru = eg.find(u)
      if (rt == ra && ru == rb) || (rt == rb && ru == ra) then
        impliedBy(v) = eg.disequality(t, u).get
        implied += Lit(v, false)

  def explain(lit: Lit): ArrayBuffer[Lit] =
    val (t, u) = atoms(lit.variable).nn
    if lit.positive then lits(eg.explain(t, u))
    else
      val (s, s2, reason) = impliedBy(lit.variable).nn
      lits(eg.explain(t, s) ++= eg.explain(u, s2) += reason)

  def push(): Unit =
    eg.push()
    marks += Mark(assignedTrail.length, moves.length, processed)

  def pop(): Unit =
    eg.pop()
    val mark = marks.remove(marks.length - 1)
    while assignedTrail.length > mark.assigned do assigned(assignedTrail.remove(assignedTrail.length - 1)) = false
    while moves.length > mark.moves do
      val move = moves.remove(moves.length - 1)
      if move.last == -1 then head(move.into) = -1 else next(move.last) = -1
      last(move.into) = move.last
      count(move.into) = move.count
    // Events of the outer level processed inside the popped one are processed again.
    processed = mark.processed
