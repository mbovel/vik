package net.bovel.cdclt

import scala.collection.mutable.ArrayBuffer

/** A CDCL(T) solver over `numVars` variables and the clauses given to
 *  [[addClause]], with `theory` deciding the atoms.
 *
 *  Unit propagation watches two literals per clause: a clause is visited only
 *  when one of its two watched literals becomes false, and then either another
 *  non-false literal takes over the watch, or the other watched literal is
 *  true, or the clause is unit (the other watched literal is propagated) or
 *  false (a conflict). Nothing is done on backtracking. Decisions take the
 *  unassigned variable most recently involved in a conflict (variable
 *  move-to-front) and give it the value it last had (phase saving, false at
 *  first). The search restarts from level 0 after a number of conflicts
 *  following the Luby sequence (times 100), keeping the learned clauses; there
 *  is no clause deletion. After a conflict, the first unique implication point
 *  clause is learned and the solver backjumps to the level where it is unit.
 *
 *  The trail is a stack of assigned literals with, per variable, its level and
 *  reason, in the style of CreuSAT (Skotåm 2022), whose proofs rest on these
 *  invariants: every literal is on the trail at most once, levels are
 *  non-decreasing along it, the reason clause of a propagated literal is
 *  "post-unit" (the literal, at index 0, is true and the others are false; a
 *  theory reason is explained lazily into such a clause), and a learned clause
 *  is a resolvent of clauses of the database, so the database stays
 *  equisatisfiable with the input. `Unsat` means a conflict at level 0, i.e.
 *  the empty clause by resolution; `Sat` means a complete assignment without
 *  conflict, which needs no completeness argument for propagation or decisions.
 *
 *  Watched literals are the first two of each clause of two or more literals;
 *  when propagation is complete, a false watched literal implies that the other
 *  one is true (assigned at the same level or before), so that backtracking
 *  never invalidates a watch.
 */
object Solver:
  enum Result:
    case Sat(model: Array[Boolean])
    case Unsat

