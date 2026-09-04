# LIA solver: a fast path when no bound is asserted, 2026-09-03, laraserver4

The hypothesis of the sparse-rows record (2026-09-02): the compiler's queries pay for the simplex on a tableau
with no bound at all — `checkRelaxation` at every propagation round, `check` at every final check, each a
scan of the variables and, in `check`, of every row for the small-model box. The change: the solver counts
its bounds (`assertBound` on a fresh one, `pop` on its undo — a bound of level 0 is permanent and keeps the
count up) and remembers whether some value moved from zero; a check with no bound and no disequality resets
the values to zero when they moved (the all-zero assignment satisfies the rows and is integral) and answers
`Sat` at once. 35 lines, exact.

Measured against `f802a1d` (the `Int` arrays), same worktree, one configuration after the other.

- Host: laraserver4, AMD EPYC 9254 (24 cores / 96 threads), shared. Temurin 25.0.3, Scala 3.10.0-RC1, sbt 2.0.8, sbt-jmh 0.4.8.
- JMH: `-f 2 -wi 3 -i 3`, average time per operation, one thread; 95% CI from the 6 samples.
- Raw data: `LiaBench` [before](2026-09-03-laraserver4-no-bounds-before.json), [after](2026-09-03-laraserver4-no-bounds-after.json);
  the evaluation instances [before](2026-09-03-laraserver4-no-bounds-vik-before.json), [after](2026-09-03-laraserver4-no-bounds-vik-after.json).

**Result: no effect. Not kept.** The hypothesis was wrong: the compiler's queries carry bounds, almost all of
them at level 0 (`x ≥ 0` on sizes and indices, the facts of the refinement). A counter in the guard over four
passes of the 2 031 queries: 1 460 checks took the fast path, 88 960 did not — one in sixty. On the LIA
instances (`CAV_2009`, `wisas`) it never triggers. What `checkRelaxation` and `check` cost on these queries
is the simplex with bounds, which the `Int` arrays already made cheaper.

## `LiaBench`

| Benchmark | n | Before (ms/op) | After (ms/op) | Before / after |
|---|---|---:|---:|---:|
| `backtrack` | 10 | 4.046 ± 0.180 | 4.054 ± 0.182 | 1.00× |
| `backtrack` | 20 | 41.870 ± 6.075 | 40.055 ± 1.631 | 1.05× |
| `incremental` | 10 | 3.401 ± 0.145 | 3.417 ± 0.135 | 1.00× |
| `incremental` | 20 | 41.993 ± 1.725 | 42.108 ± 1.719 | 1.00× |
| `solve` | 10 | 0.671 ± 0.027 | 0.664 ± 0.033 | 1.01× |
| `solve` | 20 | 3.013 ± 0.167 | 3.222 ± 0.666 | 0.93× |
| `stepChains` | 10 | 0.373 ± 0.054 | 0.372 ± 0.052 | 1.00× |
| `stepChains` | 20 | 1.671 ± 0.100 | 1.817 ± 0.229 | 0.92× |

## The evaluation instances (`VikBench.api`, the whole solver on the script)

| Instance | Before (ms/op) | After (ms/op) | Before / after |
|---|---:|---:|---:|
| CAV_2009_benchmarks__20-vars__problem__001 | 2.136 ± 0.155 | 2.050 ± 0.033 | 1.04× |
| dillig__20-4 | 8.308 ± 0.181 | 8.323 ± 0.180 | 1.00× |
| wisas__xs_8_8 | 25.092 ± 0.488 | 24.895 ± 0.637 | 1.01× |
| sage__app2__bench_512 | 925.850 ± 46.273 | 923.744 ± 34.002 | 1.00× |
| alpha-093dddceea58 | 0.178 ± 0.002 | 0.181 ± 0.002 | 0.99× |

## The Scala 3 queries (`corpus/QF_UFDTLIA/dotty`, 2 031 queries, a fresh `Session` each, warm, best of 20)

Two runs per configuration, sum over the queries of the best of 20 (and of the medians): before 161.8 ms
(166.2), 160.6 ms (165.2); after 162.0 ms (166.5), 160.7 ms (165.2).

## The change, for the record

