# LIA solver: rows as sorted sparse arrays instead of hash maps, 2026-09-02, laraserver4

A row of the tableau was a `HashMap[Var, Rational]` (boxed keys, hash nodes, hashing at every coefficient
read); a profile of VIK inside the Scala 3 qualified-types checker had `BoxesRunTime.boxToInteger` at 18% of
the self time and hash-map traffic at ~10%, and `CAV_2009` had half its samples in `HashMap.findNode`/`update`
under `lia.Solver.pivot`. Commit `26c473f` made a row two arrays — the variables in increasing order and their
coefficients — with a binary search to find one and a shift to insert, every walk by index; columns and the
mixed-integer cut untouched. Measured against its parent (`0b9a9d7`), same JVM, same machine, one after the
other; the consolidation campaign was classifying corpus files on other cores throughout, hence the wide
intervals.

- Host: laraserver4, AMD EPYC 9254 (24 cores / 96 threads), shared. Temurin 25.0.3, Scala 3.10.0-RC1, sbt 2.0.8, sbt-jmh 0.4.8.
- JMH: `-f 2 -wi 3 -i 3`, average time per operation, one thread; 95% CI from the 6 samples.
- Raw data: [before](2026-09-02-laraserver4-sparse-rows-before.json), [after](2026-09-02-laraserver4-sparse-rows-after.json);
  the evaluation instances [before](2026-09-02-laraserver4-sparse-rows-vik-before.json), [after](2026-09-02-laraserver4-sparse-rows-vik-after.json).

**Result: no measurable effect. Reverted.** Every difference is inside the confidence intervals; the boxing
the profile showed is not in the rows but around them (the column sets, the theory's maps from terms to
variables, the atoms), and on these workloads the rows are a dozen entries long, where a hash map and a sorted
array cost the same.

## `LiaBench`

| Benchmark | n | Before (ms/op) | After (ms/op) | Before / after |
|---|---|---:|---:|---:|
| `backtrack` | 10 | 4.451 ± 0.586 | 4.783 ± 0.953 | 0.93× |
| `backtrack` | 20 | 43.904 ± 9.778 | 42.159 ± 7.169 | 1.04× |
| `incremental` | 10 | 4.012 ± 0.921 | 3.773 ± 0.586 | 1.06× |
| `incremental` | 20 | 53.471 ± 5.882 | 50.857 ± 5.591 | 1.05× |
| `solve` | 10 | 0.928 ± 0.228 | 0.913 ± 0.186 | 1.02× |
| `solve` | 20 | 3.828 ± 1.727 | 3.532 ± 0.425 | 1.08× |
| `stepChains` | 10 | 0.396 ± 0.066 | 0.429 ± 0.119 | 0.92× |
| `stepChains` | 20 | 1.978 ± 0.196 | 1.892 ± 0.328 | 1.05× |

## The evaluation instances (`VikBench.api`, the whole solver on the script)

| Instance | Before (ms/op) | After (ms/op) | Before / after |
|---|---:|---:|---:|
| `CAV_2009_benchmarks__20-vars__problem__001` | 2.563 ± 1.223 | 2.183 ± 0.170 | 1.17× |
| `alpha-093dddceea58` | 0.242 ± 0.098 | 0.210 ± 0.010 | 1.15× |
| `dillig__20-4` | 10.704 ± 1.833 | 8.931 ± 1.244 | 1.20× |
| `sage__app2__bench_512` | 836.442 ± 114.609 | 876.042 ± 102.971 | 0.95× |
| `wisas__xs_8_8` | 29.738 ± 5.116 | 29.568 ± 10.386 | 1.01× |

## The Scala 3 queries (`corpus/QF_UFDTLIA/dotty`, 2 031 queries, a fresh `Session` each, warm, best of 20)

Two alternating runs per configuration, sum over the queries of the best of 20 (and of the medians):
before 197 ms (209), 190 ms (198); after 218 ms (250), 184 ms (195). Noise.

What would move these numbers is what the profile actually pointed at: the simplex running on problems with no
bound at all (`checkRelaxation` at every propagation round, `finalCheck` again) — the fast path — and the
boxing in the theory's glue rather than in the tableau.

