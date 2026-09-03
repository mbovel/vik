# The overflow and the sign as choices of the SAT solver, 2026-09-03, laraserver4

After the hash-consing of the lowering ([the record of the fuel-outs](2026-09-03-laraserver4-hash-consing.md)),
three `sat` bit-vector queries still ran past the budget, their profile all simplex pivots on rows with
`2³²` as a coefficient, the products of two of them past `Long` and into `BigInteger` rationals, Bland's
rule walking through them before branch and bound found the overflow a model needs. The overflow count `k`
of `a + b = r + 2³²·k` and the sign bit `s` of `a = u + 2³¹·s` were integer unknowns of the simplex, and
the relaxation set them to fractions.

Neither needs to be. The sign of `a` is the atom `a ≥ 2ⁿ⁻¹`, so a signed comparison is Boolean structure
over the two top bits and the unsigned comparison — `a <ₛ b` is `a` negative and `b` not, or both of one
sign and `a < b` — with no unknown at all; the signed value where a term needs it (`sbv_to_int`, the
signed divisions, `sign_extend`) is a constant defined by the two cases. The overflow of `bvadd`, `bvsub`,
`bvneg` and `bvmul` by a small constant is a disjunction of equalities, `a + b = r` or `a + b = r + 2ⁿ`,
decided by the SAT solver with its learning and its restarts; `2ⁿ` only ever appears as a constant the
simplex compares against, never as a coefficient it pivots on. A `bvmul` by a constant above four keeps its
integer count. All of it is in `Lowering`; the solver did not change. One bit decided as a bit is as far
towards bit-blasting as the fragment goes.

**Result.** The 31 queries of the compiler's comparison all answer, in 0.3–0.5 s cold with the JVM's
start, as z3 does; the two files the hash-consing had lost answer again, and `Sage2__bench_12504` comes off
`ignore.txt`. Warm, the evaluation's bit-vector instances (`VikBench.api`, JMH `-f 2 -wi 3 -i 3`, against
the hash-consed lowering of the same day, [raw](2026-09-03-laraserver4-bits-as-cases.json)):

| Instance | One chain (ms/op) | Bits as cases (ms/op) | Ratio |
|---|---:|---:|---:|
| `bench_ab__a649test0047` | 0.871 ± 0.046 | 0.459 ± 0.059 | 1.90× |
| `sage__app2__bench_512` | 22.1 ± 0.2 | 0.970 ± 0.115 | 22.8× |
| `pspace__ndist.a.20000` | 0.306 ± 0.034 | 0.070 ± 0.007 | 4.4× |
| `alpha-093dddceea58` (integers, a control) | 0.182 ± 0.004 | 0.182 ± 0.004 | 1.00× |

`sage__app2__bench_512` was 0.9 s in the morning's LIA records: 900× over the day, the two changes of the
lowering. The corpus suite is green (5 298 files, the 4-bit random test of every operation included).

The 31 queries, one run each, 20 s cap, laraserver4 (shared):

