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

<div class="table-scroll">
<table>
<thead><tr><th>Instance</th><th>Theory</th><th>Incremental</th><th>Status</th><th>VIK (API)</th><th>VIK (SMT2)</th><th>z3 (API)</th><th>z3 (SMT2)</th><th>cvc5 (SMT2)</th><th>princess (API)</th><th>SMTInterpol (API)</th></tr></thead>
<tbody>
<tr><td><a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UF/smt-lib/QG-classification__qg5__iso_brn001.smt2" title="QG-classification__qg5__iso_brn001">QG-classification__<wbr>…</a></td><td>QF_UF</td><td>✗</td><td>sat</td><td align="right"><span class="best">0.74</span></td><td align="right">1.42</td><td align="right">2.77</td><td align="right">2.79</td><td align="right">8.28</td><td align="right">24.91</td><td align="right">1.09</td></tr>
<tr><td><a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UF/smt-lib/2018-Goel-hwbench__QF_UF_bug-1_ab_reg_max.smt2" title="2018-Goel-hwbench__QF_UF_bug-1_ab_reg_max">2018-Goel-hwbench__<wbr>…</a></td><td>QF_UF</td><td>✗</td><td>sat</td><td align="right"><span class="best">0.01</span></td><td align="right">0.03</td><td align="right">0.78</td><td align="right">0.91</td><td align="right">1.75</td><td align="right">0.68</td><td align="right">0.02</td></tr>
<tr><td><a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UF/smt-lib/TypeSafe__z3.1184131.smt2" title="TypeSafe__z3.1184131">TypeSafe__<wbr>z3.1184131</a></td><td>QF_UF</td><td>✗</td><td>unsat</td><td align="right"><span class="best">0.01</span></td><td align="right">0.03</td><td align="right">0.71</td><td align="right">0.84</td><td align="right">1.73</td><td align="right">0.91</td><td align="right">0.02</td></tr>
<tr><td><a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_LIA/smt-lib/CAV_2009_benchmarks__20-vars__problem__001.smt2" title="CAV_2009_benchmarks__20-vars__problem__001">CAV_2009_benchmarks…</a></td><td>QF_LIA</td><td>✗</td><td>sat</td><td align="right">2.29</td><td align="right">2.63</td><td align="right">2.42</td><td align="right">2.60</td><td align="right">27.93</td><td align="right">6.14</td><td align="right"><span class="best">0.18</span></td></tr>
<tr><td><a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_LIA/smt-lib/dillig__20-4.smt2" title="dillig__20-4">dillig__<wbr>20-4</a></td><td>QF_LIA</td><td>✗</td><td>sat</td><td align="right">9.73</td><td align="right">10.82</td><td align="right">2.58</td><td align="right">2.62</td><td align="right">31.83</td><td align="right">7.15</td><td align="right"><span class="best">0.40</span></td></tr>
<tr><td><a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_LIA/smt-lib/prime-cone__prime_cone_unsat_3.smt2" title="prime-cone__prime_cone_unsat_3">prime-cone__<wbr>prime_c…</a></td><td>QF_LIA</td><td>✗</td><td>unsat</td><td align="right"><span class="best">0.05</span></td><td align="right">0.08</td><td align="right">2.01</td><td align="right">2.10</td><td align="right">3.00</td><td align="right">—</td><td align="right">—</td></tr>
<tr><td><a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_LIA/smt-lib/convert__convert-tiff2jpg-query-1831.smt2" title="convert__convert-tiff2jpg-query-1831">convert__<wbr>convert-ti…</a></td><td>QF_LIA</td><td>✗</td><td>unsat</td><td align="right"><span class="best">0.42</span></td><td align="right">0.71</td><td align="right">1.85</td><td align="right">1.76</td><td align="right">4.43</td><td align="right">—</td><td align="right">—</td></tr>
<tr><td><a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UFLIA/smt-lib/wisas__xs_8_8.smt2" title="wisas__xs_8_8">wisas__<wbr>xs_8_8</a></td><td>QF_UFLIA</td><td>✗</td><td>sat</td><td align="right">27.72</td><td align="right">28.50</td><td align="right">5.53</td><td align="right"><span class="best">3.19</span></td><td align="right">9.45</td><td align="right">34.15</td><td align="right">6.66</td></tr>
<tr><td><a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UFLIA/smt-lib/TwoSquares__smtlib.598294.smt2" title="TwoSquares__smtlib.598294">TwoSquares__<wbr>smtlib.…</a></td><td>QF_UFLIA</td><td>✗</td><td>sat</td><td align="right"><span class="best"><0.01</span></td><td align="right">0.02</td><td align="right">0.94</td><td align="right">1.06</td><td align="right">1.28</td><td align="right">—</td><td align="right">—</td></tr>
<tr><td><a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UFLIA/smt-lib/mathsat__Wisa__xs-09-05-2-4-4-5.smt2" title="mathsat__Wisa__xs-09-05-2-4-4-5">mathsat__<wbr>Wisa__<wbr>xs-0…</a></td><td>QF_UFLIA</td><td>✗</td><td>sat</td><td align="right">31.67</td><td align="right">35.77</td><td align="right">6.24</td><td align="right"><span class="best">3.37</span></td><td align="right">10.96</td><td align="right">—</td><td align="right">—</td></tr>
<tr><td><a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_DT/smt-lib/20172804-Barrett__tests__v10__v10l20080.cvc.smt2" title="20172804-Barrett__tests__v10__v10l20080.cvc">20172804-Barrett__<wbr>t…</a></td><td>QF_DT</td><td>✗</td><td>sat</td><td align="right"><span class="best">0.02</span></td><td align="right">0.06</td><td align="right">1.00</td><td align="right">1.10</td><td align="right">2.20</td><td align="right">—</td><td align="right">—</td></tr>
<tr><td><a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_DT/smt-lib/20230720-blocksworld__from_0_1_3_to_1_1_2_bmc_0.smt2" title="20230720-blocksworld__from_0_1_3_to_1_1_2_bmc_0">20230720-blocksworl…</a></td><td>QF_DT</td><td>✗</td><td>unsat</td><td align="right">0.21</td><td align="right">0.29</td><td align="right">1.04</td><td align="right">1.14</td><td align="right">2.23</td><td align="right">8.57</td><td align="right"><span class="best">0.12</span></td></tr>
<tr><td><a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_BV/smt-lib/sage__app7__bench_8505.smt2" title="sage__app7__bench_8505">sage__<wbr>app7__<wbr>bench_8…</a></td><td>QF_BV</td><td>✗</td><td>sat</td><td align="right"><span class="best">0.01</span></td><td align="right">0.01</td><td align="right">—</td><td align="right">0.89</td><td align="right">0.92</td><td align="right">—</td><td align="right">—</td></tr>
<tr><td><a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_BV/smt-lib/bmc-bv__adpcm.smt2" title="bmc-bv__adpcm">bmc-bv__<wbr>adpcm</a></td><td>QF_BV</td><td>✗</td><td>sat</td><td align="right"><span class="best"><0.01</span></td><td align="right">0.01</td><td align="right">—</td><td align="right">0.75</td><td align="right">0.87</td><td align="right">—</td><td align="right">—</td></tr>
<tr><td><a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UF/smt-lib/QG-classification__qg5__iso_icl662.smt2" title="QG-classification__qg5__iso_icl662">QG-classification__<wbr>…</a></td><td>QF_UF</td><td>✗</td><td>unsat</td><td align="right">—</td><td align="right">—</td><td align="right">—</td><td align="right">—</td><td align="right">—</td><td align="right">153.11</td><td align="right"><span class="best">6.21</span></td></tr>
<tr><td><a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UF/smt-lib/20170829-Rodin__smt3534838651727683253.smt2" title="20170829-Rodin__smt3534838651727683253">20170829-Rodin__<wbr>smt…</a></td><td>QF_UF</td><td>✗</td><td>unsat</td><td align="right">—</td><td align="right">—</td><td align="right">—</td><td align="right">—</td><td align="right">—</td><td align="right">0.36</td><td align="right"><span class="best">0.02</span></td></tr>
<tr><td><a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UF/smt-lib/20190906-CLEARSY__0016__00003.smt2" title="20190906-CLEARSY__0016__00003">20190906-CLEARSY__<wbr>0…</a></td><td>QF_UF</td><td>✗</td><td>sat</td><td align="right">—</td><td align="right">—</td><td align="right">—</td><td align="right">—</td><td align="right">—</td><td align="right">17.28</td><td align="right"><span class="best">0.03</span></td></tr>
<tr><td><a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_LIA/smt-lib/mathsat__FISCHER1-1-fair.smt2" title="mathsat__FISCHER1-1-fair">mathsat__<wbr>FISCHER1-1…</a></td><td>QF_LIA</td><td>✗</td><td>sat</td><td align="right">—</td><td align="right">—</td><td align="right">—</td><td align="right">—</td><td align="right">—</td><td align="right">7.62</td><td align="right"><span class="best">0.24</span></td></tr>
<tr><td><a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_DT/smt-lib/20172804-Barrett__tests__v1__v1l20086.cvc.smt2" title="20172804-Barrett__tests__v1__v1l20086.cvc">20172804-Barrett__<wbr>t…</a></td><td>QF_DT</td><td>✗</td><td>sat</td><td align="right">—</td><td align="right">—</td><td align="right">—</td><td align="right">—</td><td align="right">—</td><td align="right">3.56</td><td align="right"><span class="best">0.02</span></td></tr>
<tr><td><a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UF/smt-lib-incremental/20190906-CLEARSY__0020__00009.smt2" title="20190906-CLEARSY__0020__00009">20190906-CLEARSY__<wbr>0…</a></td><td>QF_UF</td><td>✓</td><td>181 checks</td><td align="right">9.36</td><td align="right">10.65</td><td align="right"><span class="best">7.12</span></td><td align="right">—</td><td align="right">64.90</td><td align="right">754.29</td><td align="right">13.15</td></tr>
<tr><td><a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UFLIA/smt-lib-incremental/kind__MESI_i4.ec.smt2" title="kind__MESI_i4.ec">kind__<wbr>MESI_i4.ec</a></td><td>QF_UFLIA</td><td>✓</td><td>5 checks</td><td align="right">752.02</td><td align="right">737.89</td><td align="right"><span class="best">23.88</span></td><td align="right">—</td><td align="right">102.69</td><td align="right">96490.10</td><td align="right">69.28</td></tr>
<tr><td><a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UF/smt-lib/NEQ__NEQ004_size4.smt2" title="NEQ__NEQ004_size4">NEQ__<wbr>NEQ004_size4</a></td><td>QF_UF</td><td>✗</td><td>unsat</td><td align="right">113.62</td><td align="right">110.13</td><td align="right">11.48</td><td align="right"><span class="best">11.12</span></td><td align="right">109.53</td><td align="right">—</td><td align="right">124.49</td></tr>
</tbody>
</table>
</div>

