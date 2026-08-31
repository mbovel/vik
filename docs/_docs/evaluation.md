---
title: Evaluation
extraCSS:
  - css/site.css
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

Twenty instances of the [SMT-LIB library](https://smt-lib.org), release 2025, spread over the logics VIK
supports and small enough that the comparison is about the solvers rather than about one hard instance: five
`QF_UF`, four `QF_LIA`, six `QF_UFLIA`, three with datatypes (one of them `QF_UFDTLIA`), three `QF_BV`.
Three are incremental scripts — several `check-sat`s under `push`/`pop`, the *Incremental* column — and
there the whole script is the question. They are part of the
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
| z3 | 4.14.0 (the binary and, in process, `org.sosy-lab:javasmt-solver-z3:4.14.0`) |
| cvc5 | 1.2.1 (the binary and, in process, `org.sosy-lab:javasmt-solver-cvc5:1.2.1-g8594a8e4dc`) |
| princess | `io.github.uuverifiers:princess:2026-05-20` |
| SMTInterpol | `de.uni-freiburg.informatik.ultimate:smtinterpol:2.5-916-ga5843d8b` |

The machine is shared, so numbers taken hours apart are not comparable; every table here comes from one run,
and the two tables were taken within the same hour.

## Warm: the solver as a library

The measurement is JMH in the same JVM: two forks, three warm-up and three measurement iterations of one
second each, average time per operation, one thread. Every operation is a complete answer — parse the script,
build the problem, solve it — on a solver built for that call, which is what a verification tool does per
query.

**Times in milliseconds — lower is better;** the best cell of a row is green.

| Instance | Theory | Incremental | Status | VIK (API) | VIK (SMT2) | z3 (API) | z3 (SMT2) | cvc5 (SMT2) | princess (SMT2) | SMTInterpol (SMT2) |
|---|---|---|---|---:|---:|---:|---:|---:|---:|---:|
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UF/smt-lib/QG-classification__qg5__iso_brn001.smt2" title="QG-classification__qg5__iso_brn001">QG-classification__<wbr>qg5__<wbr>…</a> | QF_UF | ✗ | sat | <span class="best">0.78</span> | 1.45 | 2.80 | 2.80 | 8.34 | 60.95 | 3.92 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UF/smt-lib/2018-Goel-hwbench__QF_UF_bug-1_ab_reg_max.smt2" title="2018-Goel-hwbench__QF_UF_bug-1_ab_reg_max">2018-Goel-hwbench__<wbr>QF_UF…</a> | QF_UF | ✗ | sat | <span class="best">0.01</span> | 0.03 | 0.78 | 0.91 | 1.81 | 1.14 | 0.18 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UF/smt-lib/TypeSafe__z3.1184131.smt2" title="TypeSafe__z3.1184131">TypeSafe__<wbr>z3.1184131</a> | QF_UF | ✗ | unsat | <span class="best">0.01</span> | 0.03 | 0.72 | 0.84 | 1.74 | 2.26 | 0.19 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_LIA/smt-lib/CAV_2009_benchmarks__20-vars__problem__001.smt2" title="CAV_2009_benchmarks__20-vars__problem__001">CAV_2009_benchmarks__<wbr>20-…</a> | QF_LIA | ✗ | sat | 2.39 | 2.46 | 2.43 | 2.62 | 27.88 | 17.11 | <span class="best">1.04</span> |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_LIA/smt-lib/dillig__20-4.smt2" title="dillig__20-4">dillig__<wbr>20-4</a> | QF_LIA | ✗ | sat | 10.43 | 10.12 | 2.60 | 2.58 | 31.67 | 16.22 | <span class="best">1.68</span> |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_LIA/smt-lib/prime-cone__prime_cone_unsat_3.smt2" title="prime-cone__prime_cone_unsat_3">prime-cone__<wbr>prime_cone_u…</a> | QF_LIA | ✗ | unsat | <span class="best">0.05</span> | 0.07 | 2.01 | 2.16 | 3.03 | 0.87 | 0.26 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_LIA/smt-lib/convert__convert-tiff2jpg-query-1831.smt2" title="convert__convert-tiff2jpg-query-1831">convert__<wbr>convert-tiff2jp…</a> | QF_LIA | ✗ | unsat | <span class="best">0.41</span> | 0.75 | 1.86 | 1.78 | 4.49 | 8.67 | 1.59 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UFLIA/smt-lib/wisas__xs_8_8.smt2" title="wisas__xs_8_8">wisas__<wbr>xs_8_8</a> | QF_UFLIA | ✗ | sat | 27.56 | 31.05 | 5.56 | <span class="best">3.22</span> | 9.43 | 32.80 | 4.05 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UFLIA/smt-lib/TwoSquares__smtlib.598294.smt2" title="TwoSquares__smtlib.598294">TwoSquares__<wbr>smtlib.598294</a> | QF_UFLIA | ✗ | sat | <span class="best"><0.01</span> | 0.02 | 0.95 | 1.06 | 1.29 | 1.95 | 0.11 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UFLIA/smt-lib/mathsat__Wisa__xs-09-05-2-4-4-5.smt2" title="mathsat__Wisa__xs-09-05-2-4-4-5">mathsat__<wbr>Wisa__<wbr>xs-09-05-…</a> | QF_UFLIA | ✗ | sat | 31.42 | 32.37 | 6.29 | <span class="best">3.38</span> | 10.92 | 35.83 | 7.00 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_DT/smt-lib/20172804-Barrett__tests__v10__v10l20080.cvc.smt2" title="20172804-Barrett__tests__v10__v10l20080.cvc">20172804-Barrett__<wbr>tests_…</a> | QF_DT | ✗ | sat | <span class="best">0.01</span> | 0.06 | 1.01 | 1.10 | 2.24 | 17.15 | 0.24 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_DT/smt-lib/20230720-blocksworld__from_0_1_3_to_1_1_2_bmc_0.smt2" title="20230720-blocksworld__from_0_1_3_to_1_1_2_bmc_0">20230720-blocksworld__<wbr>fr…</a> | QF_DT | ✗ | unsat | <span class="best">0.19</span> | 0.30 | 1.05 | 1.15 | 2.23 | 10.95 | 0.54 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_BV/smt-lib/sage__app7__bench_8505.smt2" title="sage__app7__bench_8505">sage__<wbr>app7__<wbr>bench_8505</a> | QF_BV | ✗ | sat | <span class="best">0.01</span> | 0.01 | — | 0.90 | 0.93 | 0.61 | — |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_BV/smt-lib/bmc-bv__adpcm.smt2" title="bmc-bv__adpcm">bmc-bv__<wbr>adpcm</a> | QF_BV | ✗ | sat | <span class="best"><0.01</span> | 0.01 | — | 0.75 | 0.87 | 0.17 | — |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UF/smt-lib-incremental/20190906-CLEARSY__0020__00009.smt2" title="20190906-CLEARSY__0020__00009">20190906-CLEARSY__<wbr>0020__<wbr>…</a> | QF_UF | ✓ | 181 checks | <span class="best">11.97</span> | 14.10 | — | — | 63.28 | — | 23.81 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UFLIA/smt-lib-incremental/kind__MESI_i4.ec.smt2" title="kind__MESI_i4.ec">kind__<wbr>MESI_i4.ec</a> | QF_UFLIA | ✓ | 5 checks | 455.21 | 474.55 | — | — | 102.53 | — | <span class="best">18.36</span> |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UF/smt-lib/NEQ__NEQ004_size4.smt2" title="NEQ__NEQ004_size4">NEQ__<wbr>NEQ004_size4</a> | QF_UF | ✗ | unsat | 98.34 | 102.60 | 11.48 | <span class="best">11.11</span> | 109.42 | — | 127.07 |

The 95% confidence intervals (Student's t over the samples of a cell) average 6% of the mean; every cell is within 20% of its mean except VIK (API) on `TypeSafe__z3.1184131` (28%), VIK (SMT2) on `wisas__xs_8_8` (23%), VIK (SMT2) on `20172804-Barrett__tests__v10__v10l20080.cvc` (23%).





The *(SMT2)* columns — cvc5, princess and SMTInterpol included — receive the text of the script and parse
it themselves; the *(API)* columns receive the problem already built: *VIK (API)* solves its own tree, and
*z3 (API)* is handed that tree through z3's Java API, so the difference between the two z3 columns is what
parsing costs. For a bit-vector instance the tree is already lowered to integers, so *z3 (API)* is not the
same question there and the cell is left out. The incremental instances leave three more cells out: z3's
two columns and princess flatten the script into one problem, which on a script of many `check-sat`s is a
different question again.

Three instances are in the cold table only: `pspace/ndist.a.20000`, whose sort the in-process cvc5 bindings
reject, and `car_6` and the `Certora` one, which ask seconds per operation of VIK. `NEQ004_size4` is in the table without
princess, which needs minutes per operation on it. SMTInterpol's cells on the `QF_BV` instances are left
out of both tables: it has no bit-vector theory — it rejects the declarations and then answers about what
remains (see
[external-issues.md](https://github.com/mbovel/vik/blob/main/external-issues.md)) — so a number there would
not measure solving.

Read the two VIK columns together: *VIK (API)* is the solver on a problem it already holds, which is how a
tool that builds the terms itself calls it, and *VIK (SMT2)* adds the parsing of the script, which is what
the other *(SMT2)* columns include. The gap between them is the front end — tens of microseconds on
the small instances.

On the queries a verifier produces — the small ones, a millisecond or less — VIK is the fastest of the six,
by one to two orders of magnitude: 5 µs where z3 takes 780 µs and cvc5 1.8 ms on `bug-1_ab_reg_max`, 1 µs
against 620 µs and 870 µs on `bmc-bv/adpcm`. That is not a statement about the solving algorithms but about
what a call costs: z3 and cvc5 pay a context and a JNI crossing per query, and their engines are built for
much larger problems than these.

Where the instance has real work in it, the ranking turns around, and by more than the factor of five the
project aims at: `dillig/20-4` is 10 ms for VIK against 2.6 ms for z3, `wisas/xs_8_8` 28 ms against 3.2 ms,
`NEQ004_size4` 98 ms against 11 ms. The two `wisas` instances are the honest worst case here — an arrangement
of shared integer terms that grows with the square of their number — and they are in the table for that
reason; they are also where the arithmetic's [propagation of the bounds it already
knows](https://github.com/mbovel/vik/blob/main/bench/results/smt/2026-08-31-laraserver4-bound-propagation.md)
earns its keep (without it they measured half again slower).

The middle ground is where the design shows: `CAV_2009/20-vars` is a tie with z3 (2.4 ms for both),
`convert-tiff2jpg` is four times and `prime_cone_unsat_3` forty times faster than z3, and the datatype
instances are one to two orders of magnitude faster than princess, which is the other solver written for the
JVM.

The incremental scripts split. On `CLEARSY__0020` — 181 easy `check-sat`s — VIK is the fastest again, 12 ms
for the whole script against 24 ms for SMTInterpol and 63 ms for cvc5: each check is one more small query.
On `kind/MESI_i4` the five checks are real work, and VIK re-encodes and re-learns everything at each
(no state survives a `check-sat` yet): 455 ms where SMTInterpol, which keeps its state, needs 18 ms — the
measured price of the missing incrementality, and the row the planned incremental mode has to move.

## Cold: a process per question

The measurement is the one a competition makes: a new process for every instance, the wall-clock time of the
whole answer, three repetitions, the median reported (a scheduling hiccup then decides nothing), with a
limit of 30 seconds. This is where start-up appears, and it is what a tool pays when it shells out to a
solver.

**Times in seconds — lower is better;** the best correct answer of a row is green.

| Instance | Theory | Incremental | Status | VIK (JVM) | VIK (native) | VIK (Node) | z3 | cvc5 | princess | SMTInterpol |
|---|---|---|---|---:|---:|---:|---:|---:|---:|---:|
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UF/smt-lib/NEQ__NEQ004_size4.smt2" title="NEQ__NEQ004_size4">NEQ__<wbr>NEQ004_size4</a> | QF_UF | ✗ | unsat | 0.63 | 1.26 | 0.45 | <span class="best">0.02</span> | 0.04 | timeout | 0.38 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UF/smt-lib/QG-classification__qg5__iso_brn001.smt2" title="QG-classification__qg5__iso_brn001">QG-classification__<wbr>qg5__<wbr>…</a> | QF_UF | ✗ | sat | 0.31 | 0.05 | 0.09 | 0.02 | <span class="best">0.01</span> | 0.91 | 0.14 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UF/smt-lib/2018-Goel-hwbench__QF_UF_bug-1_ab_reg_max.smt2" title="2018-Goel-hwbench__QF_UF_bug-1_ab_reg_max">2018-Goel-hwbench__<wbr>QF_UF…</a> | QF_UF | ✗ | sat | 0.20 | 0.01 | 0.06 | 0.01 | <span class="best">0.01</span> | 0.49 | 0.09 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UF/smt-lib/TypeSafe__z3.1184131.smt2" title="TypeSafe__z3.1184131">TypeSafe__<wbr>z3.1184131</a> | QF_UF | ✗ | unsat | 0.23 | 0.01 | 0.06 | 0.01 | <span class="best">0.01</span> | 0.50 | 0.11 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_LIA/smt-lib/CAV_2009_benchmarks__20-vars__problem__001.smt2" title="CAV_2009_benchmarks__20-vars__problem__001">CAV_2009_benchmarks__<wbr>20-…</a> | QF_LIA | ✗ | sat | 0.29 | 0.03 | 0.09 | <span class="best">0.02</span> | 0.03 | 0.58 | 0.13 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_LIA/smt-lib/dillig__20-4.smt2" title="dillig__20-4">dillig__<wbr>20-4</a> | QF_LIA | ✗ | sat | 0.33 | 0.07 | 0.11 | <span class="best">0.02</span> | 0.03 | 0.58 | 0.14 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_LIA/smt-lib/prime-cone__prime_cone_unsat_3.smt2" title="prime-cone__prime_cone_unsat_3">prime-cone__<wbr>prime_cone_u…</a> | QF_LIA | ✗ | unsat | 0.24 | <span class="best">0.01</span> | 0.07 | 0.02 | <span class="best">0.01</span> | 0.49 | 0.11 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_LIA/smt-lib/convert__convert-tiff2jpg-query-1831.smt2" title="convert__convert-tiff2jpg-query-1831">convert__<wbr>convert-tiff2jp…</a> | QF_LIA | ✗ | unsat | 0.28 | 0.02 | 0.08 | 0.02 | <span class="best">0.01</span> | 0.55 | 0.13 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UFLIA/smt-lib/wisas__xs_8_8.smt2" title="wisas__xs_8_8">wisas__<wbr>xs_8_8</a> | QF_UFLIA | ✗ | sat | 0.51 | 0.28 | 0.20 | 0.02 | <span class="best">0.01</span> | 0.87 | 0.17 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UFLIA/smt-lib/TwoSquares__smtlib.598294.smt2" title="TwoSquares__smtlib.598294">TwoSquares__<wbr>smtlib.598294</a> | QF_UFLIA | ✗ | sat | 0.25 | 0.01 | 0.06 | 0.01 | <span class="best">0.01</span> | 0.56 | 0.10 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UFLIA/smt-lib/mathsat__Wisa__xs-09-05-2-4-4-5.smt2" title="mathsat__Wisa__xs-09-05-2-4-4-5">mathsat__<wbr>Wisa__<wbr>xs-09-05-…</a> | QF_UFLIA | ✗ | sat | 0.53 | 0.32 | 0.22 | 0.02 | <span class="best">0.01</span> | 0.90 | 0.20 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_DT/smt-lib/20172804-Barrett__tests__v10__v10l20080.cvc.smt2" title="20172804-Barrett__tests__v10__v10l20080.cvc">20172804-Barrett__<wbr>tests_…</a> | QF_DT | ✗ | sat | 0.24 | 0.01 | 0.07 | 0.02 | <span class="best">0.01</span> | 0.78 | 0.10 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_DT/smt-lib/20230720-blocksworld__from_0_1_3_to_1_1_2_bmc_0.smt2" title="20230720-blocksworld__from_0_1_3_to_1_1_2_bmc_0">20230720-blocksworld__<wbr>fr…</a> | QF_DT | ✗ | unsat | 0.28 | 0.01 | 0.07 | 0.01 | <span class="best">0.01</span> | 0.65 | 0.12 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_BV/smt-lib/sage__app7__bench_8505.smt2" title="sage__app7__bench_8505">sage__<wbr>app7__<wbr>bench_8505</a> | QF_BV | ✗ | sat | 0.22 | <span class="best">0.01</span> | 0.06 | 0.01 | <span class="best">0.01</span> | 0.51 | — |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_BV/smt-lib/bmc-bv__adpcm.smt2" title="bmc-bv__adpcm">bmc-bv__<wbr>adpcm</a> | QF_BV | ✗ | sat | 0.20 | 0.01 | 0.06 | 0.01 | <span class="best">0.01</span> | 0.45 | — |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_BV/smt-lib/pspace__ndist.a.20000.smt2" title="pspace__ndist.a.20000">pspace__<wbr>ndist.a.20000</a> | QF_BV | ✗ | unsat | 0.25 | 0.02 | 0.07 | timeout | <span class="best">0.01</span> | 0.58 | — |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UF/smt-lib-incremental/20190906-CLEARSY__0020__00009.smt2" title="20190906-CLEARSY__0020__00009">20190906-CLEARSY__<wbr>0020__<wbr>…</a> | QF_UF | ✓ | 181 checks | 0.44 | 0.22 | 0.19 | <span class="best">0.02</span> | 0.03 | — | 0.24 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UFLIA/smt-lib-incremental/kind__MESI_i4.ec.smt2" title="kind__MESI_i4.ec">kind__<wbr>MESI_i4.ec</a> | QF_UFLIA | ✓ | 5 checks | 1.29 | 3.08 | 1.20 | <span class="best">0.03</span> | 0.09 | — | 0.27 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UFLIA/smt-lib-incremental/kind__car_6_e2_893.ec.smt2" title="kind__car_6_e2_893.ec">kind__<wbr>car_6_e2_893.ec</a> | QF_UFLIA | ✓ | 21 checks | 5.38 | timeout | 10.01 | <span class="best">0.10</span> | 0.88 | — | 0.60 |
| <a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_DT/smt-lib/20230314-Jaroslav-Bendik-Certora__65782_cd31513fdcd15701933b_6_QF_UFDTLIA.smt2" title="20230314-Jaroslav-Bendik-Certora__65782_cd31513fdcd15701933b_6_QF_UFDTLIA">20230314-Jaroslav-Bendik…</a> | QF_UFDTLIA | ✗ | unknown | 14.32 | timeout | 24.58 | <span class="best">0.04</span> | 0.10 | 2.05 | 0.50 |

The floors say most of it: about 200 ms for a JVM process (the JVM's own start-up), about 56 ms for Node with
the WebAssembly module, and about 7 ms for the native binary — the same order as z3 (14–25 ms) and cvc5
(6–36 ms), which are C++ binaries. On the instances that need real work the ranking changes: the JVM's
just-in-time compiler pays off (`NEQ004`: 0.63 s against 1.25 s native), while the native binary and Node
never warm up.

`pspace/ndist.a.20000` is the instance where the fragment matters more than the engine: 20 000-bit vectors,
which VIK lowers to integers and answers in 20 ms as a native binary, while z3 bit-blasts and does not
finish in 30 seconds.

The three added rows draw the solver's current boundary. On `CLEARSY` — 181 checks, each easy — every
solver is fast and VIK's script time is start-up plus 181 small solves. On `kind/MESI_i4` and `car_6` the
redone work compounds: 5.4 s for `car_6`'s 21 checks on the JVM, the native binary over the limit, where z3
replays them in 0.10 s. And the `Certora` instance — `QF_UFDTLIA`, its status unknown to the release —
takes VIK 14 s on the JVM where every other solver answers within about 2 s: datatypes, functions and
integers together are the combination the solver still pays most for (the solvers that finish agree on
`sat`).

## Limitations

- One machine, shared with other users; the numbers are medians and confidence intervals, not guarantees.
- Twenty instances, chosen small and inside VIK's fragment. On the competition's hard instances VIK is far
  behind z3 and cvc5, and on some of them it does not finish at all — the
  [corpus](https://github.com/mbovel/vik/tree/main/smtlib) records that: of its 699 files, 55 are marked as
  out of the solver's reach.
- The in-process z3 and cvc5 go through the JavaSMT bindings, which cross the JNI boundary per call; their
  binaries are what the cold table measures. Both are reported.
- VIK's answers are checked against the status of every file, on every run of the test suite; a wrong answer
  would show as such in these tables (none does).

## Reproducing

```sh
# warm, in process: JMH, two forks, three plus three iterations; the second run is the incremental
# instances on the solvers that replay scripts, the third is NEQ004 without princess
sbt "bench/Jmh/run -f 2 -wi 3 -i 3 -rf json -rff warm.json -p file=<instances> -e .*print.* .*OtherSolversBench.*"
sbt "bench/Jmh/run -f 2 -wi 3 -i 3 -rf json -rff warm-inc.json -p file=<incremental> .*OtherSolversBench.(vik|vikText|cvc5|smtinterpol)$"
python3 bench/warm2md.py warm.json warm-inc.json warm-neq.json \
  --blank z3Direct:sage__app7__bench_8505,z3Direct:bmc-bv__adpcm,smtinterpol:sage__app7__bench_8505,smtinterpol:bmc-bv__adpcm

# cold, one process per run: three repetitions, the median
bench/cold.sh bench/evaluation-instances.txt 3 30 > cold.tsv
python3 bench/cold2md.py cold.tsv \
  --blank smtinterpol:sage__app7__bench_8505,smtinterpol:bmc-bv__adpcm,smtinterpol:pspace__ndist.a.20000

# the native binary and the WebAssembly module the cold run uses
sbt "smt2Native/nativeLink; docsAssets"
```

The raw data of this page, committed in the repository:
[cold](https://github.com/mbovel/vik/blob/main/bench/results/smt/2026-08-31-laraserver4-cold.tsv),
[warm](https://github.com/mbovel/vik/blob/main/bench/results/smt/2026-08-31-laraserver4-warm.json),
[warm, incremental](https://github.com/mbovel/vik/blob/main/bench/results/smt/2026-08-31-laraserver4-warm-inc.json),
[warm, `NEQ004`](https://github.com/mbovel/vik/blob/main/bench/results/smt/2026-08-31-laraserver4-warm-neq.json).
