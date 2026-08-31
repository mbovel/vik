---
title: Evaluation
---

# Evaluation

What VIK costs, measured two ways, against the solvers a verification tool would otherwise call. The first
way is the one that matters when the solver is embedded: the process is already running and warm, and each
question is a call. The second is the one competitions measure: a new process per question, start-up
included.

Everything here is reproducible from the repository; the commands are at the end, and the raw data is
committed next to the results.

## What was measured

Sixteen instances of the [SMT-LIB library](https://smt-lib.org), release 2025, spread over the logics VIK
supports and small enough that the comparison is about the solvers rather than about one hard instance: four
`QF_UF`, four `QF_LIA`, three `QF_UFLIA`, two `QF_DT`, three `QF_BV`. They are part of the
[corpus](https://github.com/mbovel/vik/tree/main/smtlib) the tests play, with their families and licences.
The set is deliberately of the kind a verifier produces — many small queries — and it is not a sample of the
competition's hard tail, where VIK does not compete (see [limitations](#limitations)).

Two families of solvers appear:

- **VIK** on its three platforms: the JVM, a native binary (Scala Native, LLVM), and Node.js (Scala.js,
  linked to WebAssembly).
- **Others**: z3 and cvc5 (the binaries, and in process through the JavaSMT bindings), princess and
  SMTInterpol (JVM solvers, in process and from the command line).

## The machine and the versions

| | |
|---|---|
| Machine | AMD EPYC 9254 (24 cores, 96 threads), 128 GB, shared with other users |
| System | Ubuntu 24.04.4 LTS, Linux 6.8.0 |
| JVM | OpenJDK 17.0.20 (Temurin build of the distribution) |
| Node.js | 26.5.0 |
| Scala | 3.10.0-RC1 (3.9.0 for the native build), Scala.js 1.22.0, Scala Native 0.5.12, LLVM 18.1.8 |
| Build | sbt 2.0.8, JMH through sbt-jmh 0.4.8 |
| VIK | this repository, commit of the run |
| z3 | 4.13.0 (binary), `org.sosy-lab:javasmt-solver-z3:4.14.0` in process |
| cvc5 | 1.1.2 (binary), `org.sosy-lab:javasmt-solver-cvc5:1.2.1-g8594a8e4dc` in process |
| princess | `io.github.uuverifiers:princess:2026-05-20` |
| SMTInterpol | `de.uni-freiburg.informatik.ultimate:smtinterpol:2.5-916-ga5843d8b` |

The machine is shared, so numbers taken hours apart are not comparable; every table here comes from one run,
and the two tables were taken within the same hour.

## Warm: the solver as a library

The measurement is JMH in the same JVM: two forks, three warm-up and three measurement iterations of one
second each, average time per operation, one thread. Every operation is a complete answer — parse the script,
build the problem, solve it — on a solver built for that call, which is what a verification tool does per
query. The number after ± is a 95% confidence interval (Student's t over the six iterations).

| Instance | VIK | VIK, parsing | z3, API | z3, parsing | cvc5 | princess | SMTInterpol |
|---|---:|---:|---:|---:|---:|---:|---:|
| `QG-classification__qg5__iso_brn001` | 0.731 ± 0.078 | 1.478 ± 0.114 | 3.428 ± 0.110 | 2.799 ± 0.007 | 8.423 ± 0.513 | 59.754 ± 8.163 | 4.089 ± 0.178 |
| `2018-Goel-hwbench__QF_UF_bug-1_ab_reg_max` | 0.005 ± 0.001 | 0.030 ± 0.001 | 0.788 ± 0.003 | 0.915 ± 0.002 | 1.780 ± 0.201 | 1.158 ± 0.111 | 0.182 ± 0.003 |
| `TypeSafe__z3.1184131` | 0.006 ± 0.001 | 0.034 ± 0.002 | 0.726 ± 0.004 | 0.842 ± 0.002 | 1.736 ± 0.195 | 2.194 ± 0.161 | 0.197 ± 0.002 |
| `CAV_2009_benchmarks__20-vars__problem__001` | 2.485 ± 0.084 | 2.743 ± 0.224 | 2.459 ± 0.008 | 2.625 ± 0.010 | 27.975 ± 0.297 | 17.757 ± 0.592 | 1.100 ± 0.067 |
| `dillig__20-4` | 9.487 ± 0.312 | 9.663 ± 0.079 | 2.623 ± 0.010 | 2.584 ± 0.008 | 32.038 ± 0.383 | 16.704 ± 0.664 | 1.658 ± 0.051 |
| `prime-cone__prime_cone_unsat_3` | 0.051 ± 0.003 | 0.079 ± 0.009 | 2.031 ± 0.006 | 2.125 ± 0.006 | 3.009 ± 0.043 | 0.853 ± 0.054 | 0.263 ± 0.009 |
| `convert__convert-tiff2jpg-query-1831` | 0.392 ± 0.021 | 0.673 ± 0.007 | 2.032 ± 0.017 | 1.782 ± 0.012 | 4.423 ± 0.669 | 8.537 ± 0.297 | 1.623 ± 0.026 |
| `wisas__xs_8_8` | 27.691 ± 0.897 | 32.877 ± 6.489 | 5.912 ± 0.113 | 3.222 ± 0.010 | 9.433 ± 0.663 | 33.295 ± 6.081 | 4.057 ± 0.036 |
| `TwoSquares__smtlib.598294` | 0.005 ± 0.001 | 0.018 ± 0.002 | 0.947 ± 0.001 | 1.055 ± 0.002 | 1.277 ± 0.134 | 1.897 ± 0.281 | 0.105 ± 0.011 |
| `mathsat__Wisa__xs-09-05-2-4-4-5` | 31.974 ± 0.671 | 32.144 ± 0.683 | 6.570 ± 0.086 | 3.415 ± 0.035 | 11.014 ± 0.385 | 35.920 ± 4.854 | 7.897 ± 1.385 |
| `20172804-Barrett__tests__v10__v10l20080.cvc` | 0.014 ± 0.002 | 0.052 ± 0.001 | 1.005 ± 0.009 | 1.101 ± 0.003 | 2.201 ± 0.257 | 17.583 ± 3.461 | 0.236 ± 0.004 |
| `20230720-blocksworld__from_0_1_3_to_1_1_2_bmc_0` | 0.190 ± 0.020 | 0.299 ± 0.027 | 1.043 ± 0.009 | 1.149 ± 0.005 | 2.226 ± 0.210 | 11.371 ± 1.000 | 0.570 ± 0.018 |
| `sage__app7__bench_8505` | 0.006 ± 0.001 | 0.013 ± 0.001 | — | 0.893 ± 0.004 | 0.934 ± 0.077 | 0.601 ± 0.094 | 0.060 ± 0.002 |
| `bmc-bv__adpcm` | 0.001 ± 0.000 | 0.007 ± 0.001 | — | 0.752 ± 0.002 | 0.873 ± 0.069 | 0.165 ± 0.004 | 0.057 ± 0.002 |
| `NEQ__NEQ004_size4` | 97.984 ± 4.604 | 93.617 ± 7.299 | 12.173 ± 0.091 | 11.154 ± 0.017 | 109.385 ± 0.465 | — | 126.903 ± 0.476 |

Every solver receives the file as it is and parses it, except the column *z3, API*, where the problem is
built through z3's API from VIK's syntax tree — the difference between the two z3 columns is what parsing
costs. For a bit-vector instance that tree is already lowered to integers, so *z3, API* is not the same
question there and the cell is left out.

Two instances are not in this table. `pspace/ndist.a.20000` because the in-process bindings do not survive
it: cvc5 answers `Unsupported internal sort: (_ BitVec 20000)` and SMTInterpol answers `sat` where its own
binary answers `unsat` — the binaries are measured in the cold table instead. `NEQ004_size4` is in the table
without princess, which needs minutes per operation on it.

Read the two VIK columns together: the first is the solver on a problem it already holds, which is how a tool
that builds the terms itself calls it, and the second adds the parsing of the script, which is what the
columns of the solvers that parse include. The gap between them is the front end — tens of microseconds on
the small instances.

On the queries a verifier produces — the small ones, a millisecond or less — VIK is the fastest of the six,
by one to two orders of magnitude: 5 µs where z3 takes 790 µs and cvc5 1.8 ms on `bug-1_ab_reg_max`, 1 µs
against 750 µs and 870 µs on `bmc-bv/adpcm`. That is not a statement about the solving algorithms but about
what a call costs: z3 and cvc5 pay a context and a JNI crossing per query, and their engines are built for
much larger problems than these.

Where the instance has real work in it, the ranking turns around, and by more than the factor of five the
project aims at: `dillig/20-4` is 9.5 ms for VIK against 2.6 ms for z3, `wisas/xs_8_8` 28 ms against 3.2 ms,
`NEQ004_size4` 98 ms against 11 ms. The two `wisas` instances are the honest worst case here — an arrangement
of shared integer terms that grows with the square of their number — and they are in the table for that
reason; they are also where the arithmetic's [propagation of the bounds it already
knows](https://github.com/mbovel/vik/blob/main/bench/results/smt/2026-08-31-laraserver4-bound-propagation.md)
earns its keep (without it they measured half again slower).

The middle ground is where the design shows: `CAV_2009/20-vars` is a tie with z3 (2.5 ms for both),
`prime_cone_unsat_3` and `convert-tiff2jpg` are three to five times faster than z3, and the datatype
instances are one to two orders of magnitude faster than princess, which is the other solver written for the
JVM.

## Cold: a process per question

The measurement is the one a competition makes: a new process for every instance, the wall-clock time of the
whole answer, three repetitions, the median reported (a scheduling hiccup then decides nothing), with a limit
of 30 seconds. This is where start-up appears, and it is what a tool pays when it shells out to a solver.

| Instance | Status | VIK (JVM) | VIK (native) | VIK (Node) | z3 | cvc5 | princess | SMTInterpol |
|---|---|---:|---:|---:|---:|---:|---:|---:|
| `NEQ__NEQ004_size4` | unsat | 0.632 | 1.251 | 0.458 | 0.020 | 0.036 | timeout | 0.369 |
| `QG-classification__qg5__iso_brn001` | sat | 0.304 | 0.052 | 0.088 | 0.016 | 0.011 | 0.891 | 0.151 |
| `2018-Goel-hwbench__QF_UF_bug-1_ab_reg_max` | sat | 0.202 | 0.009 | 0.058 | 0.014 | 0.007 | 0.504 | 0.095 |
| `TypeSafe__z3.1184131` | unsat | 0.226 | 0.008 | 0.059 | 0.015 | 0.008 | 0.492 | 0.106 |
| `CAV_2009_benchmarks__20-vars__problem__001` | sat | 0.290 | 0.030 | 0.083 | 0.018 | 0.029 | 0.565 | 0.127 |
| `dillig__20-4` | sat | 0.320 | 0.071 | 0.113 | 0.018 | 0.032 | 0.590 | 0.138 |
| `prime-cone__prime_cone_unsat_3` | unsat | 0.240 | 0.009 | 0.070 | 0.017 | 0.008 | 0.492 | 0.111 |
| `convert__convert-tiff2jpg-query-1831` | unsat | 0.295 | 0.024 | 0.078 | 0.016 | 0.008 | 0.552 | 0.131 |
| `wisas__xs_8_8` | sat | 0.520 | 0.275 | 0.207 | 0.025 | 0.013 | 0.880 | 0.181 |
| `TwoSquares__smtlib.598294` | sat | 0.249 | 0.008 | 0.062 | 0.022 | 0.006 | 0.584 | 0.109 |
| `mathsat__Wisa__xs-09-05-2-4-4-5` | sat | 0.545 | 0.315 | 0.212 | 0.024 | 0.015 | 0.890 | 0.198 |
| `20172804-Barrett__tests__v10__v10l20080.cvc` | sat | 0.243 | 0.009 | 0.061 | 0.022 | 0.008 | 0.788 | 0.109 |
| `20230720-blocksworld__from_0_1_3_to_1_1_2_bmc_0` | unsat | 0.288 | 0.014 | 0.068 | 0.022 | 0.007 | 0.659 | 0.120 |
| `sage__app7__bench_8505` | sat | 0.217 | 0.007 | 0.058 | 0.015 | 0.006 | 0.523 | 0.086 |
| `bmc-bv__adpcm` | sat | 0.206 | 0.007 | 0.056 | 0.015 | 0.006 | 0.455 | 0.099 |
| `pspace__ndist.a.20000` | unsat | 0.256 | 0.016 | 0.072 | timeout | 0.007 | 0.580 | **sat** 0.086 |

The floors say most of it: about 200 ms for a JVM process (the JVM's own start-up), about 56 ms for Node with
the WebAssembly module, and about 7 ms for the native binary — the same order as z3 (14–25 ms) and cvc5
(6–36 ms), which are C++ binaries. On the instances that need real work the ranking changes: the JVM's
just-in-time compiler pays off (`NEQ004`: 0.63 s against 1.25 s native), while the native binary and Node
never warm up.

`pspace/ndist.a.20000` is the instance where the fragment matters more than the engine: 20 000-bit vectors,
which VIK lowers to integers and answers in 16 ms as a native binary, while z3 bit-blasts and does not
finish in 30 seconds.

## Limitations

- One machine, shared with other users; the numbers are medians and confidence intervals, not guarantees.
- Sixteen instances, chosen small and inside VIK's fragment. On the competition's hard instances VIK is far
  behind z3 and cvc5, and on some of them it does not finish at all — the
  [corpus](https://github.com/mbovel/vik/tree/main/smtlib) records that: of its 688 files, 49 are marked as
  out of the solver's reach.
- The in-process z3 and cvc5 go through the JavaSMT bindings, which cross the JNI boundary per call; their
  binaries are what the cold table measures. Both are reported.
- VIK's answers are checked against the status of every file, on every run of the test suite; a wrong answer
  would show as such in these tables (none does).

## Reproducing

```sh
# warm, in process: JMH, two forks, three plus three iterations
sbt "bench/Jmh/run -f 2 -wi 3 -i 3 -rf json -rff warm.json -p file=<instances> -e .*print.* .*OtherSolversBench.*"
python3 bench/warm2md.py warm.json

# cold, one process per run: three repetitions, the median
bench/cold.sh instances.txt 3 30 > cold.tsv
python3 bench/cold2md.py cold.tsv

# the native binary and the WebAssembly module the cold run uses
sbt "smt2Native/nativeLink; docsAssets"
```

The raw data of this page, committed in the repository:
[cold](https://github.com/mbovel/vik/blob/main/bench/results/smt/2026-08-31-laraserver4-cold.tsv),
[warm](https://github.com/mbovel/vik/blob/main/bench/results/smt/2026-08-31-laraserver4-warm.json),
[warm, `NEQ004`](https://github.com/mbovel/vik/blob/main/bench/results/smt/2026-08-31-laraserver4-warm-neq.json).