The 95% confidence intervals (Student's t over the samples of a cell) average 6% of the mean, at most 16% (VIK (API) on `bmc-bv__adpcm`).











The *(SMT2)* columns — z3 and cvc5 — receive the text of the script and parse it themselves; the *(API)*
columns receive the problem already built: *VIK (API)* solves its own tree, and *z3 (API)*, *princess (API)*
and *SMTInterpol (API)* are handed that tree through each solver's API — declarations, terms, asserts,
`push`, `pop` and every `check-sat` — which is what an embedding verifier pays. The difference between the
two z3 columns is what parsing costs; measured for the other two before their text columns were dropped,
parsing cost princess 1.3–2.4× and SMTInterpol 4–6× (exceptions: the arithmetic-heavy `wisas` and
`MESI_i4`, which SMTInterpol answers 1.6× and 3.7× faster from text — its parser feeds its internal affine
form more directly than `term` calls do). For a
bit-vector instance the tree is already lowered to integers, so the *(API)* columns are not the same
question there and those cells are left out. The incremental instances leave one more cell out: z3's text
column flattens the script into one problem — its bindings execute no script — a different question on a
script of many `check-sat`s. Princess is measured through its API for the same reason in the other
direction: its own script executor rejects these scripts (a re-declaration after `pop`, see
[external-issues.md](https://github.com/mbovel/vik/blob/main/external-issues.md)).

Three instances are in the cold table only: `pspace/ndist.a.20000`, whose sort the in-process cvc5 bindings
reject, and `car_6` and the `Certora` one, which ask seconds per operation of VIK. `NEQ004_size4` is in the table without
princess, which needs minutes per operation on it. SMTInterpol's binary keeps its `QF_BV` cells out of the
cold table — it has no bit-vector theory: it rejects the declarations and then answers about what remains
(see [external-issues.md](https://github.com/mbovel/vik/blob/main/external-issues.md)) — and in the warm
table its `QF_BV` cells are out with the other *(API)* columns, by the lowering rule above.

Read the two VIK columns together: *VIK (API)* is the solver on a problem it already holds, which is how a
tool that builds the terms itself calls it, and *VIK (SMT2)* adds the parsing of the script, which is what
the other *(SMT2)* columns include. The gap between them is the front end — tens of microseconds on
the small instances.

On the queries a verifier produces — the small ones, a millisecond or less — VIK is the fastest of the six,
by one to two orders of magnitude: 5 µs where z3 takes 780 µs and cvc5 1.8 ms on `bug-1_ab_reg_max`, 1 µs
against 750 µs and 870 µs on `bmc-bv/adpcm`. That is not a statement about the solving algorithms but about
what a call costs: z3 and cvc5 pay a context and a JNI crossing per query, and their engines are built for
much larger problems than these.

Where the instance has real work in it, the ranking turns around, and by more than the factor of five the
project aims at: `dillig/20-4` is 10 ms for VIK against 2.6 ms for z3, `wisas/xs_8_8` 28 ms against 3.2 ms,
`NEQ004_size4` 114 ms against 11 ms. The two `wisas` instances are the honest worst case here — an arrangement
of shared integer terms that grows with the square of their number — and they are in the table for that
reason; they are also where the arithmetic's [propagation of the bounds it already
knows](https://github.com/mbovel/vik/blob/main/bench/results/smt/2026-08-31-laraserver4-bound-propagation.md)
earns its keep (without it they measured half again slower).

The middle ground is where the design shows: `CAV_2009/20-vars` is a tie with z3 (2.4 ms for both),
`convert-tiff2jpg` is four times and `prime_cone_unsat_3` forty times faster than z3, and the datatype
instances are one to two orders of magnitude faster than princess, which is the other solver written for the
JVM.

The incremental scripts split. On `CLEARSY__0020` — 181 easy `check-sat`s — the z3 replay and VIK lead,
7 ms and 9 ms for the whole script against 13 ms for SMTInterpol, 65 ms for cvc5 and 754 ms for princess:
each check is one more small query, and the solver now lives across all of them. On `kind/MESI_i4` the five
checks are real work, and holding the state of the earlier ones costs more than it saves: 752 ms where z3's
replay needs 24 ms and SMTInterpol 69 ms (princess pays 96 s). That price is
[measured and accepted](https://github.com/mbovel/vik/blob/main/bench/results/smt/2026-08-31-laraserver4-incremental.md):
the same persistence buys `car_6`'s 21 checks a 1.45× win in the cold table below.

## Cold: a process per question

The measurement is the one a competition makes: a new process for every instance, the wall-clock time of the
whole answer, three repetitions, the median reported (a scheduling hiccup then decides nothing), with a
limit of 30 seconds. This is where start-up appears, and it is what a tool pays when it shells out to a
solver.

**Times in seconds — lower is better;** the best correct answer of a row is green.

<div class="table-scroll">
<table>
<thead><tr><th>Instance</th><th>Theory</th><th>Incremental</th><th>Status</th><th>VIK (JVM)</th><th>VIK (native)</th><th>VIK (Node)</th><th>z3</th><th>cvc5</th><th>princess</th><th>SMTInterpol</th></tr></thead>
<tbody>
<tr><td><a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UF/smt-lib/NEQ__NEQ004_size4.smt2" title="NEQ__NEQ004_size4">NEQ__<wbr>NEQ004_size4</a></td><td>QF_UF</td><td>✗</td><td>unsat</td><td align="right">0.70</td><td align="right">1.26</td><td align="right">0.46</td><td align="right"><span class="best">0.02</span></td><td align="right">0.04</td><td align="right">timeout</td><td align="right">0.36</td></tr>
<tr><td><a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UF/smt-lib/QG-classification__qg5__iso_brn001.smt2" title="QG-classification__qg5__iso_brn001">QG-classification__<wbr>…</a></td><td>QF_UF</td><td>✗</td><td>sat</td><td align="right">0.33</td><td align="right">0.05</td><td align="right">0.09</td><td align="right">0.02</td><td align="right"><span class="best">0.01</span></td><td align="right">0.90</td><td align="right">0.14</td></tr>
<tr><td><a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UF/smt-lib/2018-Goel-hwbench__QF_UF_bug-1_ab_reg_max.smt2" title="2018-Goel-hwbench__QF_UF_bug-1_ab_reg_max">2018-Goel-hwbench__<wbr>…</a></td><td>QF_UF</td><td>✗</td><td>sat</td><td align="right">0.21</td><td align="right">0.01</td><td align="right">0.06</td><td align="right">0.01</td><td align="right"><span class="best">0.01</span></td><td align="right">0.50</td><td align="right">0.10</td></tr>
<tr><td><a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UF/smt-lib/TypeSafe__z3.1184131.smt2" title="TypeSafe__z3.1184131">TypeSafe__<wbr>z3.1184131</a></td><td>QF_UF</td><td>✗</td><td>unsat</td><td align="right">0.22</td><td align="right">0.01</td><td align="right">0.06</td><td align="right">0.01</td><td align="right"><span class="best">0.01</span></td><td align="right">0.50</td><td align="right">0.10</td></tr>
<tr><td><a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_LIA/smt-lib/CAV_2009_benchmarks__20-vars__problem__001.smt2" title="CAV_2009_benchmarks__20-vars__problem__001">CAV_2009_benchmarks…</a></td><td>QF_LIA</td><td>✗</td><td>sat</td><td align="right">0.29</td><td align="right">0.03</td><td align="right">0.10</td><td align="right"><span class="best">0.02</span></td><td align="right">0.03</td><td align="right">0.57</td><td align="right">0.13</td></tr>
<tr><td><a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_LIA/smt-lib/dillig__20-4.smt2" title="dillig__20-4">dillig__<wbr>20-4</a></td><td>QF_LIA</td><td>✗</td><td>sat</td><td align="right">0.33</td><td align="right">0.07</td><td align="right">0.10</td><td align="right"><span class="best">0.02</span></td><td align="right">0.03</td><td align="right">0.59</td><td align="right">0.14</td></tr>
<tr><td><a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_LIA/smt-lib/prime-cone__prime_cone_unsat_3.smt2" title="prime-cone__prime_cone_unsat_3">prime-cone__<wbr>prime_c…</a></td><td>QF_LIA</td><td>✗</td><td>unsat</td><td align="right">0.26</td><td align="right"><span class="best">0.01</span></td><td align="right">0.07</td><td align="right">0.02</td><td align="right">0.01</td><td align="right">0.48</td><td align="right">0.10</td></tr>
<tr><td><a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_LIA/smt-lib/convert__convert-tiff2jpg-query-1831.smt2" title="convert__convert-tiff2jpg-query-1831">convert__<wbr>convert-ti…</a></td><td>QF_LIA</td><td>✗</td><td>unsat</td><td align="right">0.29</td><td align="right">0.02</td><td align="right">0.08</td><td align="right">0.02</td><td align="right"><span class="best">0.01</span></td><td align="right">0.54</td><td align="right">0.14</td></tr>
<tr><td><a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UFLIA/smt-lib/wisas__xs_8_8.smt2" title="wisas__xs_8_8">wisas__<wbr>xs_8_8</a></td><td>QF_UFLIA</td><td>✗</td><td>sat</td><td align="right">0.52</td><td align="right">0.28</td><td align="right">0.20</td><td align="right">0.02</td><td align="right"><span class="best">0.01</span></td><td align="right">0.86</td><td align="right">0.18</td></tr>
<tr><td><a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UFLIA/smt-lib/TwoSquares__smtlib.598294.smt2" title="TwoSquares__smtlib.598294">TwoSquares__<wbr>smtlib.…</a></td><td>QF_UFLIA</td><td>✗</td><td>sat</td><td align="right">0.25</td><td align="right">0.01</td><td align="right">0.07</td><td align="right">0.01</td><td align="right"><span class="best">0.01</span></td><td align="right">0.56</td><td align="right">0.10</td></tr>
<tr><td><a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UFLIA/smt-lib/mathsat__Wisa__xs-09-05-2-4-4-5.smt2" title="mathsat__Wisa__xs-09-05-2-4-4-5">mathsat__<wbr>Wisa__<wbr>xs-0…</a></td><td>QF_UFLIA</td><td>✗</td><td>sat</td><td align="right">0.55</td><td align="right">0.31</td><td align="right">0.22</td><td align="right">0.02</td><td align="right"><span class="best">0.01</span></td><td align="right">0.88</td><td align="right">0.20</td></tr>
<tr><td><a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_DT/smt-lib/20172804-Barrett__tests__v10__v10l20080.cvc.smt2" title="20172804-Barrett__tests__v10__v10l20080.cvc">20172804-Barrett__<wbr>t…</a></td><td>QF_DT</td><td>✗</td><td>sat</td><td align="right">0.25</td><td align="right">0.01</td><td align="right">0.06</td><td align="right">0.01</td><td align="right"><span class="best">0.01</span></td><td align="right">0.80</td><td align="right">0.10</td></tr>
<tr><td><a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_DT/smt-lib/20230720-blocksworld__from_0_1_3_to_1_1_2_bmc_0.smt2" title="20230720-blocksworld__from_0_1_3_to_1_1_2_bmc_0">20230720-blocksworl…</a></td><td>QF_DT</td><td>✗</td><td>unsat</td><td align="right">0.28</td><td align="right">0.01</td><td align="right">0.07</td><td align="right">0.01</td><td align="right"><span class="best">0.01</span></td><td align="right">0.66</td><td align="right">0.12</td></tr>
<tr><td><a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_BV/smt-lib/sage__app7__bench_8505.smt2" title="sage__app7__bench_8505">sage__<wbr>app7__<wbr>bench_8…</a></td><td>QF_BV</td><td>✗</td><td>sat</td><td align="right">0.22</td><td align="right">0.01</td><td align="right">0.07</td><td align="right">0.01</td><td align="right"><span class="best">0.01</span></td><td align="right">0.52</td><td align="right">—</td></tr>
<tr><td><a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_BV/smt-lib/bmc-bv__adpcm.smt2" title="bmc-bv__adpcm">bmc-bv__<wbr>adpcm</a></td><td>QF_BV</td><td>✗</td><td>sat</td><td align="right">0.21</td><td align="right"><span class="best">0.01</span></td><td align="right">0.06</td><td align="right">0.01</td><td align="right"><span class="best">0.01</span></td><td align="right">0.45</td><td align="right">—</td></tr>
<tr><td><a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_BV/smt-lib/pspace__ndist.a.20000.smt2" title="pspace__ndist.a.20000">pspace__<wbr>ndist.a.200…</a></td><td>QF_BV</td><td>✗</td><td>unsat</td><td align="right">0.25</td><td align="right">0.02</td><td align="right">0.07</td><td align="right">timeout</td><td align="right"><span class="best">0.01</span></td><td align="right">0.58</td><td align="right">—</td></tr>
<tr><td><a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UF/smt-lib-incremental/20190906-CLEARSY__0020__00009.smt2" title="20190906-CLEARSY__0020__00009">20190906-CLEARSY__<wbr>0…</a></td><td>QF_UF</td><td>✓</td><td>181 checks</td><td align="right">0.43</td><td align="right">0.22</td><td align="right">0.19</td><td align="right"><span class="best">0.02</span></td><td align="right">0.03</td><td align="right">—</td><td align="right">0.24</td></tr>
<tr><td><a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UFLIA/smt-lib-incremental/kind__MESI_i4.ec.smt2" title="kind__MESI_i4.ec">kind__<wbr>MESI_i4.ec</a></td><td>QF_UFLIA</td><td>✓</td><td>5 checks</td><td align="right">1.64</td><td align="right">3.08</td><td align="right">1.21</td><td align="right"><span class="best">0.03</span></td><td align="right">0.09</td><td align="right">—</td><td align="right">0.26</td></tr>
<tr><td><a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_UFLIA/smt-lib-incremental/kind__car_6_e2_893.ec.smt2" title="kind__car_6_e2_893.ec">kind__<wbr>car_6_e2_893.…</a></td><td>QF_UFLIA</td><td>✓</td><td>21 checks</td><td align="right">3.62</td><td align="right">timeout</td><td align="right">10.04</td><td align="right"><span class="best">0.10</span></td><td align="right">0.88</td><td align="right">—</td><td align="right">0.61</td></tr>
<tr><td><a href="https://github.com/mbovel/vik/blob/main/smtlib/QF_DT/smt-lib/20230314-Jaroslav-Bendik-Certora__65782_cd31513fdcd15701933b_6_QF_UFDTLIA.smt2" title="20230314-Jaroslav-Bendik-Certora__65782_cd31513fdcd15701933b_6_QF_UFDTLIA">20230314-Jaroslav-B…</a></td><td>QF_UFDTLIA</td><td>✗</td><td>unknown</td><td align="right">14.68</td><td align="right">timeout</td><td align="right">24.43</td><td align="right"><span class="best">0.04</span></td><td align="right">0.10</td><td align="right">2.02</td><td align="right">0.49</td></tr>
</tbody>
</table>
</div>

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
learned clauses carry across the checks: 3.6 s for `car_6`'s 21 checks on the JVM — 5.4 s before the solver
persisted — the native binary over the limit, where z3 replays them in 0.10 s. And the `Certora` instance — `QF_UFDTLIA`, its status unknown to the release —
takes VIK 15 s on the JVM where every other solver answers within about 2 s: datatypes, functions and
integers together are the combination the solver still pays most for (the solvers that finish agree on
`sat`).

## Limitations

- One machine, shared with other users; the numbers are medians and confidence intervals, not guarantees.
- Twenty instances, chosen small and inside VIK's fragment. On the competition's hard instances VIK is far
  behind z3 and cvc5, and on some of them it does not finish at all — the
  [corpus](https://github.com/mbovel/vik/tree/main/smtlib) records that: of its 740 files, 55 are marked as
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
