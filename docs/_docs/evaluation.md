---
title: Evaluation
extraCSS:
  - css/tables.css
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
query. Times are milliseconds; the best cell of each row is green.

| Instance | Theory | Incremental | Status | VIK (API) | VIK (SMT2) | z3 (API) | z3 (SMT2) | cvc5 (SMT2) | princess (SMT2) | SMTInterpol (SMT2) |
|---|---|---|---|---:|---:|---:|---:|---:|---:|---:|
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UF/smt-lib/QG-classification__qg5__iso_brn001.smt2"><code>QG-classification__<wbr>qg5__<wbr>iso_brn001</code></a> | QF_UF | ✗ | sat | <span class="best">0.73</span> | 1.48 | 3.43 | 2.80 | 8.42 | 59.75 | 4.09 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UF/smt-lib/2018-Goel-hwbench__QF_UF_bug-1_ab_reg_max.smt2"><code>2018-Goel-hwbench__<wbr>QF_UF_bug-1_ab_reg_max</code></a> | QF_UF | ✗ | sat | <span class="best"><0.01</span> | 0.03 | 0.79 | 0.91 | 1.78 | 1.16 | 0.18 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UF/smt-lib/TypeSafe__z3.1184131.smt2"><code>TypeSafe__<wbr>z3.1184131</code></a> | QF_UF | ✗ | unsat | <span class="best">0.01</span> | 0.03 | 0.73 | 0.84 | 1.74 | 2.19 | 0.20 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_LIA/smt-lib/CAV_2009_benchmarks__20-vars__problem__001.smt2"><code>CAV_2009_benchmarks__<wbr>20-vars__<wbr>problem__<wbr>001</code></a> | QF_LIA | ✗ | sat | 2.48 | 2.74 | 2.46 | 2.63 | 27.98 | 17.76 | <span class="best">1.10</span> |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_LIA/smt-lib/dillig__20-4.smt2"><code>dillig__<wbr>20-4</code></a> | QF_LIA | ✗ | sat | 9.49 | 9.66 | 2.62 | 2.58 | 32.04 | 16.70 | <span class="best">1.66</span> |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_LIA/smt-lib/prime-cone__prime_cone_unsat_3.smt2"><code>prime-cone__<wbr>prime_cone_unsat_3</code></a> | QF_LIA | ✗ | unsat | <span class="best">0.05</span> | 0.08 | 2.03 | 2.12 | 3.01 | 0.85 | 0.26 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_LIA/smt-lib/convert__convert-tiff2jpg-query-1831.smt2"><code>convert__<wbr>convert-tiff2jpg-query-1831</code></a> | QF_LIA | ✗ | unsat | <span class="best">0.39</span> | 0.67 | 2.03 | 1.78 | 4.42 | 8.54 | 1.62 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UFLIA/smt-lib/wisas__xs_8_8.smt2"><code>wisas__<wbr>xs_8_8</code></a> | QF_UFLIA | ✗ | sat | 27.69 | 32.88 | 5.91 | <span class="best">3.22</span> | 9.43 | 33.29 | 4.06 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UFLIA/smt-lib/TwoSquares__smtlib.598294.smt2"><code>TwoSquares__<wbr>smtlib.598294</code></a> | QF_UFLIA | ✗ | sat | <span class="best"><0.01</span> | 0.02 | 0.95 | 1.06 | 1.28 | 1.90 | 0.11 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UFLIA/smt-lib/mathsat__Wisa__xs-09-05-2-4-4-5.smt2"><code>mathsat__<wbr>Wisa__<wbr>xs-09-05-2-4-4-5</code></a> | QF_UFLIA | ✗ | sat | 31.97 | 32.14 | 6.57 | <span class="best">3.41</span> | 11.01 | 35.92 | 7.90 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_DT/smt-lib/20172804-Barrett__tests__v10__v10l20080.cvc.smt2"><code>20172804-Barrett__<wbr>tests__<wbr>v10__<wbr>v10l20080.cvc</code></a> | QF_DT | ✗ | sat | <span class="best">0.01</span> | 0.05 | 1.01 | 1.10 | 2.20 | 17.58 | 0.24 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_DT/smt-lib/20230720-blocksworld__from_0_1_3_to_1_1_2_bmc_0.smt2"><code>20230720-blocksworld__<wbr>from_0_1_3_to_1_1_2_bmc_0</code></a> | QF_DT | ✗ | unsat | <span class="best">0.19</span> | 0.30 | 1.04 | 1.15 | 2.23 | 11.37 | 0.57 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_BV/smt-lib/sage__app7__bench_8505.smt2"><code>sage__<wbr>app7__<wbr>bench_8505</code></a> | QF_BV | ✗ | sat | <span class="best">0.01</span> | 0.01 | — | 0.89 | 0.93 | 0.60 | 0.06 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_BV/smt-lib/bmc-bv__adpcm.smt2"><code>bmc-bv__<wbr>adpcm</code></a> | QF_BV | ✗ | sat | <span class="best"><0.01</span> | 0.01 | — | 0.75 | 0.87 | 0.16 | 0.06 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UF/smt-lib/NEQ__NEQ004_size4.smt2"><code>NEQ__<wbr>NEQ004_size4</code></a> | QF_UF | ✗ | unsat | 97.98 | 93.62 | 12.17 | <span class="best">11.15</span> | 109.38 | — | 126.90 |