| Query | e-graph | Fuel out after (+282) | One chain | Bits as cases | z3 |
|---|---|---:|---:|---:|---:|
| `fib_selfify-04a20d6a75ae` | unsat | 48.3 s | unsat 0.35 s | unsat 0.36 s | unsat 0.6 ms |
| `binary_search_neg-3faf8d39783c` | unknown | 44.2 s | 🐢 | sat 0.34 s | sat 2.2 ms |
| `binary_search_neg-f9aeccb48d07` | unknown | 20.8 s | sat 0.41 s | sat 0.35 s | sat 3.4 ms |
| `fib_memoized2-cae11b3947fe` | unknown | 18.5 s | sat 0.45 s | sat 0.39 s | sat 12.3 ms |
| `fib_memoized2-c6d3b4862501` | unknown | 18.3 s | sat 0.42 s | sat 0.39 s | sat 12.4 ms |
| `fib_memoized2-30eec0e0d1fc` | unknown | 18.1 s | sat 0.45 s | sat 0.42 s | sat 12.8 ms |
| `fib_memoized2-24300d6620e7` | unknown | 18.1 s | sat 0.43 s | sat 0.38 s | sat 16.6 ms |
| `fib_memoized2-7309e4f1b3dd` | unknown | 18.1 s | sat 0.42 s | sat 0.40 s | sat 31.0 ms |
| `fib_memoized2-f1e448b81968` | unknown | 18.1 s | sat 0.44 s | sat 0.37 s | sat 13.0 ms |
| `fib_memoized2-136ceb568ac8` | unknown | 9.4 s | sat 0.43 s | sat 0.43 s | sat 30.4 ms |
| `fib_memoized2-d366bc4772bc` | unknown | 9.4 s | sat 0.44 s | sat 0.37 s | sat 30.0 ms |
| `fib_memoized2-4ad1e996d4c3` | unknown | 9.3 s | sat 0.43 s | sat 0.40 s | sat 12.3 ms |
| `fib_memoized2-f31450d65b69` | unknown | 9.3 s | sat 0.42 s | sat 0.37 s | sat 11.8 ms |
| `fib_memoized2-6113d395afe5` | unknown | 9.2 s | sat 0.41 s | sat 0.39 s | sat 11.9 ms |
| `fib_memoized2-e86bc6675a6d` | unknown | 9.2 s | sat 0.43 s | sat 0.40 s | sat 12.2 ms |
| `fib_selfify-3f08c05868df` | unknown | 8.5 s | sat 0.54 s | sat 0.39 s | sat 13.5 ms |
| `fib_selfify-4badffbe7f89` | unknown | 8.4 s | sat 0.53 s | sat 0.41 s | sat 12.9 ms |
| `fib_selfify-015126f6148c` | unknown | 8.4 s | sat 0.52 s | sat 0.42 s | sat 13.3 ms |
| `fib_selfify-4eaddf6f7831` | unknown | 8.4 s | sat 0.51 s | sat 0.41 s | sat 33.7 ms |
| `fib_selfify-867d73a7e6bb` | unknown | 8.4 s | sat 0.53 s | sat 0.40 s | sat 14.4 ms |
| `fib_selfify-eab4088540ea` | unknown | 8.3 s | sat 0.52 s | sat 0.40 s | sat 11.7 ms |
| `fib_selfify-07c50764735b` | unknown | 6.7 s | sat 0.49 s | sat 0.43 s | sat 31.1 ms |
| `fib_memoized2-af745873ef16` | unsat | 6.7 s | unsat 0.33 s | unsat 0.34 s | unsat 0.6 ms |
| `fib_selfify-f385662dc903` | unknown | 6.6 s | sat 0.51 s | sat 0.41 s | sat 12.3 ms |
| `fib_selfify-474421e51f45` | unknown | 6.6 s | sat 0.52 s | sat 0.39 s | sat 33.1 ms |
| `fib_selfify-97e265dade8e` | unknown | 6.6 s | sat 0.49 s | sat 0.38 s | sat 14.2 ms |
| `fib_selfify-d493c8b11d25` | unknown | 6.6 s | sat 0.52 s | sat 0.40 s | sat 14.2 ms |
| `fib_selfify-708ac187768b` | unknown | 6.6 s | sat 0.51 s | sat 0.40 s | sat 32.6 ms |
| `subtyping_normalization-2855607370bf` | unsat | 2.9 s | unsat 0.28 s | unsat 0.28 s | unsat 0.2 ms |
| `fib_memoized2-1600ee1bff39` | unknown | 2.1 s | sat 0.36 s | sat 0.36 s | sat 3.3 ms |
| `subtyping_normalization-aefd11751e2a` | unsat | 1.9 s | unsat 0.29 s | unsat 0.27 s | unsat 0.2 ms |
