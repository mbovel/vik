package io.bovel.cdclt

import scala.collection.mutable.ArrayBuffer

/** A CDCL(T) solver over `numVars` variables and the clauses given to
 *  [[Solver.addClause]], with `theory` deciding the atoms.
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

    /** The fuel is spent (see [[Solver.solve]]), or a theory gave up. */
    case Unknown

final class Solver(numVars: Int, theory: Theory = NoTheory, restartInterval: Int = 100):
  import Solver.Result

  private val Decision = -1
  private val FromTheory = -2

  /** The variables so far: `numVars`, plus one per [[newVar]]. */
  private var varCount = numVars

  /** Value of each literal: 1 true, -1 false, 0 unassigned. */
  private var value = new Array[Byte](2 * numVars)

  /** Decision level of each assigned variable. */
  private var level = new Array[Int](numVars)

  /** Why each assigned variable has its value: the index of the clause that
   *  propagated it, `Decision`, or `FromTheory` (explained lazily).
   */
  private var reason = new Array[Int](numVars)

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
  private val watches = ArrayBuffer.fill(2 * numVars)(ArrayBuffer.empty[Int])

  /** A conflict at level 0 (an empty clause, or a unit contradicting another).
   */
  private var unsat = false

  /** Variable move-to-front (Biere and Fröhlich 2015): the variables in a
   *  doubly linked list, `vmtfHead` first; a variable involved in a conflict
   *  moves to the front, and decisions take the first unassigned one. Each
   *  variable carries the time it last moved, so that `vmtfSearch`, the
   *  position from which the next decision searches, can be reset when a
   *  variable before it becomes unassigned. A greater stamp must mean earlier
   *  in the list for every pair, so the initial stamps decrease along the
   *  initial list (`-v`) and the bumped ones grow from 1: without that, two
   *  never-bumped variables compare equal and the reset can leave `vmtfSearch`
   *  past an unassigned variable — a decision never made, and a `Sat` on an
   *  incomplete assignment.
   */
  private var vmtfPrev = Array.tabulate(numVars)(v => v - 1)
  private var vmtfNext = Array.tabulate(numVars)(v => if v + 1 < numVars then v + 1 else -1)
  private var vmtfStamp = Array.tabulate(numVars)(v => -v.toLong)
  private var vmtfHead = if numVars > 0 then 0 else -1
  private var vmtfTail = numVars - 1
  private var vmtfSearch = vmtfHead
  private var vmtfTime = 0L

  /** The value each variable had when last unassigned (phase saving), false at
   *  first.
   */
  private var phase = new Array[Boolean](numVars)

  /** Whether the variable has been decided or assigned before: its first
   *  decision may take the theory's phase, the later ones keep the saved one (a
   *  theory phase at every decision loses phase saving's locality, measured ten
   *  times slower on an unsat bit-vector file).
   */
  private var seen = new Array[Boolean](numVars)

  /** How many variables there are: the solver adds some itself for a
   *  [[Theory.Result.Split]].
   */
  def vars: Int = varCount

  /** Adds a variable — unassigned, false phase, last in the decision order —
   *  and returns it. Solving between two [[newVar]]s is fine: the arrays grow,
   *  the trail is untouched. The solver adds variables itself for a
   *  [[Theory.Result.Split]].
   */
  def newVar(): Var =
    val v = varCount
    varCount += 1
    if 2 * varCount > value.length then
      val n = math.max(4, 2 * value.length)
      value = java.util.Arrays.copyOf(value, n)
      level = java.util.Arrays.copyOf(level, n / 2)
      reason = java.util.Arrays.copyOf(reason, n / 2)
      phase = java.util.Arrays.copyOf(phase, n / 2)
      seen = java.util.Arrays.copyOf(seen, n / 2)
      vmtfPrev = java.util.Arrays.copyOf(vmtfPrev, n / 2)
      vmtfNext = java.util.Arrays.copyOf(vmtfNext, n / 2)
      vmtfStamp = java.util.Arrays.copyOf(vmtfStamp, n / 2)
    watches += ArrayBuffer.empty
    watches += ArrayBuffer.empty
    vmtfPrev(v) = vmtfTail
    vmtfNext(v) = -1
    vmtfStamp(v) = -v.toLong // last in the order: the smallest stamp so far
    if vmtfTail >= 0 then vmtfNext(vmtfTail) = v else vmtfHead = v
    vmtfTail = v
    if vmtfSearch < 0 then vmtfSearch = v
    v

  /** Conflicts so far, and the count at which to restart next (Luby sequence
   *  times 100).
   */
  private var conflicts = 0L
  private var decisions = 0L
  private var restarts = 0

  /** Statistics of the search so far. */
  def numConflicts: Long = conflicts
  def numDecisions: Long = decisions

  /** Literals assigned, and clauses visited by Boolean propagation (a watcher,
   *  or a literal scanned for a new watch): the work between two decisions,
   *  which a fuel counts besides them (see `io.bovel.vik.Solver.spent`).
   */
  def numPropagations: Long = propagations
  def numClauseVisits: Long = clauseVisits
  private var propagations = 0L
  private var clauseVisits = 0L
  private var nextRestart = restartInterval.toLong

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

  def isUnassigned(l: Lit): Boolean = value(l.index) == 0

  /** Unwinds the search to level 0, the theories included: called between
   *  solves, so that what is added next lands in the persistent state, never on
   *  the trail.
   */
  def unwind(): Unit = backtrack(0)

  /** Adds a clause; only before the search. */
  def addClause(lits: Lit*): Unit = addClause(lits.toArray)

  def addClause(lits: Array[Lit]): Unit = addClause(lits, -1)

  /** Adds a clause; with `definedBy` a variable, one of its definition: see
   *  [[relevantOnly]].
   */
  def addClause(lits: Array[Lit], definedBy: Var): Unit =
    backtrack(0) // a clause added between two solves leaves the previous model
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
    if definedBy < 0 then roots += index
    else
      while definitions.length <= definedBy do definitions += null
      if definitions(definedBy) == null then definitions(definedBy) = ArrayBuffer.empty
      definitions(definedBy).nn += index
    if alive == 1 && value(c(0).index) == 0 then enqueue(c(0), index)
    if c.length >= 2 then
      watches(c(0).index) += index
      watches(c(1).index) += index

  private def enqueue(lit: Lit, why: Int): Unit =
    propagations += 1
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
        case Theory.Result.Unknown =>
          gaveUp = true
          return null
        case Theory.Result.Split(_) => throw IllegalStateException("a split outside the final check")
        case Theory.Result.Ok(implied) =>
          var progress = false
          var i = 0
          while i < implied.length do
            val l = implied(i)
            if value(l.index) == 0 then
              enqueue(l, FromTheory)
              progress = true
            else if value(l.index) < 0 then return theoryClause(l) // implied, yet false: a conflict
            i += 1
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
      clauseVisits += 1
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
        clauseVisits += k - 2
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

  /** The clause of the theory's explanation of `lit`: `lit`, then the negations
   *  of the literals that imply it. A reason clause when `lit` is assigned by
   *  the theory, a conflict clause when the theory implies `lit` while it is
   *  false.
   */
  private def theoryClause(lit: Lit): Array[Lit] = (lit +: theory.explain(lit).map(_.neg)).toArray

  /** The clause explaining the assignment of `lit`: `lit` at index 0, then the
   *  negations of the literals that implied it, which are all false.
   */
  private def reasonClause(lit: Lit): Array[Lit] =
    val why = reason(lit.variable)
    val clause = if why == FromTheory then theoryClause(lit) else clauses(why)
    clause

  /** Conflict analysis: from a false clause, the first unique implication point
   *  clause (its first literal is the one of the current level) and the level
   *  to backjump to.
   */
  private def analyze(conflict: Array[Lit]): (Array[Lit], Int) =
    val seen = new Array[Boolean](varCount)
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
      val mark = walkMarks.length - 4
      firstDone = walkMarks(mark)
      trailDone = walkMarks(mark + 1)
      definitionsDone = walkMarks(mark + 2)
      requiredDone = walkMarks(mark + 3)
      walkMarks.takeInPlace(mark)
      while trail.length > start do
        val lit = trail.remove(trail.length - 1)
        value(lit.index) = 0
        value(lit.neg.index) = 0
        phase(lit.variable) = lit.positive
        if vmtfSearch < 0 || vmtfStamp(lit.variable) > vmtfStamp(vmtfSearch) then vmtfSearch = lit.variable
      theory.pop()
    qhead = math.min(qhead, trail.length) // a literal enqueued at a surviving level stays to be propagated

  /** Moves `v` to the front of the move-to-front list. */
  private def bump(v: Var): Unit =
    if v != vmtfHead then
      // Unlink.
      val prev = vmtfPrev(v)
      val next = vmtfNext(v)
      vmtfNext(prev) = next
      if next >= 0 then vmtfPrev(next) = prev else vmtfTail = prev
      // Link in front.
      vmtfPrev(v) = -1
      vmtfNext(v) = vmtfHead
      vmtfPrev(vmtfHead) = v
      vmtfHead = v
    vmtfTime += 1
    vmtfStamp(v) = vmtfTime
    if value(Lit(v).index) == 0 then vmtfSearch = v

  /** Whether a `Sat` may leave variables unassigned: the search stops when the
   *  clauses that are not definitions, the definitions of the variables
   *  assigned and the [[require]]d variables are satisfied. Whatever is left is
   *  then extended by the theories' model: an atom takes its value there, and a
   *  variable its definition's. Each decision that nothing needs costs a round
   *  of the theories, and most of the atoms of an unrolling are behind a guard
   *  that is false. `theory.neededDecision` says what the theories' model
   *  cannot do without.
   */
  var relevantOnly = false

  private val roots = ArrayBuffer.empty[Int]
  private val definitions = ArrayBuffer.empty[ArrayBuffer[Int] | Null]
  private val required = ArrayBuffer.empty[Var]

  /** Has `v` decided even if nothing needs it: see [[relevantOnly]]. */
  def require(v: Var): Unit = required += v

  /** The clauses the walk starts with: the roots, then the definitions of the
   *  variables true or false before any decision, in the order the variables
   *  were made, which is the order the formula was written in. Those are roots
   *  as well, and the trail has them in the order of propagation instead, which
   *  turns a conjunction of nested `and`s around (`hash_sat_10_20` went from
   *  timing out to 0.2 s). Collected at the first decision of each [[solve]].
   */
  private val firstClauses = ArrayBuffer.empty[Int]
  private var firstCollected = false

  private def collectFirst(): Unit =
    firstClauses ++= roots
    val end = if trailLim.isEmpty then trail.length else trailLim(0)
    val defined = (0 until end).map(i => trail(i).variable).filter(u =>
      u < definitions.length && definitions(u) != null
    )
    for u <- defined.sorted do firstClauses ++= definitions(u).nn
    firstCollected = true

  /** How far the clauses to satisfy have been walked and found satisfied:
   *  [[firstClauses]], the trail with the definitions of the current literal,
   *  the required variables. All that stays true deeper in the search, so each
   *  level takes over from the one before, and these are put back to their
   *  value at the decision on backtracking.
   */
  private var firstDone = 0
  private var trailDone = 0
  private var definitionsDone = 0
  private var requiredDone = 0
  private val walkMarks = ArrayBuffer.empty[Int]

  private def openLevel(): Unit =
    trailLim += trail.length
    walkMarks += firstDone += trailDone += definitionsDone += requiredDone
    theory.push()

  /** The unassigned variable most recently in a conflict of the clause, other
   *  than `except`, if the clause is not satisfied, otherwise `-1`; `wanted` is
   *  then the literal that is its first choice, and may have changed otherwise.
   */
  private var wanted: Lit = Lit(0)
  private def unsatisfiedIn(clause: Array[Lit], except: Var = -1): Var =
    var best = -1
    var i = 0
    while i < clause.length do
      val l = clause(i)
      val x = value(l.index)
      if x > 0 then return -1
      if x == 0 && l.variable != except && (best < 0 || vmtfStamp(l.variable) > vmtfStamp(best)) then
        best = l.variable
        wanted = l
      i += 1
    best

  /** The variable to decide for `wanted` when it is a definition variable,
   *  taken down through its definition: rather than the variable of a
   *  disjunction, the operand that makes it true (or false for a conjunction),
   *  the way a justification walk goes down a formula. The variable then
   *  follows by propagation, and nested connectives cost no decision of their
   *  own: a disjunction of ten written as nine binary ones (`hash_sat`) was a
   *  chain of decisions before. The operand is decided the way the definition
   *  wants it, whatever its saved phase: with that phase, half the decisions
   *  satisfied nothing (measured 3× slower on `hash_sat`).
   */
  private var descended = false
  private def descend(v: Var): Var =
    var u = v
    var going = true
    while going do
      going = false
      val own = if u < definitions.length then definitions(u) else null
      if own != null then
        val need = wanted.neg
        var k = 0
        while k < own.length && !going do
          val clause = clauses(own(k))
          if clause.contains(need) then
            val before = wanted
            val operand = unsatisfiedIn(clause, except = u)
            if operand >= 0 then
              descended = true
              u = operand
              going = true
            else wanted = before
          k += 1
    u

  private def nextRelevant(): Var =
    if !firstCollected then collectFirst()
    while firstDone < firstClauses.length do
      val v = unsatisfiedIn(clauses(firstClauses(firstDone)))
      if v >= 0 then return descend(v)
      firstDone += 1
    while trailDone < trail.length do
      val u = trail(trailDone).variable
      val own = if u < definitions.length then definitions(u) else null
      if own != null then
        while definitionsDone < own.length do
          val v = unsatisfiedIn(clauses(own(definitionsDone)))
          if v >= 0 then return descend(v)
          definitionsDone += 1
      definitionsDone = 0
      trailDone += 1
    while requiredDone < required.length do
      val v = required(requiredDone)
      if value(Lit(v).index) == 0 then
        wanted = Lit(v, phase(v))
        return v
      requiredDone += 1
    -1

  /** The variable to decide next: the first unassigned of the move-to-front
   *  list, `-1` if none; or when only what is relevant is decided, one that has
   *  been in a conflict if there is one, and else what the clauses need.
   */
  private def nextDecision(): Var =
    while vmtfSearch >= 0 && value(Lit(vmtfSearch).index) != 0 do vmtfSearch = vmtfNext(vmtfSearch)
    if !relevantOnly || (vmtfSearch >= 0 && vmtfStamp(vmtfSearch) > 0) then
      wanted = if vmtfSearch >= 0 then Lit(vmtfSearch, phase(vmtfSearch)) else Lit(0)
      vmtfSearch
    else nextRelevant()

  private def decide(v: Var): Unit =
    decisions += 1
    val first = wanted.positive
    openLevel()
    val value =
      if descended then first
      else if seen(v) then phase(v)
      else theory.phase(v).getOrElse(first)
    descended = false
    seen(v) = true
    enqueue(Lit(v, value), Decision)

  /** Learns the clause of a conflict and backjumps, or reports level 0. Returns
   *  whether the search can go on. A theory conflict may involve no literal of
   *  the current level (a final check can find one after unrelated decisions):
   *  the search first backtracks to the level of its most recent literal, where
   *  the conflict already held; an empty one holds at level 0.
   */
  private def resolve(conflict: Array[Lit]): Boolean =
    if conflict.isEmpty then
      unsat = true // a theory found the clauses inconsistent by themselves
      return false
    val top = conflict.map(l => level(l.variable)).max
    if top < decisionLevel then backtrack(top)
    if decisionLevel == 0 then
      // Every literal of the conflict is a level-0 fact: the clauses alone are unsatisfiable, for good.
      // Said here, or the next solve, starting past the literals already propagated, never meets the
      // clause again and answers sat (issue 37).
      unsat = true
      false
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
        nextRestart = conflicts + luby(restarts) * restartInterval
        backtrack(0)
      true

  /** A theory gave up in `propagate`: the search stops. */
  private var gaveUp = false

  /** The assumptions of the last [[solve]] that made it `Unsat`: the ones in
   *  the implication cone of the first assumption found false — re-solving with
   *  just these stays `Unsat`. Empty when the clauses alone are `Unsat`, with
   *  or without the assumptions.
   */
  def unsatCore: Array[Lit] = lastCore

  private var lastCore: Array[Lit] = Array.empty

  /** MiniSat's analyzeFinal: `failed` is false under the assumptions before it,
   *  so the assumptions among the reasons of its negation, transitively, are a
   *  core.
   */
  private def analyzeCore(failed: Lit): Array[Lit] =
    val seen = new Array[Boolean](varCount)
    val core = ArrayBuffer(failed)
    seen(failed.variable) = true
    var index = trail.length - 1
    while index >= 0 do
      val p = trail(index)
      index -= 1
      if seen(p.variable) && level(p.variable) > 0 then
        reason(p.variable) match
          case Decision => core += p // an assumption: decisions above it are not seen
          case FromTheory => for l <- theoryClause(p).drop(1) do seen(l.variable) = true
          case clause => for l <- clauses(clause).drop(1) do seen(l.variable) = true
    core.toArray

  /** Solves; `keepGoing` is asked at every conflict and decision, and `Unknown`
   *  follows its first `false` (a budget of steps, a deadline, ...), or a
   *  theory giving up. Solving again is allowed: the trail unwinds first, the
   *  learned clauses stay.
   */
  def solve(keepGoing: () => Boolean = () => true): Result = solve(Array.empty[Lit], keepGoing)

  /** Solves with `assumptions` decided first, in order — `Unsat` then means
   *  unsatisfiable together with them (the clauses alone may be satisfiable). A
   *  restart or a backjump undoes assumptions like decisions; they are
   *  re-decided on the way down.
   */
  def solve(assumptions: Array[Lit], keepGoing: () => Boolean): Result =
    backtrack(0)
    firstDone = 0
    firstClauses.clear()
    firstCollected = false
    trailDone = 0
    definitionsDone = 0
    requiredDone = 0
    gaveUp = false
    lastCore = Array.empty
    if unsat then return Result.Unsat
    while true do
      val conflict = propagate()
      if gaveUp then return Result.Unknown
      if conflict != null then
        if !keepGoing() then return Result.Unknown
        if !resolve(conflict) then return Result.Unsat
      else if decisionLevel < assumptions.length then
        val a = assumptions(decisionLevel)
        if value(a.index) < 0 then
          lastCore = analyzeCore(a)
          return Result.Unsat // false under the assumptions before it
        else if value(a.index) > 0 then
          openLevel() // already implied: an empty level keeps the level-to-assumption count
        else
          if !keepGoing() then return Result.Unknown
          decisions += 1
          openLevel()
          enqueue(a, Decision)
      else
        val v = nextDecision()
        if v >= 0 then
          if !keepGoing() then return Result.Unknown
          decide(v)
        else
          theory.finalCheck() match
            case Theory.Result.Unknown => return Result.Unknown
            case Theory.Result.Conflict(reasons) =>
              if !resolve(reasons.map(_.neg).toArray) then return Result.Unsat
            case Theory.Result.Split(lit) =>
              if !keepGoing() then return Result.Unknown
              while varCount <= lit.variable do newVar()
              Predef.require(value(lit.index) == 0, s"split on the assigned literal $lit")
              decisions += 1
              openLevel()
              seen(lit.variable) = true
              enqueue(lit, Decision)
            case Theory.Result.Ok(implied) if implied.exists(l => value(l.index) == 0) =>
              for l <- implied if value(l.index) == 0 do enqueue(l, FromTheory)
            case Theory.Result.Ok(implied) =>
              implied.find(l => value(l.index) < 0) match
                case Some(l) => if !resolve(theoryClause(l)) then return Result.Unsat
                case None =>
                  val needed = if relevantOnly then theory.neededDecision() else Theory.nothingNeeded
                  if needed == Theory.everythingNeeded then relevantOnly = false
                  else if needed >= 0 then
                    wanted = Lit(needed, phase(needed))
                    decide(needed)
                  else return Result.Sat(Array.tabulate(varCount)(v => value(Lit(v).index) > 0))
    Result.Unsat
