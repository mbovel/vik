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
| `QG-classification__qg5__iso_brn001` | 0.755 ± 0.104 | 1.448 ± 0.055 | 3.414 ± 0.102 | 2.806 ± 0.010 | 8.308 ± 0.476 | 59.956 ± 7.886 | 3.931 ± 0.060 |
| `2018-Goel-hwbench__QF_UF_bug-1_ab_reg_max` | 0.005 ± 0.001 | 0.030 ± 0.001 | 0.786 ± 0.001 | 0.915 ± 0.002 | 1.772 ± 0.184 | 1.143 ± 0.095 | 0.184 ± 0.015 |
| `TypeSafe__z3.1184131` | 0.006 ± 0.001 | 0.034 ± 0.002 | 0.722 ± 0.001 | 0.843 ± 0.003 | 1.728 ± 0.191 | 2.273 ± 0.190 | 0.199 ± 0.006 |
| `CAV_2009_benchmarks__20-vars__problem__001` | 2.373 ± 0.159 | 2.678 ± 0.111 | 2.466 ± 0.011 | 2.645 ± 0.034 | 27.763 ± 0.212 | 18.320 ± 0.814 | 1.009 ± 0.005 |
| `dillig__20-4` | 10.055 ± 0.652 | 9.589 ± 0.088 | 2.623 ± 0.010 | 2.602 ± 0.016 | 31.916 ± 0.525 | 16.535 ± 0.939 | 1.669 ± 0.064 |
| `prime-cone__prime_cone_unsat_3` | 0.053 ± 0.008 | 0.079 ± 0.010 | 2.013 ± 0.006 | 2.123 ± 0.012 | 3.008 ± 0.081 | 0.865 ± 0.051 | 0.290 ± 0.046 |
| `convert__convert-tiff2jpg-query-1831` | 0.395 ± 0.016 | 0.692 ± 0.011 | 2.014 ± 0.017 | 1.781 ± 0.007 | 4.432 ± 0.681 | 8.510 ± 0.244 | 1.599 ± 0.044 |
| `wisas__xs_8_8` | 64.909 ± 6.990 | 65.677 ± 8.306 | 5.959 ± 0.104 | 3.222 ± 0.011 | 9.429 ± 0.615 | 33.116 ± 5.622 | 4.170 ± 0.171 |
| `TwoSquares__smtlib.598294` | 0.004 ± 0.000 | 0.018 ± 0.002 | 0.943 ± 0.003 | 1.059 ± 0.004 | 1.275 ± 0.122 | 1.841 ± 0.261 | 0.102 ± 0.002 |
| `mathsat__Wisa__xs-09-05-2-4-4-5` | 58.435 ± 1.913 | 63.769 ± 4.213 | 6.539 ± 0.079 | 3.391 ± 0.009 | 10.957 ± 0.275 | 35.280 ± 4.909 | 8.446 ± 2.215 |
| `20172804-Barrett__tests__v10__v10l20080.cvc` | 0.014 ± 0.001 | 0.056 ± 0.005 | 1.000 ± 0.007 | 1.100 ± 0.003 | 2.194 ± 0.249 | 17.546 ± 2.801 | 0.227 ± 0.007 |
| `20230720-blocksworld__from_0_1_3_to_1_1_2_bmc_0` | 0.188 ± 0.023 | 0.307 ± 0.037 | 1.051 ± 0.008 | 1.151 ± 0.002 | 2.219 ± 0.207 | 10.955 ± 0.794 | 0.535 ± 0.007 |
| `sage__app7__bench_8505` | 0.006 ± 0.001 | 0.013 ± 0.001 | — | 0.901 ± 0.005 | 0.927 ± 0.079 | 0.599 ± 0.078 | 0.060 ± 0.004 |
| `bmc-bv__adpcm` | 0.001 ± 0.000 | 0.007 ± 0.000 | — | 0.749 ± 0.003 | 0.872 ± 0.069 | 0.171 ± 0.005 | 0.055 ± 0.007 |
| `NEQ__NEQ004_size4` | 97.706 ± 5.225 | 93.936 ± 5.508 | 12.210 ± 0.132 | 11.128 ± 0.053 | 109.026 ± 0.716 | — | 126.407 ± 0.467 |

Every solver receives the file as it is and parses it, except the column *z3 (built)*, where the problem is
built through z3's API from VIK's syntax tree — the difference between the two z3 columns is what parsing
costs. For a bit-vector instance that tree is already lowered to integers, so *z3 (built)* is not the same
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
project aims at: `dillig/20-4` is 10 ms for VIK against 2.6 ms for z3, `wisas/xs_8_8` 65 ms against 3.2 ms,
`NEQ004_size4` 98 ms against 11 ms. The two `wisas` instances are the honest worst case here — an arrangement
of shared integer terms that grows with the square of their number — and they are in the table for that
reason.

