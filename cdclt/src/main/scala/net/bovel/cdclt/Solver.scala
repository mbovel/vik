package net.bovel.cdclt

import scala.collection.mutable.ArrayBuffer

import Lit.{neg, variable}

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
    if c.exists(l => c.contains(neg(l))) then return // a tautology
    val index = clauses.length
    clauses += c
    for l <- c do occurs(l) += index
    // The literals of `c` that are already false were propagated before `c` existed; examine it now.
    if examine(c, index) then unsat = true

  private def enqueue(lit: Lit, why: Int): Unit =
    value(lit) = 1
    value(neg(lit)) = -1
    level(variable(lit)) = decisionLevel
    reason(variable(lit)) = why
    trail += lit

  /** Propagates from clause `c` (index `index`) if it is unit, and returns
   *  whether it is false.
   */
  private def examine(c: Array[Lit], index: Int): Boolean =
    var unassigned = 0
    var unit = -1
    for l <- c do
      if value(l) > 0 then return false
      if value(l) == 0 then
        unassigned += 1
        unit = l
    if unassigned == 0 then true
    else
      if unassigned == 1 then enqueue(unit, index)
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
        for index <- occurs(neg(p)) do
          val c = clauses(index)
          if examine(c, index) then return c
      theory.propagate() match
        case Theory.Result.Conflict(reasons) => return reasons.map(neg).toArray
        case Theory.Result.Ok(implied) =>
          var progress = false
          for l <- implied do
            if value(l) == 0 then
              enqueue(l, FromTheory)
              progress = true
            else if value(l) < 0 then return reasonClause(l) // implied, yet false
          if !progress then return null
    null

  /** The clause explaining the assignment of `lit`: `lit` and the negations of
   *  the literals that implied it, which are all false.
   */
  private def reasonClause(lit: Lit): Array[Lit] =
    val why = reason(variable(lit))
    if why == FromTheory then (lit +: theory.explain(lit).map(neg)).toArray
    else clauses(why)

  /** Conflict analysis: from a false clause, the first unique implication point
   *  clause (its first literal is the one of the current level) and the level
   *  to backjump to.
   */
  private def analyze(conflict: Array[Lit]): (Array[Lit], Int) =
    val seen = new Array[Boolean](numVars)
    val learned = ArrayBuffer[Lit](-1) // slot for the literal of the current level
    var counter = 0 // seen variables of the current level not yet resolved away
    var clause = conflict
    var p = -1 // the literal resolved on, `-1` for the conflict clause itself
    var index = trail.length - 1
    while
      for q <- clause if q != p do
        val v = variable(q)
        if !seen(v) && level(v) > 0 then
          seen(v) = true
          if level(v) == decisionLevel then counter += 1 else learned += q
      // The most recently assigned seen literal of the current level.
      while !seen(variable(trail(index))) do index -= 1
      p = trail(index)
      index -= 1
      seen(variable(p)) = false
      counter -= 1
      counter > 0
    do clause = reasonClause(p)
    learned(0) = neg(p)
    val backjump = if learned.length == 1 then 0 else learned.tail.map(l => level(variable(l))).max
    (learned.toArray, backjump)

  private def backtrack(to: Int): Unit =
    while decisionLevel > to do
      val start = trailLim.remove(trailLim.length - 1)
      while trail.length > start do
        val lit = trail.remove(trail.length - 1)
        value(lit) = 0
        value(neg(lit)) = 0
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
      for l <- learned do occurs(l) += index
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
        val v = (0 until numVars).find(v => value(Lit(v)) == 0)
        v match
          case Some(v) => decide(v)
          case None =>
            theory.finalCheck() match
              case Theory.Result.Conflict(reasons) =>
                if !resolve(reasons.map(neg).toArray) then return Result.Unsat
              case Theory.Result.Ok(implied) if implied.exists(l => value(l) == 0) =>
                for l <- implied if value(l) == 0 do enqueue(l, FromTheory)
              case Theory.Result.Ok(implied) =>
                implied.find(l => value(l) < 0) match
                  case Some(l) => if !resolve(reasonClause(l)) then return Result.Unsat
                  case None => return Result.Sat(Array.tabulate(numVars)(v => value(Lit(v)) > 0))
    Result.Unsat
