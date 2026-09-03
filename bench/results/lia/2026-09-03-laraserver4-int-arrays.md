# LIA solver: the tableau on `Int` arrays, 2026-09-03, laraserver4

The sparse rows of 2026-09-02 ([the record](2026-09-02-laraserver4-sparse-rows.md)) measured as nothing on their
own, and the profile of that day pointed at what was left around them: the columns (`HashSet[Var]`, a boxed
`Integer` per membership) and `Rational.gcd` on every product and sum of integers. This experiment changes the
three together and then takes them apart:

- **Rows** — the sparse rows again (a sorted `Array[Var]` and an `Array[Rational]`, binary search, shift on
  insertion), plus a fast path in `Rational`: a result with denominator 1 is normalized already, no gcd.
- **Scan** — rows, and the columns as `Int` arrays in no order, an owner leaving by a linear search and the
  last one taking its slot.
- **Linked** — rows, and the columns as `Int` arrays where each row entry remembers its slot in the column
  (`Row.slots`), so an owner leaves without a search: the last owner takes the slot and its row is told
  ([`Solver.leave`](../../../lia/src/main/scala/net/bovel/lia/Solver.scala)). **Kept.**

Measured against the parent (`c8be007`, hash-map rows and hash-set columns), same JVM, same machine, one
configuration after the other in a worktree, the corpus suite of the main checkout running on other cores
during the baseline (hence its wider intervals).

- Host: laraserver4, AMD EPYC 9254 (24 cores / 96 threads), shared. Temurin 25.0.3, Scala 3.10.0-RC1, sbt 2.0.8, sbt-jmh 0.4.8.
- JMH: `-f 2 -wi 3 -i 3`, average time per operation, one thread; 95% CI from the 6 samples.
- Raw data: `LiaBench` [before](2026-09-03-laraserver4-int-arrays-before.json), [rows](2026-09-03-laraserver4-int-arrays-rows.json),
  [scan](2026-09-03-laraserver4-int-arrays-scan.json), [linked](2026-09-03-laraserver4-int-arrays-linked.json); the evaluation instances
  [before](2026-09-03-laraserver4-int-arrays-vik-before.json), [rows](2026-09-03-laraserver4-int-arrays-vik-rows.json),
  [scan](2026-09-03-laraserver4-int-arrays-vik-scan.json), [linked](2026-09-03-laraserver4-int-arrays-vik-linked.json).

**Result: kept, as the linked columns.** The `LiaBench` workloads run 1.06–1.39× faster, the evaluation's LIA
instances 1.11–1.16×, the Scala 3 queries 1.13×; the one loss is the wide bit-vector instance
`sage__app2__bench_512` at 0.90×, down from 0.79× with the scanning columns.

## `LiaBench`

| Benchmark | Before (ms/op) | Rows (ms/op) | Scan (ms/op) | Linked, kept (ms/op) | Before / linked |
|---|---:|---:|---:|---:|---:|
| `backtrack` n=10 | 4.656 ± 0.685 | 4.522 ± 0.210 | 4.024 ± 0.174 | 4.051 ± 0.184 | 1.15× |
| `backtrack` n=20 | 43.158 ± 1.783 | 39.466 ± 2.028 | 39.515 ± 1.541 | 40.125 ± 1.675 | 1.08× |
| `incremental` n=10 | 4.202 ± 0.626 | 3.782 ± 0.150 | 3.384 ± 0.133 | 3.370 ± 0.152 | 1.25× |
| `incremental` n=20 | 54.204 ± 6.835 | 50.265 ± 2.056 | 41.777 ± 1.700 | 42.174 ± 1.686 | 1.29× |
| `solve` n=10 | 0.902 ± 0.051 | 0.859 ± 0.050 | 0.653 ± 0.032 | 0.647 ± 0.030 | 1.39× |
| `solve` n=20 | 3.556 ± 0.150 | 3.422 ± 0.157 | 3.020 ± 0.186 | 3.282 ± 0.697 | 1.08× |
| `stepChains` n=10 | 0.402 ± 0.016 | 0.388 ± 0.056 | 0.352 ± 0.050 | 0.373 ± 0.046 | 1.08× |
| `stepChains` n=20 | 1.914 ± 0.080 | 1.886 ± 0.256 | 1.698 ± 0.194 | 1.810 ± 0.224 | 1.06× |

## The evaluation instances (`VikBench.api`, the whole solver on the script)

| Instance | Before (ms/op) | Rows (ms/op) | Scan (ms/op) | Linked, kept (ms/op) | Before / linked |
|---|---:|---:|---:|---:|---:|
| `CAV_2009_benchmarks__20-vars__problem__001` | 2.349 ± 0.109 | 2.154 ± 0.047 | 2.046 ± 0.058 | 2.047 ± 0.037 | 1.15× |
| `dillig__20-4` | 9.457 ± 0.319 | 9.303 ± 1.277 | 8.969 ± 1.100 | 8.447 ± 0.297 | 1.12× |
| `wisas__xs_8_8` | 29.367 ± 0.813 | 26.357 ± 0.374 | 24.440 ± 0.468 | 25.371 ± 1.715 | 1.16× |
| `sage__app2__bench_512` | 814.779 ± 37.063 | 807.777 ± 10.539 | 1035.068 ± 4.492 | 903.534 ± 16.321 | 0.90× |
| `alpha-093dddceea58` | 0.230 ± 0.028 | 0.208 ± 0.022 | 0.183 ± 0.019 | 0.206 ± 0.016 | 1.11× |

## The Scala 3 queries (`corpus/QF_UFDTLIA/dotty`, 2 031 queries, a fresh `Session` each, warm, best of 20)

Two runs per configuration, sum over the queries of the best of 20 (and of the medians):

| Configuration | Run 1 | Run 2 |
|---|---:|---:|
| Before | 186.9 ms (197.8) | 199.7 ms (204.7) |
| Rows | 174.4 ms (179.5) | 175.3 ms (180.6) |
| Scan | 158.6 ms (165.2) | 159.4 ms (166.0) |
| Linked | 164.8 ms (169.3) | 161.0 ms (166.1) |

## What the profiles say about `sage__app2__bench_512`

JFR (`-prof jfr`) on that instance alone. Before: `BoxesRunTime.boxToInteger` 23% of the samples, the hash
set's `addElem` and `remove` 16% — the columns. Scan: `Column.-=` 24%, the search itself; the instance's
columns run to 166 owners and a check removes half a million of them, the search averaging 35 entries. Linked:
`leave` 20% inclusive, half of it the binary search in the moved owner's row, the rest the cache miss into
that row — the residual against the hash set, which touched only its own node. The rows are up to 88 entries
long there (a dozen elsewhere), which is also where the shift on insertion costs most.

Not tried: a column with holes compacted lazily (no other row touched at removal, an extra check at every
walk); and a dense row for the wide instances, whose columns would be the identity.
