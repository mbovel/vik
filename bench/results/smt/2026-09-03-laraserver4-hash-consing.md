# The compiler's bit-vector fuel-outs, and one chain of constants per operation, 2026-09-03, laraserver4

The qualified-types checker's comparison (`scala3-rt/solver-comparison/REPORT.md`, "Bit-vectors, corrected")
had VIK 0.0.0+282 spend its million steps on 31 queries z3 and SMTInterpol answer in milliseconds
(`solver-comparison/fuel-outs`). The smallest is `x0 = 4(x+1) ∧ x0 ≠ 4(x+1)`, and its lowered script shows
the cause: each occurrence of a bit-vector operation got its own chain of fresh constants
(`x + 1 = bv!1 + 2³² k!2`, `4 bv!1 = bv!3 + 2³² k!4`, then `bv!5 … k!8` for the same term), so the solver
had to prove the two chains equal by an integer argument over 2³² (a value in `[0, 2³²)` congruent to
`x + 1` is unique, but that is a fact about integers, not a syntactic one), which branch and bound walked
through for minutes. The lowering now memoizes the operations by their lowered operands, once per level
(`Lowering.loweredOperations`): the second `bvmul 4 (bvadd 1 x)` reuses the constants of the first, the
equality is syntactic, and the fib queries — whose `ite` chains repeat whole subterms — lower to half the
constants (`binary_search_neg-d7a8ef9fc866`: 34 instead of 60).

**Result.** 30 of the 31 answer, in 0.3–0.5 s cold with the JVM's start, all as z3 does; the four the
e-graph proves are `unsat` in 0.3 s. Times on laraserver4, 20 s cap, one run each, the `vik` classes of
`f2213cc` before and this change after; z3's are the comparison's.

| Query | e-graph | VIK before (fuel out after) | VIK after | z3 |
|---|---|---:|---:|---:|
| `fib_selfify-04a20d6a75ae` | unsat | 48.3 s | unsat 0.35 s | unsat 0.6 ms |
| `binary_search_neg-3faf8d39783c` | unknown | 44.2 s | 🐢 | sat 2.2 ms |
| `binary_search_neg-f9aeccb48d07` | unknown | 20.8 s | sat 0.41 s | sat 3.4 ms |
| `fib_memoized2-cae11b3947fe` | unknown | 18.5 s | sat 0.45 s | sat 12.3 ms |
| `fib_memoized2-c6d3b4862501` | unknown | 18.3 s | sat 0.42 s | sat 12.4 ms |
| `fib_memoized2-30eec0e0d1fc` | unknown | 18.1 s | sat 0.45 s | sat 12.8 ms |
| `fib_memoized2-24300d6620e7` | unknown | 18.1 s | sat 0.43 s | sat 16.6 ms |
| `fib_memoized2-7309e4f1b3dd` | unknown | 18.1 s | sat 0.42 s | sat 31.0 ms |
| `fib_memoized2-f1e448b81968` | unknown | 18.1 s | sat 0.44 s | sat 13.0 ms |
| `fib_memoized2-136ceb568ac8` | unknown | 9.4 s | sat 0.43 s | sat 30.4 ms |
| `fib_memoized2-d366bc4772bc` | unknown | 9.4 s | sat 0.44 s | sat 30.0 ms |
| `fib_memoized2-4ad1e996d4c3` | unknown | 9.3 s | sat 0.43 s | sat 12.3 ms |
| `fib_memoized2-f31450d65b69` | unknown | 9.3 s | sat 0.42 s | sat 11.8 ms |
| `fib_memoized2-6113d395afe5` | unknown | 9.2 s | sat 0.41 s | sat 11.9 ms |
| `fib_memoized2-e86bc6675a6d` | unknown | 9.2 s | sat 0.43 s | sat 12.2 ms |
| `fib_selfify-3f08c05868df` | unknown | 8.5 s | sat 0.54 s | sat 13.5 ms |
| `fib_selfify-4badffbe7f89` | unknown | 8.4 s | sat 0.53 s | sat 12.9 ms |
| `fib_selfify-015126f6148c` | unknown | 8.4 s | sat 0.52 s | sat 13.3 ms |
| `fib_selfify-4eaddf6f7831` | unknown | 8.4 s | sat 0.51 s | sat 33.7 ms |
| `fib_selfify-867d73a7e6bb` | unknown | 8.4 s | sat 0.53 s | sat 14.4 ms |
| `fib_selfify-eab4088540ea` | unknown | 8.3 s | sat 0.52 s | sat 11.7 ms |
| `fib_selfify-07c50764735b` | unknown | 6.7 s | sat 0.49 s | sat 31.1 ms |
| `fib_memoized2-af745873ef16` | unsat | 6.7 s | unsat 0.33 s | unsat 0.6 ms |
| `fib_selfify-f385662dc903` | unknown | 6.6 s | sat 0.51 s | sat 12.3 ms |
| `fib_selfify-474421e51f45` | unknown | 6.6 s | sat 0.52 s | sat 33.1 ms |
| `fib_selfify-97e265dade8e` | unknown | 6.6 s | sat 0.49 s | sat 14.2 ms |
| `fib_selfify-d493c8b11d25` | unknown | 6.6 s | sat 0.52 s | sat 14.2 ms |
| `fib_selfify-708ac187768b` | unknown | 6.6 s | sat 0.51 s | sat 32.6 ms |
| `subtyping_normalization-2855607370bf` | unsat | 2.9 s | unsat 0.28 s | unsat 0.2 ms |
| `fib_memoized2-1600ee1bff39` | unknown | 2.1 s | sat 0.36 s | sat 3.3 ms |
| `subtyping_normalization-aefd11751e2a` | unsat | 1.9 s | unsat 0.29 s | unsat 0.2 ms |

**Warm, the evaluation's bit-vector instances** (`VikBench.api`, JMH `-f 2 -wi 3 -i 3`, ms/op):
`sage__app2__bench_512` 22.1 ± 0.5 where the day's LIA records had it at 0.82–0.93 s — its `concat`s and
`extract`s of the same bytes lowered once instead of at every use; `pspace__ndist.a.20000` 0.306 ± 0.034
against 0.341 ± 0.016 in the warm table of 2026-09-02; `bench_ab__a649test0047` 0.871 ± 0.06 (no earlier
warm number); the integer query `alpha-093dddceea58` 0.182 ± 0.002, unchanged as it must be.

**Two files that answered before now run past the budget**, `20230314-Jaroslav-Bendik-Certora__65782_…_6_QF_UFBV`
(1.2 s before) and the compiler's `binary_search_neg-d7a8ef9fc866` (0.4 s before), both `sat`, both on
`ignore.txt` as beyond the search. Their profile is the remaining cause, the one the hash-consing does not
touch: the simplex pivots on rows whose coefficients are 2³², the products of two of them leave `Long` and
the rationals become `BigInteger` arithmetic, and Bland's rule walks through many such pivots before the
branch and bound finds the overflow a model needs. Branching on the fractional variable with the narrowest
range first (the overflow and sign bits before the 32-bit values) was tried and changed nothing: the walk is
in the relaxation, not in the choice of the split. A decision procedure for these modular constraints —
cuts, or the top bits reasoned about as bits — is the next step for the bit-vector fragment; the compiler
falls back to its e-graph on a fuel-out meanwhile.