```diff
diff --git a/lia/src/main/scala/io/bovel/lia/Solver.scala b/lia/src/main/scala/io/bovel/lia/Solver.scala
index 68d4f63..2a345dc 100644
--- a/lia/src/main/scala/io/bovel/lia/Solver.scala
+++ b/lia/src/main/scala/io/bovel/lia/Solver.scala
@@ -194,6 +194,26 @@ final class Solver:
    */
   private val distinct = ArrayBuffer.empty[(Var, BigInt, Int)] // in the caller's units (see `scale`)
 
+  /** How many bounds are set, and whether some value moved from zero: with no
+   *  bound at all the all-zero assignment satisfies the rows and is integral,
+   *  so a check has nothing to decide once the values are back at zero — the
+   *  fast path of a search that asserts no bound, or has popped them all.
+   */
+  private var bounds = 0
+  private var moved = false
+
+  private def unconstrained(): Boolean =
+    bounds == 0 && distinct.isEmpty && {
+      if moved then
+        var x = 0
+        while x < numVars do
+          values(x) = Rational.zero
+          if anyReal then valuesD(x) = Rational.zero
+          x += 1
+        moved = false
+      true
+    }
+
   /** Reasons of the branches of the current branch and bound search: negative
    *  (never a caller's), and removed from the explanations they contribute to.
    */
@@ -375,6 +395,7 @@ final class Solver:
     if opposite != null && (if isUpper then c < opposite.value else c > opposite.value) then
       return Result.Unsat(bound.reasons ++ opposite.reasons)
     if levels.nonEmpty then trail += Undo(x, isUpper, same)
+    if same == null then bounds += 1
     if isUpper then upper(x) = bound else lower(x) = bound
     if rows(x) == null && (if isUpper then compareValue(x, c) > 0 else compareValue(x, c) < 0) then update(x, c)
     tightened += x
@@ -469,15 +490,16 @@ final class Solver:
 
   /** Sets the non-basic `x` to `v`, updating the basic variables. */
   private def update(x: Var, v: Delta): Unit =
-    val moved = v.r - values(x)
-    val movedD = if anyReal then v.d - valuesD(x) else Rational.zero
+    moved = true
+    val delta = v.r - values(x)
+    val deltaD = if anyReal then v.d - valuesD(x) else Rational.zero
     val column = columns(x)
     var i = 0
     while i < column.size do
       val b = column(i)
       val a = rows(b).nn(x)
-      values(b) = values(b) + moved * a
-      if anyReal then valuesD(b) = valuesD(b) + movedD * a
+      values(b) = values(b) + delta * a
+      if anyReal then valuesD(b) = valuesD(b) + deltaD * a
       i += 1
     values(x) = v.r
     if anyReal then valuesD(x) = v.d
@@ -486,6 +508,7 @@ final class Solver:
    *  makes `n` basic in its place (`PivotAndUpdate` in the paper).
    */
   private def pivotAndUpdate(b: Var, n: Var, v: Delta): Unit =
+    moved = true
     val a = rows(b).nn(n)
     val theta = (v.r - values(b)) / a
     val thetaD = if anyReal then (v.d - valuesD(b)) / a else Rational.zero
@@ -611,6 +634,7 @@ final class Solver:
     out.result()
 
   def check(): Result =
+    if unconstrained() then return Result.Sat
     val boxBits: Long =
       if anyReal then -1L
       else if boxBitsOverride >= 0 then boxBitsOverride
@@ -691,9 +715,12 @@ final class Solver:
   /** Decides the rational relaxation of the bounds: bound propagation, then the
    *  simplex; `Sat` leaves the integrality to [[check]].
    */
-  def checkRelaxation(): Result = propagateBounds() match
-    case Result.Sat => simplex()
-    case unsat => unsat
+  def checkRelaxation(): Result =
+    if unconstrained() then Result.Sat
+    else
+      propagateBounds() match
+        case Result.Sat => simplex()
+        case unsat => unsat
 
   /** A cut on the integer content of the mixed rows, for the problems branch
    *  and bound alone cannot close: an integer combination confined to a window
@@ -859,5 +886,6 @@ final class Solver:
     distinct.takeInPlace(distinctMark)
     while trail.length > mark do
       val undo = trail.remove(trail.length - 1)
+      if undo.old == null then bounds -= 1
       if undo.isUpper then upper(undo.x) = undo.old else lower(undo.x) = undo.old
     tightened.clear() // popping only loosens bounds
```
