package net.bovel.lia

import scala.collection.mutable.{ArrayBuffer, HashMap, HashSet}
import scala.util.boundary
import scala.util.boundary.break

/** A variable of the [[Solver]]: an index in `0 until numVars`. */
type Var = Int

object Solver:
  enum Result:
    case Sat

    /** [[Solver.check]] was told to stop (see its `keepGoing`). */
    case Unknown

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
  private final class Bound(val value: Delta, val reason: Int, val premises: Array[Bound]):
    lazy val reasons: Set[Int] = // once per bound: a conflict may ask again and again along a long derivation
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
      var left: Set[Int] | Null = null,
      strict: Boolean = false // a real disequality splits into strict sides
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

  /** The infinitesimal parts of the assignment, touched only when a real
   *  variable exists ([[anyReal]]): a pure-integer problem pays for exactly one
   *  rational per variable.
   */
  private val valuesD = ArrayBuffer.empty[Rational]
  private var anyReal = false

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

  /** Whether the variable is integer-valued: branch and bound and the integer
   *  tightenings apply to it; a real variable is decided by the relaxation.
   */
  private val isInt = ArrayBuffer.empty[Boolean]
  private var anyInt = false

  def isInteger(x: Var): Boolean = isInt(x)

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

  /** A new variable, unbounded, non-basic, valued 0; integer unless said
   *  otherwise.
   */
  def newVar(integer: Boolean = true): Var =
    lower += null
    upper += null
    values += Rational.zero
    valuesD += Rational.zero
    rows += null
    columns += HashSet.empty
    isSlack += false
    scale += 1
    isInt += integer
    if integer then anyInt = true else anyReal = true
    values.length - 1

  /** A new variable standing for `Σ coefficient × variable` over `terms`
   *  (non-empty, integer coefficients, non-zero): basic, with the expression
   *  substituted by the rows of the basic variables it mentions.
   */
  def newSlack(terms: Iterable[(BigInt, Var)], integer: Boolean = true): Var =
    require(terms.nonEmpty, "empty expression")
    require(terms.forall((a, _) => a.signum != 0), "zero coefficient")
    val g = if integer then terms.map((a, _) => a.abs).reduce(_ gcd _) else BigInt(1)
    val s = newVar(integer)
    isSlack(s) = true
    scale(s) = g
    val row = HashMap.empty[Var, Rational]
    for (a, x) <- terms do
      val c = Rational(a / g)
      val r = rows(x)
      if r == null then add(s, row, x, c) else for (y, b) <- r do add(s, row, y, c * b)
    rows(s) = row
    values(s) = row.iterator.map((x, c) => values(x) * c).foldLeft(Rational.zero)(_ + _)
    if anyReal then valuesD(s) = row.iterator.map((x, c) => valuesD(x) * c).foldLeft(Rational.zero)(_ + _)
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

  /** The value of a real variable, the infinitesimal resolved: after a `Sat`
   *  [[check]], a concrete positive δ small enough for every bound exists, and
   *  this is the value at it.
   */
  def realValue(x: Var): Rational = values(x) + (if anyReal then valuesD(x) else Rational.zero) * delta0()

  /** A δ that satisfies every asserted bound strictly enough: for each bound
   *  whose infinitesimal part demands room, the rational gap divided by the
   *  infinitesimal gap bounds δ from above; 1 works when nothing does.
   */
  private def delta0(): Rational =
    var best = Rational.one
    var x = 0
    while x < numVars do
      val vr = values(x)
      val vd = if anyReal then valuesD(x) else Rational.zero
      val l = lower(x)
      val u = upper(x)
      if l != null && l.value.d > vd && l.value.r < vr then
        val candidate = (vr - l.value.r) / (l.value.d - vd)
        if candidate < best then best = candidate
      if u != null && u.value.d < vd && u.value.r > vr then
        val candidate = (u.value.r - vr) / (vd - u.value.d)
        if candidate < best then best = candidate
      x += 1
    best

  /** Asserts `x ≤ c` for `reason` (on the expression `x` stands for): `Unsat`
   *  if it contradicts the lower bound of `x`, `Sat` otherwise, which leaves
   *  the feasibility of the whole to [[check]].
   */
  def assertUpper(x: Var, c: BigInt, reason: Int): Result = assertUpper(x, c, false, reason)

  /** Asserts `x ≤ c`, or `x < c` when `strict` — which for an integer variable
   *  is `x ≤ c − 1`, and for a real one an inclusive bound at `c − δ`.
   */
  def assertUpper(x: Var, c: BigInt, strict: Boolean, reason: Int): Result =
    val value =
      if isInt(x) then Delta(Rational(floorDiv(if strict then c - 1 else c, scale(x))))
      else if strict then Delta.strictUpper(Rational(c))
      else Delta(Rational(c))
    assertBound(x, Bound(value, reason, Array.empty), true)

  /** Asserts `x ≥ c` for `reason`, as [[assertUpper]]. */
  def assertLower(x: Var, c: BigInt, reason: Int): Result = assertLower(x, c, false, reason)

  /** Asserts `x ≥ c`, or `x > c` when `strict`, as [[assertUpper]]. */
  def assertLower(x: Var, c: BigInt, strict: Boolean, reason: Int): Result =
    val value =
      if isInt(x) then Delta(Rational(-floorDiv(-(if strict then c + 1 else c), scale(x))))
      else if strict then Delta.strictLower(Rational(c))
      else Delta(Rational(c))
    assertBound(x, Bound(value, reason, Array.empty), false)

  /** Asserts `x ≠ c` (for a slack, that its combination is not `c`): a
   *  disequality is decided by [[check]], which branches on `x ≤ c − 1` or
   *  `x ≥ c + 1` when the assignment violates it; it fails at once only against
   *  bounds fixing `x` to `c`.
   */
  def assertDistinct(x: Var, c: BigInt, reason: Int): Result =
    if isInt(x) && c.mod(scale(x)) != 0 then
      Result.Sat // a combination never takes a value its gcd does not divide
    else
      val v = Delta(Rational(c / (if isInt(x) then scale(x) else BigInt(1))))
      val l = lower(x)
      val u = upper(x)
      if l != null && u != null && l.value == v && u.value == v then Result.Unsat(l.reasons ++ u.reasons + reason)
      else
        distinct += ((x, c, reason))
        Result.Sat

  private def floorDiv(a: BigInt, b: BigInt): BigInt = (a - a.mod(b)) / b

  /** Whether the bounds asserted so far already decide `x ≤ c`, with the
   *  reasons: `Some((true, ...))` when they force it, `Some((false, ...))` when
   *  they forbid it, `None` when both remain possible. For the theory
   *  propagation of a bound atom (see `smt.LiaTheory`).
   */
  def decidesUpper(x: Var, c: BigInt): Option[(Boolean, Set[Int])] =
    val value = Delta(Rational(floorDiv(c, scale(x))))
    val u = upper(x)
    val l = lower(x)
    if u != null && u.value <= value then Some((true, u.reasons))
    else if l != null && l.value > value then Some((false, l.reasons))
    else None

  private def assertBound(x: Var, bound: Bound, isUpper: Boolean): Result =
    val c = bound.value
    val same = if isUpper then upper(x) else lower(x)
    val opposite = if isUpper then lower(x) else upper(x)
    if same != null && (if isUpper then same.value <= c else same.value >= c) then return Result.Sat // not tighter
    if opposite != null && (if isUpper then c < opposite.value else c > opposite.value) then
      return Result.Unsat(bound.reasons ++ opposite.reasons)
    if levels.nonEmpty then trail += Undo(x, isUpper, same)
    if isUpper then upper(x) = bound else lower(x) = bound
    if rows(x) == null && (if isUpper then compareValue(x, c) > 0 else compareValue(x, c) < 0) then update(x, c)
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
        if !keepGoing() then
          tightened.clear()
          break(Result.Unknown)
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
    var low = Delta.zero
    var high = Delta.zero
    var lowMissing = 0
    var highMissing = 0
    val lowPremises = ArrayBuffer.empty[Bound]
    val highPremises = ArrayBuffer.empty[Bound]
    for (j, a) <- row do
      val forLow = if a.signum > 0 then lower(j) else upper(j)
      val forHigh = if a.signum > 0 then upper(j) else lower(j)
      if forLow == null then lowMissing += 1
      else
        low = low + forLow.value * a
        lowPremises += forLow
      if forHigh == null then highMissing += 1
      else
        high = high + forHigh.value * a
        highPremises += forHigh
    def derive(x: Var, c: Delta, premises: Array[Bound], isUpper: Boolean): Unit =
      val rounded = if isInt(x) then Delta(Rational(if isUpper then c.floor else c.ceil)) else c
      assertBound(x, Bound(rounded, -1, premises), isUpper) match
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
      if u != null && lowMissing == 0 then
        derive(j, (u.value - low + forLow.nn.value * a) / a, withUpper, a.signum > 0)
      if l != null && highMissing == 0 then
        derive(j, (l.value - high + forHigh.nn.value * a) / a, withLower, a.signum < 0)
    Result.Sat

  /** Sets the non-basic `x` to `v`, updating the basic variables. */
  private def update(x: Var, v: Delta): Unit =
    val moved = v.r - values(x)
    val movedD = if anyReal then v.d - valuesD(x) else Rational.zero
    for b <- columns(x) do
      val a = rows(b).nn(x)
      values(b) = values(b) + moved * a
      if anyReal then valuesD(b) = valuesD(b) + movedD * a
    values(x) = v.r
    if anyReal then valuesD(x) = v.d

  /** Sets the basic `b` to `v` by moving the non-basic `n` of its row, then
   *  makes `n` basic in its place (`PivotAndUpdate` in the paper).
   */
  private def pivotAndUpdate(b: Var, n: Var, v: Delta): Unit =
    val a = rows(b).nn(n)
    val theta = (v.r - values(b)) / a
    val thetaD = if anyReal then (v.d - valuesD(b)) / a else Rational.zero
    values(b) = v.r
    values(n) = values(n) + theta
    if anyReal then
      valuesD(b) = v.d
      valuesD(n) = valuesD(n) + thetaD
    for k <- columns(n) if k != b do
      val c = rows(k).nn(n)
      values(k) = values(k) + theta * c
      if anyReal then valuesD(k) = valuesD(k) + thetaD * c
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
  /** Asked at every step — a pivot, a row propagation, a branch — whether to go
   *  on: a budget of steps (fuel), a deadline, ... `false` makes the check
   *  return `Unknown`.
   */
  var keepGoing: () => Boolean = () => true

  /** For the tests: pretend the small-model box is this many bits wide. */
  private[lia] var boxBitsOverride: Long = -1

  /** How many bits a solution needs at most, when every variable is an integer:
   *  a generous envelope of Papadimitriou's small-model bound (JACM 1981) — a
   *  feasible system of `m` constraints over `n` variables with entries of
   *  absolute value at most `a` has a solution with entries at most
   *  `n·(m·a)^(2m+1)`. The bound itself is astronomical and never computed; its
   *  bit length is enough to prune a branch against, and the pruning is what
   *  makes [[check]] a decision procedure for pure integers, fuel allowing: no
   *  solution lives past the box, so exhausting it is a sound unsat.
   */
  private def smallModelBoxBits(): Long =
    var a = BigInt(2)
    var m = 0
    var x = 0
    while x < numVars do
      val row = rows(x)
      if row != null then
        m += 1
        var l = BigInt(1)
        for (_, c) <- row do l = l / (l gcd c.den) * c.den
        for (_, c) <- row do a = a.max((c.num * (l / c.den)).abs)
        a = a.max(l)
      val lo = lower(x)
      val hi = upper(x)
      if lo != null then
        m += 1
        a = a.max(lo.value.r.num.abs)
      if hi != null then
        m += 1
        a = a.max(hi.value.r.num.abs)
      x += 1
    val size = BigInt(m + numVars + 2) * (a + 2)
    (2L * (m + numVars) + 6) * size.bitLength

  /** Every reason asserted, bounds and disequalities: the explanation when the
   *  small-model box took part in an unsat — the box argument is about the
   *  whole system, never a subset of it.
   */
  private def assertedReasons(): Set[Int] =
    val out = Set.newBuilder[Int]
    var x = 0
    while x < numVars do
      val l = lower(x)
      val u = upper(x)
      if l != null then out ++= l.reasons.filter(_ >= 0)
      if u != null then out ++= u.reasons.filter(_ >= 0)
      x += 1
    for (_, _, reason) <- distinct do out += reason
    out.result()

  def check(): Result =
    val boxBits: Long =
      if anyReal then -1L
      else if boxBitsOverride >= 0 then boxBitsOverride
      else smallModelBoxBits()
    var boxPruned = false
    def pastBox(c: BigInt): Boolean =
      val past = boxBits >= 0 && c.abs.bitLength > boxBits
      if past then boxPruned = true
      past
    val open = ArrayBuffer.empty[Branch]

    /** Decides the relaxation and splits on the first fractional unknown, or
     *  else on the first violated disequality.
     */
    def step(): Result | Branch = relaxationAndWindows() match
      case Result.Sat =>
        var x = 0
        while x < numVars && (isSlack(x) || !isInt(x) || deltaValue(x).isInteger) do x += 1
        if x < numVars then
          branches += 1
          val v = deltaValue(x)
          Branch(x, v.floor, v.ceil, -branches, Set.empty)
        else
          distinct.find((x, c, _) =>
            values(x) * Rational(if isInt(x) then scale(x) else BigInt(1)) == Rational(c)
              && (!anyReal || valuesD(x).isZero)
          ) match
            case Some((x, c, reason)) =>
              branches += 1
              if isInt(x) then Branch(x, c - scale(x), c + scale(x), -branches, Set(reason))
              else Branch(x, c, c, -branches, Set(reason), strict = true)
            case None => Result.Sat
      case unsat => unsat

    def relaxationAndWindows(): Result = checkRelaxation() match
      case Result.Sat => integerWindows()
      case unsat => unsat
    var current: Result | Branch = step()
    while true do
      current match
        case Result.Sat =>
          for _ <- open do pop() // the assignment stays valid: bounds only loosen
          return Result.Sat
        case Result.Unknown =>
          for _ <- open do pop()
          return Result.Unknown
        case Result.Unsat(reasons) if open.isEmpty =>
          return Result.Unsat(if boxPruned then assertedReasons() else reasons)
        case Result.Unsat(reasons) =>
          val branch = open.last
          pop()
          if branch.left == null then // the left side failed: try the right one
            branch.left = reasons
            push()
            current =
              if pastBox(branch.ceil) then Result.Unsat(Set.empty) // no solution past the box
              else
                assertLower(branch.x, branch.ceil, branch.strict, branch.reason) match
                  case Result.Sat => step()
                  case unsat => unsat
          else // both sides failed
            open.remove(open.length - 1)
            current = Result.Unsat((branch.left.nn ++ reasons) - branch.reason ++ branch.because)
        case branch: Branch if !keepGoing() =>
          for _ <- open do pop()
          return Result.Unknown
        case branch: Branch =>
          open += branch
          push()
          current =
            if pastBox(branch.floor) then Result.Unsat(Set.empty) // no solution past the box
            else
              assertUpper(branch.x, branch.floor, branch.strict, branch.reason) match
                case Result.Sat => step()
                case unsat => unsat
    throw AssertionError("unreachable")

  /** Decides the rational relaxation of the bounds: bound propagation, then the
   *  simplex; `Sat` leaves the integrality to [[check]].
   */
  def checkRelaxation(): Result = propagateBounds() match
    case Result.Sat => simplex()
    case unsat => unsat

  /** A cut on the integer content of the mixed rows, for the problems branch
   *  and bound alone cannot close: an integer combination confined to a window
   *  holding none of its values (`is_int x` and `not (is_int (+ x 1))` — an
   *  integer difference in an open unit interval). The real variables are
   *  eliminated from the bounds and the rows — substituted away where a row
   *  defines them, Fourier–Motzkin pairs elsewhere — leaving inequalities over
   *  integer variables only, whose right sides round down after dividing by the
   *  gcd of the coefficients: opposite inequalities that cross after the
   *  rounding are inconsistent, and the bounds they came from explain it.
   *  Vacuous on a pure-integer problem, and skipped unless some row mixes the
   *  two kinds; the work is capped, a cut being an aid, not an obligation.
   */
  private def integerWindows(): Result = boundary:
    if !anyReal || !anyInt then break(Result.Sat)
    var mixed = false
    var b = 0
    while b < numVars && !mixed do
      val row = rows(b)
      mixed = row != null && row.keysIterator.exists(x => isInt(x) != isInt(b))
      b += 1
    if !mixed then break(Result.Sat)
    // The system: every bound as `Σ c·x ≤ d` with its reasons, every row touching a real as an equation.
    val inequalities = ArrayBuffer.empty[(HashMap[Var, Rational], Delta, Set[Int])]
    val equations = ArrayBuffer.empty[HashMap[Var, Rational]]
    val reals = HashSet.empty[Var]
    b = 0
    while b < numVars do
      val row = rows(b)
      if row != null && (!isInt(b) || row.keysIterator.exists(x => !isInt(x))) then
        val eq = row.clone()
        eq(b) = Rational(-1)
        equations += eq
        for x <- eq.keys if !isInt(x) do reals += x
      b += 1
    for x <- 0 until numVars do
      if reals(x) || !isInt(x) then
        val u = upper(x)
        val l = lower(x)
        if u != null then inequalities += ((HashMap(x -> Rational.one), u.value, u.reasons))
        if l != null then inequalities += ((HashMap(x -> Rational(-1)), -l.value, l.reasons))
    // Eliminate each real: by the first equation defining it, its bounds following the substitution,
    // or by pairing its upper and lower inequalities when no equation mentions it.
    val cap = 256 // a cut is an aid, not an obligation: beyond this, give up quietly
    def substitute(into: HashMap[Var, Rational], x: Var, pivot: HashMap[Var, Rational]): Unit =
      into.get(x) match
        case None => ()
        case Some(c) =>
          val f = c / pivot(x)
          for (y, a) <- pivot do
            val updated = into.getOrElse(y, Rational.zero) - a * f
            if updated.isZero then into.remove(y) else into(y) = updated
    for x <- reals.toList.sorted do // a fixed order: the same conflict on every machine
      equations.find(_.contains(x)) match
        case Some(pivot) =>
          equations -= pivot
          for eq <- equations do substitute(eq, x, pivot)
          for (terms, _, _) <- inequalities do substitute(terms, x, pivot)
        case None =>
          val (touching, rest) = inequalities.toList.partition(_._1.contains(x))
          val (uppers, lowers) = touching.partition(_._1(x).signum > 0) // c·x ≤ ... vs −c·x ≤ ...
          if uppers.length * lowers.length + rest.length > cap then break(Result.Sat)
          inequalities.clear()
          inequalities ++= rest
          for (ut, ud, ur) <- uppers; (lt, ld, lr) <- lowers do
            val terms = ut.clone()
            val f = ut(x) / -lt(x)
            for (y, a) <- lt do
              val updated = terms.getOrElse(y, Rational.zero) + a * f
              if updated.isZero then terms.remove(y) else terms(y) = updated
            terms.remove(x)
            inequalities += ((terms, ud + ld * f, ur ++ lr))
    // Integer-only inequalities: divide by the gcd, round down, and look for a crossing pair.
    val best = HashMap.empty[List[(Var, BigInt)], (BigInt, Set[Int])]
    for (terms, d, reasons) <- inequalities do
      if terms.isEmpty then
        if d < Delta.zero then break(Result.Unsat(reasons))
      else if terms.keysIterator.forall(isInt(_)) then
        var l = BigInt(1)
        for (_, a) <- terms do l = l / (l gcd a.den) * a.den
        var g = BigInt(0)
        for (_, a) <- terms do g = g gcd (a.num * (l / a.den)).abs
        val key = terms.toList.map((y, a) => (y, a.num * (l / a.den) / g)).sortBy(_._1)
        val bound = (d * Rational(l) / Rational(g)).floor
        val better = best.get(key) match
          case Some((known, _)) => bound < known
          case None => true
        if better then best(key) = (bound, reasons)
        best.get(key.map((y, n) => (y, -n))) match
          case Some((opposite, others)) if bound + opposite < 0 => break(Result.Unsat(reasons ++ others))
          case _ => ()
    Result.Sat

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
      val tooLow = l != null && compareValue(b, l.value) < 0
      val bound = if tooLow then l.nn else u.nn
      // To raise `b`, raise a non-basic variable with a positive coefficient or lower one with a negative coefficient (the converse to lower `b`).
      var n = -1
      for (j, a) <- row do
        val canMove = if a.signum > 0 == tooLow then canRaise(j) else canLower(j)
        if canMove && (n == -1 || j < n) then n = j
      if n == -1 then
        val reasons = row.iterator.map((j, a) => (if a.signum > 0 == tooLow then upper(j) else lower(j)).nn.reasons)
        return Result.Unsat(reasons.foldLeft(bound.reasons)(_ ++ _))
      if !keepGoing() then return Result.Unknown
      pivotAndUpdate(b, n, bound.value)
    throw AssertionError("unreachable")

  /** Compares the value of `x` (rational and infinitesimal parts) to `c`. */
  private def compareValue(x: Var, c: Delta): Int =
    val first = values(x).compare(c.r)
    if first != 0 || !anyReal then first else valuesD(x).compare(c.d)

  private def deltaValue(x: Var): Delta =
    Delta(values(x), if anyReal then valuesD(x) else Rational.zero)

  private def violates(x: Var): Boolean =
    val l = lower(x)
    val u = upper(x)
    (l != null && compareValue(x, l.value) < 0) || (u != null && compareValue(x, u.value) > 0)

  private def canRaise(x: Var): Boolean =
    val u = upper(x)
    u == null || compareValue(x, u.value) < 0

  private def canLower(x: Var): Boolean =
    val l = lower(x)
    l == null || compareValue(x, l.value) > 0

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
