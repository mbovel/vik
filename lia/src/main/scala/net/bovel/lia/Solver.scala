package net.bovel.lia

import scala.collection.mutable.{ArrayBuffer, HashMap, HashSet}
import scala.util.boundary
import scala.util.boundary.break

/** A variable of the [[Solver]]: an index in `0 until numVars`. */
type Var = Int

object Solver:
  enum Result:
    case Sat

    /** No solution: the bounds asserted with these reasons are inconsistent by
     *  themselves.
     */
    case Unsat(reasons: Set[Int])

  /** A bound on a variable: its value (an integer) and why it holds: the reason
   *  it was asserted with, or the bounds it was derived from (`premises`, see
   *  `propagateBounds`). The reasons of a derived bound, the union of those of
   *  its premises, are collected only when a conflict needs them: derivations
   *  are many, conflicts few.
   */
  private final class Bound(val value: Rational, val reason: Int, val premises: Array[Bound]):
    def reasons: Set[Int] =
      val literals = Set.newBuilder[Int]
      val seen = scala.collection.mutable.HashSet.empty[Bound] // premises are shared: a graph, not a tree
      val todo = ArrayBuffer(this)
      while todo.nonEmpty do
        val bound = todo.remove(todo.length - 1)
        if seen.add(bound) then
          if bound.premises.isEmpty then literals += bound.reason
          else todo ++= bound.premises
      literals.result()

  /** A bound replaced inside a level: the variable, the side, the old bound. */
  private final case class Undo(x: Var, isUpper: Boolean, old: Bound | Null)

  /** An open branch of branch and bound on `x`: its two bounds, its reason, and
   *  the explanation of its failed left side once on the right side.
   */
  private final case class Branch(
      x: Var,
      floor: BigInt,
      ceil: BigInt,
      reason: Int,
      because: Set[Int], // the reasons of the disequality the branch decides, if any: part of its explanation
      var left: Set[Int] | Null = null
  )

/** A solver for conjunctions of bounds on integer linear expressions
 *  (quantifier-free linear integer arithmetic), after Dutertre and de Moura, "A
 *  Fast Linear-Arithmetic Solver for DPLL(T)" (CAV 2006): a general simplex
 *  over the rationals whose tableau is set up once and never backtracked, with
 *  bounds as the only assertions (the only state that [[pop]] undoes), then
 *  branch and bound for integrality.
 *
 *  A variable is either an integer unknown ([[newVar]]) or stands for a linear
 *  expression over earlier variables ([[newSlack]]), integer-valued as well.
 *  Bounds are asserted with an `Int` reason (for a CDCL(T) solver, a literal)
 *  and are tightened to integers, which for an expression means dividing by the
 *  gcd of its coefficients (`2x + 2y ≤ 3` gives `x + y ≤ 1`); an
 *  [[Solver.Result.Unsat]] names the reasons of some bounds that are
 *  inconsistent by themselves.
 *
 *  Invariants between calls:
 *    - each row holds: the value of its basic variable is the row's combination
 *      of the values of the non-basic variables it mentions, and rows mention
 *      non-basic variables only;
 *    - every non-basic variable is within its bounds (asserting a bound on one
 *      moves its value, popping only loosens bounds), so only basic variables
 *      can violate their bounds, which [[check]] repairs by pivoting;
 *    - after a [[check]] returning `Sat`, every variable is within its bounds
 *      and every value is an integer: [[value]] is a solution.
 */