final class Solver(val numVars: Int, theory: Theory = NoTheory):
  import Solver.Result

  private val Decision = -1
  private val FromTheory = -2

  /** Value of each literal: 1 true, -1 false, 0 unassigned. */
  private val value = new Array[Byte](2 * numVars)

  /** Decision level of each assigned variable. */
  private val level = new Array[Int](numVars)

  /** Why each assigned variable has its value: the index of the clause that
   *  propagated it, `Decision`, or `FromTheory` (explained lazily).
   */
  private val reason = new Array[Int](numVars)

  /** The assigned literals in order. */
  private val trail = ArrayBuffer.empty[Lit]

  /** Length of `trail` at each decision; the decision level is its length. */
  private val trailLim = ArrayBuffer.empty[Int]

  /** Literals of `trail` before `qhead` have been propagated and asserted to
   *  the theory.
   */
  private var qhead = 0

  private val clauses = ArrayBuffer.empty[Array[Lit]]

  /** Indices of the clauses watching each literal (their literal 0 or 1). */
  private val watches = Array.fill(2 * numVars)(ArrayBuffer.empty[Int])

  /** A conflict at level 0 (an empty clause, or a unit contradicting another).
   */
  private var unsat = false

  /** Variable move-to-front (Biere and Fröhlich 2015): the variables in a
   *  doubly linked list, `vmtfHead` first; a variable involved in a conflict
   *  moves to the front, and decisions take the first unassigned one. Each
   *  variable carries the time it last moved, so that `vmtfSearch`, the
   *  position from which the next decision searches, can be reset when a
   *  variable before it becomes unassigned.
   */
  private val vmtfPrev = Array.tabulate(numVars)(v => v - 1)
  private val vmtfNext = Array.tabulate(numVars)(v => if v + 1 < numVars then v + 1 else -1)
  private val vmtfStamp = new Array[Long](numVars)
  private var vmtfHead = if numVars > 0 then 0 else -1
  private var vmtfSearch = vmtfHead
  private var vmtfTime = 0L

  /** The value each variable had when last unassigned (phase saving), false at
   *  first.
   */
  private val phase = new Array[Boolean](numVars)

  /** Conflicts so far, and the count at which to restart next (Luby sequence
   *  times 100).
   */
  private var conflicts = 0L
  private var restarts = 0
  private var nextRestart = 100L

  /** The `i`-th term (from 0) of the Luby sequence 1, 1, 2, 1, 1, 2, 4, 1, 1,
   *  2, 1, 1, 2, 4, 8, ...
   */
  private def luby(i: Int): Long =
    var size = 1
    var seq = 0
    while size < i + 1 do
      seq += 1
      size = 2 * size + 1
    var x = i
    while size - 1 != x do
      size = (size - 1) >> 1
      seq -= 1
      x = x % size
    1L << seq

  def decisionLevel: Int = trailLim.length

  /** Adds a clause; only before the search. */
  def addClause(lits: Lit*): Unit = addClause(lits.toArray)

  def addClause(lits: Array[Lit]): Unit =
    require(decisionLevel == 0, "clauses are added before solving")
    val c = lits.distinct
    if c.exists(l => c.contains(l.neg)) then return // a tautology
    // Literals false at level 0 are false for good: move the others first, and treat the clause accordingly.
    val alive = c.count(l => value(l.index) >= 0)
    if alive == 0 then
      unsat = true
      return
    var i = 0
    var j = 0
    while j < alive do
      if value(c(i).index) >= 0 then
        val l = c(i)
        c(i) = c(j)
        c(j) = l
        j += 1
      i += 1
    val index = clauses.length
    clauses += c
    if alive == 1 && value(c(0).index) == 0 then enqueue(c(0), index)
    if c.length >= 2 then
      watches(c(0).index) += index
      watches(c(1).index) += index

  private def enqueue(lit: Lit, why: Int): Unit =
    value(lit.index) = 1
    value(lit.neg.index) = -1
    level(lit.variable) = decisionLevel
    reason(lit.variable) = why
    trail += lit

  /** Boolean and theory propagation to a fixpoint. Returns a false clause when
   *  there is a conflict, `null` otherwise.
   */
  private def propagate(): Array[Lit] | Null =
    while true do
      while qhead < trail.length do
        val p = trail(qhead)
        qhead += 1
        theory.assertLit(p)
        val conflict = visitWatchers(p.neg)
        if conflict != null then return conflict
      theory.propagate() match
        case Theory.Result.Conflict(reasons) => return reasons.map(_.neg).toArray
        case Theory.Result.Ok(implied) =>
          var progress = false
          for l <- implied do
            if value(l.index) == 0 then
              enqueue(l, FromTheory)
              progress = true
            else if value(l.index) < 0 then return reasonClause(l) // implied, yet false
          if !progress then return null
    null

  /** Visits the clauses watching `falseLit`, which just became false: each one
   *  either moves that watch to a non-false literal, is satisfied by its other
   *  watched literal, propagates it (unit), or is a conflict. Returns the
   *  conflicting clause, `null` if none.
   */
  private def visitWatchers(falseLit: Lit): Array[Lit] | Null =
    val ws = watches(falseLit.index)
    var i = 0
    var j = 0 // watchers kept so far
    var conflict: Array[Lit] | Null = null
    while i < ws.length && conflict == null do
      val index = ws(i)
      i += 1
      val c = clauses(index)
      if c(0) == falseLit then // the false watched literal goes to index 1
        c(0) = c(1)
        c(1) = falseLit
      val other = c(0)
      if value(other.index) > 0 then
        ws(j) = index
        j += 1
      else
        var k = 2
        while k < c.length && value(c(k).index) < 0 do k += 1
        if k < c.length then // watch that literal instead
          c(1) = c(k)
          c(k) = falseLit
          watches(c(1).index) += index
        else
          ws(j) = index
          j += 1
          if value(other.index) < 0 then conflict = c else enqueue(other, index)
    while i < ws.length do // after a conflict, keep the remaining watchers
      ws(j) = ws(i)
      i += 1
      j += 1
    ws.takeInPlace(j)
    conflict

  /** The clause explaining the assignment of `lit`: `lit` at index 0, then the
   *  negations of the literals that implied it, which are all false.
   */
  private def reasonClause(lit: Lit): Array[Lit] =
    val why = reason(lit.variable)
    if why == FromTheory then (lit +: theory.explain(lit).map(_.neg)).toArray
    else clauses(why)

  /** Conflict analysis: from a false clause, the first unique implication point
   *  clause (its first literal is the one of the current level) and the level
   *  to backjump to.
   */
  private def analyze(conflict: Array[Lit]): (Array[Lit], Int) =
    val seen = new Array[Boolean](numVars)
    val learned = ArrayBuffer[Lit](Lit(0)) // slot for the literal of the current level
    var counter = 0 // seen variables of the current level not yet resolved away
    var clause = conflict
    var first = 0 // literals of `clause` to resolve in: all of the conflict clause, then all but the implied one
    var p: Lit = Lit(0) // the literal resolved on (set before use)
    var index = trail.length - 1
    while
      for i <- first until clause.length do
        val q = clause(i)
        val v = q.variable
        if !seen(v) && level(v) > 0 then
          seen(v) = true
          if level(v) == decisionLevel then counter += 1 else learned += q
      // The most recently assigned seen literal of the current level.
      while !seen(trail(index).variable) do index -= 1
      p = trail(index)
      index -= 1
      seen(p.variable) = false
      bump(p.variable)
      counter -= 1
      counter > 0
    do
      clause = reasonClause(p)
      first = 1
    learned(0) = p.neg
    for l <- learned.drop(1) do bump(l.variable)
    val backjump = if learned.length == 1 then 0 else learned.tail.map(l => level(l.variable)).max
    (learned.toArray, backjump)

  private def backtrack(to: Int): Unit =
    while decisionLevel > to do
      val start = trailLim.remove(trailLim.length - 1)
      while trail.length > start do
        val lit = trail.remove(trail.length - 1)
        value(lit.index) = 0
        value(lit.neg.index) = 0
        phase(lit.variable) = lit.positive
        if vmtfSearch < 0 || vmtfStamp(lit.variable) > vmtfStamp(vmtfSearch) then vmtfSearch = lit.variable
      theory.pop()
    qhead = trail.length

  /** Moves `v` to the front of the move-to-front list. */
  private def bump(v: Var): Unit =
    if v != vmtfHead then
      // Unlink.
      val prev = vmtfPrev(v)
      val next = vmtfNext(v)
      vmtfNext(prev) = next
      if next >= 0 then vmtfPrev(next) = prev
      // Link in front.
      vmtfPrev(v) = -1
      vmtfNext(v) = vmtfHead
      vmtfPrev(vmtfHead) = v
      vmtfHead = v
    vmtfTime += 1
    vmtfStamp(v) = vmtfTime
    if value(Lit(v).index) == 0 then vmtfSearch = v

  /** The first unassigned variable of the move-to-front list, `-1` if none. */
  private def nextDecision(): Var =
    while vmtfSearch >= 0 && value(Lit(vmtfSearch).index) != 0 do vmtfSearch = vmtfNext(vmtfSearch)
    vmtfSearch

  private def decide(v: Var): Unit =
    trailLim += trail.length
    theory.push()
    enqueue(Lit(v, phase(v)), Decision)

  /** Learns the clause of a conflict and backjumps, or reports level 0. Returns
   *  whether the search can go on.
   */
  private def resolve(conflict: Array[Lit]): Boolean =
    if decisionLevel == 0 then false
    else
      conflicts += 1
      val (learned, to) = analyze(conflict)
      backtrack(to)
      val index = clauses.length
      clauses += learned
      if learned.length >= 2 then
        // Watch the asserting literal and a literal of the backjump level, the last to become unassigned.
        var k = 2
        while k < learned.length do
          if level(learned(k).variable) > level(learned(1).variable) then
            val l = learned(1)
            learned(1) = learned(k)
            learned(k) = l
          k += 1
        watches(learned(0).index) += index
        watches(learned(1).index) += index
      enqueue(learned(0), index) // unit at the backjump level
      if conflicts >= nextRestart then
        restarts += 1
        nextRestart = conflicts + luby(restarts) * 100
        backtrack(0)
      true

  def solve(): Result =
    require(decisionLevel == 0, "solve is called once")
    if unsat then return Result.Unsat
    while true do
      val conflict = propagate()
      if conflict != null then
        if !resolve(conflict) then return Result.Unsat
      else
        val v = nextDecision()
        if v >= 0 then decide(v)
        else
          theory.finalCheck() match
            case Theory.Result.Conflict(reasons) =>
              if !resolve(reasons.map(_.neg).toArray) then return Result.Unsat
            case Theory.Result.Ok(implied) if implied.exists(l => value(l.index) == 0) =>
              for l <- implied if value(l.index) == 0 do enqueue(l, FromTheory)
            case Theory.Result.Ok(implied) =>
              implied.find(l => value(l.index) < 0) match
                case Some(l) => if !resolve(reasonClause(l)) then return Result.Unsat
                case None => return Result.Sat(Array.tabulate(numVars)(v => value(Lit(v).index) > 0))
    Result.Unsat