The 95% confidence intervals (Student's t over the samples of a cell) average 6% of the mean; every cell is within 20% of its mean except VIK (API) on `TwoSquares__smtlib.598294` (29%).


The *(SMT2)* columns — cvc5, princess and SMTInterpol included — receive the text of the script and parse
it themselves; the *(API)* columns receive the problem already built: *VIK (API)* solves its own tree, and
*z3 (API)* is handed that tree through z3's Java API, so the difference between the two z3 columns is what
parsing costs. For a bit-vector instance the tree is already lowered to integers, so *z3 (API)* is not the
same question there and the cell is left out.

Two instances are not in this table. `pspace/ndist.a.20000` because the in-process bindings do not survive
it: cvc5 answers `Unsupported internal sort: (_ BitVec 20000)` and SMTInterpol answers `sat` where its own
binary answers `unsat` — the binaries are measured in the cold table instead. `NEQ004_size4` is in the table
without princess, which needs minutes per operation on it.

Read the two VIK columns together: *VIK (API)* is the solver on a problem it already holds, which is how a
tool that builds the terms itself calls it, and *VIK (SMT2)* adds the parsing of the script, which is what
the other *(SMT2)* columns include. The gap between them is the front end — tens of microseconds on
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
whole answer, three repetitions, the median reported in seconds (a scheduling hiccup then decides nothing),
with a limit of 30 seconds; the best correct answer of each row is green. This is where start-up appears, and it is what a tool pays when it shells out to a solver.

| Instance | Theory | Incremental | Status | VIK (JVM) | VIK (native) | VIK (Node) | z3 | cvc5 | princess | SMTInterpol |
|---|---|---|---|---:|---:|---:|---:|---:|---:|---:|
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UF/smt-lib/NEQ__NEQ004_size4.smt2"><code>NEQ__<wbr>NEQ004_size4</code></a> | QF_UF | ✗ | unsat | 0.63 | 1.25 | 0.46 | <span class="best">0.02</span> | 0.04 | timeout | 0.37 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UF/smt-lib/QG-classification__qg5__iso_brn001.smt2"><code>QG-classification__<wbr>qg5__<wbr>iso_brn001</code></a> | QF_UF | ✗ | sat | 0.30 | 0.05 | 0.09 | 0.02 | <span class="best">0.01</span> | 0.89 | 0.15 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UF/smt-lib/2018-Goel-hwbench__QF_UF_bug-1_ab_reg_max.smt2"><code>2018-Goel-hwbench__<wbr>QF_UF_bug-1_ab_reg_max</code></a> | QF_UF | ✗ | sat | 0.20 | 0.01 | 0.06 | 0.01 | <span class="best">0.01</span> | 0.50 | 0.10 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UF/smt-lib/TypeSafe__z3.1184131.smt2"><code>TypeSafe__<wbr>z3.1184131</code></a> | QF_UF | ✗ | unsat | 0.23 | <span class="best">0.01</span> | 0.06 | 0.01 | <span class="best">0.01</span> | 0.49 | 0.11 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_LIA/smt-lib/CAV_2009_benchmarks__20-vars__problem__001.smt2"><code>CAV_2009_benchmarks__<wbr>20-vars__<wbr>problem__<wbr>001</code></a> | QF_LIA | ✗ | sat | 0.29 | 0.03 | 0.08 | <span class="best">0.02</span> | 0.03 | 0.56 | 0.13 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_LIA/smt-lib/dillig__20-4.smt2"><code>dillig__<wbr>20-4</code></a> | QF_LIA | ✗ | sat | 0.32 | 0.07 | 0.11 | <span class="best">0.02</span> | 0.03 | 0.59 | 0.14 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_LIA/smt-lib/prime-cone__prime_cone_unsat_3.smt2"><code>prime-cone__<wbr>prime_cone_unsat_3</code></a> | QF_LIA | ✗ | unsat | 0.24 | 0.01 | 0.07 | 0.02 | <span class="best">0.01</span> | 0.49 | 0.11 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_LIA/smt-lib/convert__convert-tiff2jpg-query-1831.smt2"><code>convert__<wbr>convert-tiff2jpg-query-1831</code></a> | QF_LIA | ✗ | unsat | 0.29 | 0.02 | 0.08 | 0.02 | <span class="best">0.01</span> | 0.55 | 0.13 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UFLIA/smt-lib/wisas__xs_8_8.smt2"><code>wisas__<wbr>xs_8_8</code></a> | QF_UFLIA | ✗ | sat | 0.52 | 0.28 | 0.21 | 0.03 | <span class="best">0.01</span> | 0.88 | 0.18 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UFLIA/smt-lib/TwoSquares__smtlib.598294.smt2"><code>TwoSquares__<wbr>smtlib.598294</code></a> | QF_UFLIA | ✗ | sat | 0.25 | 0.01 | 0.06 | 0.02 | <span class="best">0.01</span> | 0.58 | 0.11 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UFLIA/smt-lib/mathsat__Wisa__xs-09-05-2-4-4-5.smt2"><code>mathsat__<wbr>Wisa__<wbr>xs-09-05-2-4-4-5</code></a> | QF_UFLIA | ✗ | sat | 0.55 | 0.32 | 0.21 | 0.02 | <span class="best">0.01</span> | 0.89 | 0.20 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_DT/smt-lib/20172804-Barrett__tests__v10__v10l20080.cvc.smt2"><code>20172804-Barrett__<wbr>tests__<wbr>v10__<wbr>v10l20080.cvc</code></a> | QF_DT | ✗ | sat | 0.24 | 0.01 | 0.06 | 0.02 | <span class="best">0.01</span> | 0.79 | 0.11 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_DT/smt-lib/20230720-blocksworld__from_0_1_3_to_1_1_2_bmc_0.smt2"><code>20230720-blocksworld__<wbr>from_0_1_3_to_1_1_2_bmc_0</code></a> | QF_DT | ✗ | unsat | 0.29 | 0.01 | 0.07 | 0.02 | <span class="best">0.01</span> | 0.66 | 0.12 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_BV/smt-lib/sage__app7__bench_8505.smt2"><code>sage__<wbr>app7__<wbr>bench_8505</code></a> | QF_BV | ✗ | sat | 0.22 | 0.01 | 0.06 | 0.01 | <span class="best">0.01</span> | 0.52 | 0.09 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_BV/smt-lib/bmc-bv__adpcm.smt2"><code>bmc-bv__<wbr>adpcm</code></a> | QF_BV | ✗ | sat | 0.21 | 0.01 | 0.06 | 0.01 | <span class="best">0.01</span> | 0.46 | 0.10 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_BV/smt-lib/pspace__ndist.a.20000.smt2"><code>pspace__<wbr>ndist.a.20000</code></a> | QF_BV | ✗ | unsat | 0.26 | 0.02 | 0.07 | timeout | <span class="best">0.01</span> | 0.58 | **sat** 0.09 |

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