final class Solver:
  import Solver.*

  private val lower = ArrayBuffer.empty[Bound | Null]
  private val upper = ArrayBuffer.empty[Bound | Null]
  private val values = ArrayBuffer.empty[Rational]

  /** The row of each basic variable (`null` for a non-basic one): the
   *  coefficients of the non-basic variables whose combination it equals.
   */
  private val rows = ArrayBuffer.empty[HashMap[Var, Rational] | Null]

  /** For each variable, the basic variables whose rows mention it. */
  private val columns = ArrayBuffer.empty[HashSet[Var]]

  /** Whether the variable stands for an expression, and then the gcd of the
   *  expression's coefficients: the variable holds the expression divided by
   *  it, so that its bounds can be tightened to integers.
   */
  private val isSlack = ArrayBuffer.empty[Boolean]
  private val scale = ArrayBuffer.empty[BigInt]

  private val trail = ArrayBuffer.empty[Undo]
  private val levels = ArrayBuffer.empty[(Int, Int)] // the lengths of `trail` and `distinct` at each push

  /** Variables whose bounds tightened since the last bound propagation. */
  private val tightened = ArrayBuffer.empty[Var]

  /** The values variables must not take, with the reasons (see
   *  [[assertDistinct]]).
   */
  private val distinct = ArrayBuffer.empty[(Var, BigInt, Int)] // in the caller's units (see `scale`)

  /** Reasons of the branches of the current branch and bound search: negative
   *  (never a caller's), and removed from the explanations they contribute to.
   */
  private var branches = 0

  def numVars: Int = values.length

  /** A new integer variable, unbounded, non-basic, valued 0. */
  def newVar(): Var =
    lower += null
    upper += null
    values += Rational.zero
    rows += null
    columns += HashSet.empty
    isSlack += false
    scale += 1
    values.length - 1

  /** A new variable standing for `Σ coefficient × variable` over `terms`
   *  (non-empty, integer coefficients, non-zero): basic, with the expression
   *  substituted by the rows of the basic variables it mentions.
   */
  def newSlack(terms: Iterable[(BigInt, Var)]): Var =
    require(terms.nonEmpty, "empty expression")
    require(terms.forall((a, _) => a.signum != 0), "zero coefficient")
    val g = terms.map((a, _) => a.abs).reduce(_ gcd _)
    val s = newVar()
    isSlack(s) = true
    scale(s) = g
    val row = HashMap.empty[Var, Rational]
    for (a, x) <- terms do
      val c = Rational(a / g)
      val r = rows(x)
      if r == null then add(s, row, x, c) else for (y, b) <- r do add(s, row, y, c * b)
    rows(s) = row
    values(s) = row.iterator.map((x, c) => c * values(x)).foldLeft(Rational.zero)(_ + _)
    s

  /** Adds `c × x` to the row of `owner`, keeping `columns` up to date. */
  private def add(owner: Var, row: HashMap[Var, Rational], x: Var, c: Rational): Unit =
    row.get(x) match
      case None =>
        if !c.isZero then
          row(x) = c
          columns(x) += owner
      case Some(d) =>
        val e = d + c
        if e.isZero then
          row.remove(x)
          columns(x) -= owner
        else row(x) = e

  /** The value of `x` (of the expression it stands for): a solution after a
   *  `Sat` [[check]].
   */
  def value(x: Var): BigInt = (values(x) * Rational(scale(x))).floor

  /** Asserts `x ≤ c` for `reason` (on the expression `x` stands for): `Unsat`
   *  if it contradicts the lower bound of `x`, `Sat` otherwise, which leaves
   *  the feasibility of the whole to [[check]].
   */
  def assertUpper(x: Var, c: BigInt, reason: Int): Result =
    assertBound(x, Bound(Rational(floorDiv(c, scale(x))), reason, Array.empty), true)

  /** Asserts `x ≥ c` for `reason`, as [[assertUpper]]. */
  def assertLower(x: Var, c: BigInt, reason: Int): Result =
    assertBound(x, Bound(Rational(-floorDiv(-c, scale(x))), reason, Array.empty), false)

  /** Asserts `x ≠ c` (for a slack, that its combination is not `c`): a
   *  disequality is decided by [[check]], which branches on `x ≤ c − 1` or
   *  `x ≥ c + 1` when the assignment violates it; it fails at once only against
   *  bounds fixing `x` to `c`.
   */
  def assertDistinct(x: Var, c: BigInt, reason: Int): Result =
    if c.mod(scale(x)) != 0 then Result.Sat // a combination never takes a value its gcd does not divide
    else
      val v = Rational(c / scale(x))
      val l = lower(x)
      val u = upper(x)
      if l != null && u != null && l.value == v && u.value == v then Result.Unsat(l.reasons ++ u.reasons + reason)
      else
        distinct += ((x, c, reason))
        Result.Sat

  private def floorDiv(a: BigInt, b: BigInt): BigInt = (a - a.mod(b)) / b

  private def assertBound(x: Var, bound: Bound, isUpper: Boolean): Result =
    val c = bound.value
    val same = if isUpper then upper(x) else lower(x)
    val opposite = if isUpper then lower(x) else upper(x)
    if same != null && (if isUpper then same.value <= c else same.value >= c) then return Result.Sat // not tighter
    if opposite != null && (if isUpper then c < opposite.value else c > opposite.value) then
      return Result.Unsat(bound.reasons ++ opposite.reasons)
    if levels.nonEmpty then trail += Undo(x, isUpper, same)
    if isUpper then upper(x) = bound else lower(x) = bound
    if rows(x) == null && (if isUpper then values(x) > c else values(x) < c) then update(x, c)
    tightened += x
    Result.Sat

  /** Derives bounds from the rows, as Dutertre and de Moura's bound
   *  propagation: the bounds of the variables of a row bound its basic
   *  variable, and the bound of the basic variable with the bounds of the other
   *  variables bound each variable of the row, all rounded to integers. Only
   *  the rows mentioning a variable whose bound tightened since the last
   *  propagation are visited, the derived bounds queueing their rows in turn, a
   *  bounded number of times per variable, since a cyclic system can tighten
   *  forever; the simplex decides the rest. The premises of a derived bound are
   *  every bound used (for a variable of the row, its own bound is included as
   *  well: simpler, and still sound).
   */
  private def propagateBounds(): Result = boundary:
    val visits = new Array[Int](numVars) // rows visited per basic variable, to bound the work
    var i = 0
    while i < tightened.length do
      val x = tightened(i)
      i += 1
      // The rows mentioning `x`: those of the basic variables of its column, and its own if it is basic.
      val owners = if rows(x) == null then columns(x).toList else x :: columns(x).toList
      for b <- owners if visits(b) < 1 do
        visits(b) += 1
        propagateRow(b) match
          case Result.Sat => ()
          case unsat =>
            tightened.clear()
            break(unsat)
    tightened.clear()
    Result.Sat

  /** Derives the bounds the row of `b` implies (see [[propagateBounds]]). */
  private def propagateRow(b: Var): Result = boundary:
    val row = rows(b).nn
    // The lowest and highest values of the row given the bounds, when every variable has the bound needed.
    var low = Rational.zero
    var high = Rational.zero
    var lowMissing = 0
    var highMissing = 0
    val lowPremises = ArrayBuffer.empty[Bound]
    val highPremises = ArrayBuffer.empty[Bound]
    for (j, a) <- row do
      val forLow = if a.signum > 0 then lower(j) else upper(j)
      val forHigh = if a.signum > 0 then upper(j) else lower(j)
      if forLow == null then lowMissing += 1
      else
        low = low + a * forLow.value
        lowPremises += forLow
      if forHigh == null then highMissing += 1
      else
        high = high + a * forHigh.value
        highPremises += forHigh
    def derive(x: Var, c: Rational, premises: Array[Bound], isUpper: Boolean): Unit =
      assertBound(x, Bound(Rational(if isUpper then c.floor else c.ceil), -1, premises), isUpper) match
        case Result.Sat => ()
        case unsat => break(unsat)
    if lowMissing == 0 then derive(b, low, lowPremises.toArray, false)
    if highMissing == 0 then derive(b, high, highPremises.toArray, true)
    // a × x_j = b − (the rest of the row), bounded by a bound of b and the lowest or highest value of the rest.
    val u = upper(b)
    val l = lower(b)
    val withUpper = if u != null then (lowPremises += u).toArray else Array.empty[Bound]
    val withLower = if l != null then (highPremises += l).toArray else Array.empty[Bound]
    for (j, a) <- row do
      val forLow = if a.signum > 0 then lower(j) else upper(j)
      val forHigh = if a.signum > 0 then upper(j) else lower(j)
      // a × x_j ≤ u − (low − a × forLow), and a × x_j ≥ l − (high − a × forHigh)
      if u != null && lowMissing == 0 then derive(j, (u.value - low + a * forLow.nn.value) / a, withUpper, a.signum > 0)
      if l != null && highMissing == 0 then
        derive(j, (l.value - high + a * forHigh.nn.value) / a, withLower, a.signum < 0)
    Result.Sat

  /** Sets the non-basic `x` to `v`, updating the basic variables. */
  private def update(x: Var, v: Rational): Unit =
    val delta = v - values(x)
    for b <- columns(x) do values(b) = values(b) + rows(b).nn(x) * delta
    values(x) = v

  /** Sets the basic `b` to `v` by moving the non-basic `n` of its row, then
   *  makes `n` basic in its place (`PivotAndUpdate` in the paper).
   */
  private def pivotAndUpdate(b: Var, n: Var, v: Rational): Unit =
    val theta = (v - values(b)) / rows(b).nn(n)
    values(b) = v
    values(n) = values(n) + theta
    for k <- columns(n) if k != b do values(k) = values(k) + rows(k).nn(n) * theta
    pivot(b, n)

  /** Swaps the basic `b` and the non-basic `n` of its row: solves the row for
   *  `n` and substitutes the result in the other rows mentioning `n`.
   */
  private def pivot(b: Var, n: Var): Unit =
    val rowB = rows(b).nn
    val a = rowB(n)
    rows(b) = null
    for j <- rowB.keys do columns(j) -= b
    // n = (b - Σ_{j ≠ n} a_j x_j) / a
    val rowN = HashMap.empty[Var, Rational]
    add(n, rowN, b, Rational.one / a)
    for (j, aj) <- rowB if j != n do add(n, rowN, j, -aj / a)
    rows(n) = rowN
    for k <- columns(n).toList do
      val rowK = rows(k).nn
      val c = rowK.remove(n).get
      columns(n) -= k
      for (j, aj) <- rowN do add(k, rowK, j, c * aj)

  /** Decides the bounds: `Sat`, and [[value]] is an integer solution, or
   *  `Unsat` with its explanation. Bound propagation first tightens the bounds
   *  by the rows; the simplex (Bland's rule, so it terminates) then decides the
   *  rational relaxation; a fractional value is then split, depth first, into
   *  `x ≤ ⌊v⌋` or `x ≥ ⌈v⌉`, the explanations of two failed branches combining
   *  into one without the branch itself. The open branches are kept on an
   *  explicit stack (a recursion would grow with their number: thousands for a
   *  wide bit-vector lowered to an integer). Branch and bound may not terminate
   *  on unbounded variables.
   */
  def check(): Result =
    val open = ArrayBuffer.empty[Branch]

    /** Decides the relaxation and splits on the first fractional unknown, or
     *  else on the first violated disequality.
     */
    def step(): Result | Branch = checkRelaxation() match
      case Result.Sat =>
        var x = 0
        while x < numVars && (isSlack(x) || values(x).isInteger) do x += 1
        if x < numVars then
          branches += 1
          Branch(x, values(x).floor, values(x).ceil, -branches, Set.empty)
        else
          distinct.find((x, c, _) => values(x) * Rational(scale(x)) == Rational(c)) match
            case Some((x, c, reason)) =>
              branches += 1
              Branch(x, c - scale(x), c + scale(x), -branches, Set(reason))
            case None => Result.Sat
      case unsat => unsat
    var current: Result | Branch = step()
    while true do
      current match
        case Result.Sat =>
          for _ <- open do pop() // the assignment stays valid: bounds only loosen
          return Result.Sat
        case Result.Unsat(reasons) if open.isEmpty => return Result.Unsat(reasons)
        case Result.Unsat(reasons) =>
          val branch = open.last
          pop()
          if branch.left == null then // the left side failed: try the right one
            branch.left = reasons
            push()
            current = assertLower(branch.x, branch.ceil, branch.reason) match
              case Result.Sat => step()
              case unsat => unsat
          else // both sides failed
            open.remove(open.length - 1)
            current = Result.Unsat((branch.left.nn ++ reasons) - branch.reason ++ branch.because)
        case branch: Branch =>
          open += branch
          push()
          current = assertUpper(branch.x, branch.floor, branch.reason) match
            case Result.Sat => step()
            case unsat => unsat
    throw AssertionError("unreachable")

  /** Decides the rational relaxation of the bounds: bound propagation, then the
   *  simplex; `Sat` leaves the integrality to [[check]].
   */
  def checkRelaxation(): Result = propagateBounds() match
    case Result.Sat => simplex()
    case unsat => unsat

  /** Repairs the basic variables violating their bounds, smallest first, by
   *  pivoting with the smallest non-basic variable of their row that can move
   *  in the right direction (`Check` in the paper); when none can, the row's
   *  bounds explain the infeasibility.
   */
  private def simplex(): Result =
    while true do
      var b = 0
      while b < numVars && !(rows(b) != null && violates(b)) do b += 1
      if b == numVars then return Result.Sat
      val row = rows(b).nn
      val l = lower(b)
      val u = upper(b)
      val tooLow = l != null && values(b) < l.value
      val bound = if tooLow then l.nn else u.nn
      // To raise `b`, raise a non-basic variable with a positive coefficient or lower one with a negative coefficient (the converse to lower `b`).
      var n = -1
      for (j, a) <- row do
        val canMove = if a.signum > 0 == tooLow then canRaise(j) else canLower(j)
        if canMove && (n == -1 || j < n) then n = j
      if n == -1 then
        val reasons = row.iterator.map((j, a) => (if a.signum > 0 == tooLow then upper(j) else lower(j)).nn.reasons)
        return Result.Unsat(reasons.foldLeft(bound.reasons)(_ ++ _))
      pivotAndUpdate(b, n, bound.value)
    throw AssertionError("unreachable")

  private def violates(x: Var): Boolean =
    val l = lower(x)
    val u = upper(x)
    (l != null && values(x) < l.value) || (u != null && values(x) > u.value)

  private def canRaise(x: Var): Boolean =
    val u = upper(x)
    u == null || values(x) < u.value

  private def canLower(x: Var): Boolean =
    val l = lower(x)
    l == null || values(x) > l.value

  /** The number of open levels. */
  def level: Int = levels.length

  def push(): Unit = levels += ((trail.length, distinct.length))

  /** Removes the bounds and disequalities asserted since the last [[push]]. */
  def pop(): Unit =
    require(levels.nonEmpty, "pop without a matching push")
    val (mark, distinctMark) = levels.remove(levels.length - 1)
    distinct.takeInPlace(distinctMark)
    while trail.length > mark do
      val undo = trail.remove(trail.length - 1)
      if undo.isUpper then upper(undo.x) = undo.old else lower(undo.x) = undo.old
    tightened.clear() // popping only loosens bounds