The middle ground is where the design shows: `CAV_2009/20-vars` is a tie with z3 (2.4 ms against 2.5 ms),
`prime_cone_unsat_3` and `convert-tiff2jpg` are three to five times faster than z3, and the datatype
instances are one to two orders of magnitude faster than princess, which is the other solver written for the
JVM.

## Cold: a process per question

The measurement is the one a competition makes: a new process for every instance, the wall-clock time of the
whole answer, three repetitions, the median reported (a scheduling hiccup then decides nothing), with a limit
of 30 seconds. This is where start-up appears, and it is what a tool pays when it shells out to a solver.

| Instance | Status | VIK (JVM) | VIK (native) | VIK (Node) | z3 | cvc5 | princess | SMTInterpol |
|---|---|---:|---:|---:|---:|---:|---:|---:|
| `NEQ__NEQ004_size4` | unsat | 0.613 | 1.247 | 0.451 | 0.020 | 0.035 | timeout | 0.374 |
| `QG-classification__qg5__iso_brn001` | sat | 0.307 | 0.052 | 0.102 | 0.016 | 0.011 | 0.884 | 0.146 |
| `2018-Goel-hwbench__QF_UF_bug-1_ab_reg_max` | sat | 0.199 | 0.008 | 0.062 | 0.015 | 0.006 | 0.492 | 0.095 |
| `TypeSafe__z3.1184131` | unsat | 0.226 | 0.008 | 0.064 | 0.014 | 0.006 | 0.501 | 0.106 |
| `CAV_2009_benchmarks__20-vars__problem__001` | sat | 0.289 | 0.030 | 0.089 | 0.018 | 0.029 | 0.573 | 0.129 |
| `dillig__20-4` | sat | 0.323 | 0.070 | 0.111 | 0.017 | 0.032 | 0.581 | 0.136 |
| `prime-cone__prime_cone_unsat_3` | unsat | 0.234 | 0.009 | 0.064 | 0.017 | 0.008 | 0.483 | 0.107 |
| `convert__convert-tiff2jpg-query-1831` | unsat | 0.285 | 0.024 | 0.078 | 0.016 | 0.008 | 0.552 | 0.133 |
| `wisas__xs_8_8` | sat | 0.611 | 0.604 | 0.293 | 0.024 | 0.013 | 0.859 | 0.178 |
| `TwoSquares__smtlib.598294` | sat | 0.244 | 0.007 | 0.068 | 0.022 | 0.007 | 0.557 | 0.098 |
| `mathsat__Wisa__xs-09-05-2-4-4-5` | sat | 0.606 | 0.551 | 0.283 | 0.024 | 0.014 | 0.912 | 0.198 |
| `20172804-Barrett__tests__v10__v10l20080.cvc` | sat | 0.244 | 0.009 | 0.061 | 0.022 | 0.007 | 0.772 | 0.108 |
| `20230720-blocksworld__from_0_1_3_to_1_1_2_bmc_0` | unsat | 0.274 | 0.014 | 0.073 | 0.021 | 0.008 | 0.662 | 0.118 |
| `sage__app7__bench_8505` | sat | 0.215 | 0.008 | 0.064 | 0.015 | 0.007 | 0.507 | 0.095 |
| `bmc-bv__adpcm` | sat | 0.206 | 0.007 | 0.055 | 0.013 | 0.005 | 0.444 | 0.096 |
| `pspace__ndist.a.20000` | unsat | 0.253 | 0.016 | 0.071 | timeout | 0.007 | 0.584 | **sat** 0.085 |

The floors say most of it: about 190 ms for a JVM process (the JVM's own start-up), about 55 ms for Node with
the WebAssembly module, and about 7 ms for the native binary — the same order as z3 (13–22 ms) and cvc5
(5–35 ms), which are C++ binaries. On the instances that need real work the ranking changes: the JVM's
just-in-time compiler pays off (`NEQ004`: 0.61 s against 1.25 s native), while the native binary and Node
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
[warm, `NEQ004`](https://github.com/mbovel/vik/blob/main/bench/results/smt/2026-08-31-laraserver4-warm-neq.json),
[warm, VIK with parsing](https://github.com/mbovel/vik/blob/main/bench/results/smt/2026-08-31-laraserver4-warm-vik.json).
