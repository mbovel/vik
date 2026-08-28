package net.bovel.cdclt

import scala.collection.mutable.ArrayBuffer

/** A CDCL(T) solver over `numVars` variables and the clauses given to
 *  [[addClause]], with `theory` deciding the atoms.
 *
 *  Deliberately unoptimized: unit propagation visits, for each literal that
 *  becomes false, every clause containing it (occurrence lists rather than
 *  watched literals), decisions take the first unassigned variable and set it
 *  to false, and there are no restarts, no clause deletion, no heuristics. What
 *  it does have is what makes it CDCL: after a conflict, the first unique
 *  implication point clause is learned and the solver backjumps to the level
 *  where it is unit.
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
 */
final class Solver(val numVars: Int, theory: Theory = NoTheory):
  enum Result:
    case Sat(model: Array[Boolean])
    case Unsat

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

  /** Indices of the clauses containing each literal. */
  private val occurs = Array.fill(2 * numVars)(ArrayBuffer.empty[Int])

  /** A conflict at level 0 (an empty clause, or a unit contradicting another).
   */
  private var unsat = false

  def decisionLevel: Int = trailLim.length

  /** Adds a clause; only before the search. */
  def addClause(lits: Lit*): Unit = addClause(lits.toArray)

  def addClause(lits: Array[Lit]): Unit =
    require(decisionLevel == 0, "clauses are added before solving")
    val c = lits.distinct
    if c.exists(l => c.contains(l.neg)) then return // a tautology
    val index = clauses.length
    clauses += c
    for l <- c do occurs(l.index) += index
    // The literals of `c` that are already false were propagated before `c` existed; examine it now.
    if examine(c, index) then unsat = true

  private def enqueue(lit: Lit, why: Int): Unit =
    value(lit.index) = 1
    value(lit.neg.index) = -1
    level(lit.variable) = decisionLevel
    reason(lit.variable) = why
    trail += lit

  /** Propagates from clause `c` (index `index`) if it is unit, moving the
   *  propagated literal to index 0, and returns whether `c` is false.
   */
  private def examine(c: Array[Lit], index: Int): Boolean =
    var unassigned = 0
    var unit = -1
    var i = 0
    while i < c.length do
      val l = c(i)
      if value(l.index) > 0 then return false
      if value(l.index) == 0 then
        unassigned += 1
        unit = i
      i += 1
    if unassigned == 0 then true
    else
      if unassigned == 1 then
        val l = c(unit)
        c(unit) = c(0)
        c(0) = l
        enqueue(l, index)
      false

  /** Boolean and theory propagation to a fixpoint. Returns a false clause when
   *  there is a conflict, `null` otherwise.
   */
  private def propagate(): Array[Lit] | Null =
    while true do
      while qhead < trail.length do
        val p = trail(qhead)
        qhead += 1
        theory.assertLit(p)
        for index <- occurs(p.neg.index) do
          val c = clauses(index)
          if examine(c, index) then return c
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
      counter -= 1
      counter > 0
    do
      clause = reasonClause(p)
      first = 1
    learned(0) = p.neg
    val backjump = if learned.length == 1 then 0 else learned.tail.map(l => level(l.variable)).max
    (learned.toArray, backjump)

  private def backtrack(to: Int): Unit =
    while decisionLevel > to do
      val start = trailLim.remove(trailLim.length - 1)
      while trail.length > start do
        val lit = trail.remove(trail.length - 1)
        value(lit.index) = 0
        value(lit.neg.index) = 0
      theory.pop()
    qhead = trail.length

  private def decide(v: Var): Unit =
    trailLim += trail.length
    theory.push()
    enqueue(Lit(v, false), Decision)

  /** Learns the clause of a conflict and backjumps, or reports level 0. Returns
   *  whether the search can go on.
   */
  private def resolve(conflict: Array[Lit]): Boolean =
    if decisionLevel == 0 then false
    else
      val (learned, to) = analyze(conflict)
      backtrack(to)
      val index = clauses.length
      clauses += learned
      for l <- learned do occurs(l.index) += index
      enqueue(learned(0), index) // unit at the backjump level
      true

  def solve(): Result =
    require(decisionLevel == 0, "solve is called once")
    if unsat then return Result.Unsat
    while true do
      val conflict = propagate()
      if conflict != null then
        if !resolve(conflict) then return Result.Unsat
      else
        val v = (0 until numVars).find(v => value(Lit(v).index) == 0)
        v match
          case Some(v) => decide(v)
          case None =>
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
