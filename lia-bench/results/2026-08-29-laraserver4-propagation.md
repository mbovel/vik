# Bound propagation experiments, 2026-08-29, laraserver4

`SolverBench` in the quick configuration (2 forks, 5 warmup and 5 measurement iterations of 1 s), ms/op with the 95%
confidence interval over the 10 measurements, ratios against the first column. Raw JMH JSON:
`2026-08-29-laraserver4-propagation-{propagation,incremental,visit1,steps-prop,steps-noprop}.json`; the first column is
the `Long` fast path run of the [previous experiments](2026-08-29-laraserver4-experiments.md).

- **every row, 8 rounds**: before the simplex, every row derives bounds (Dutertre and de Moura's bound propagation),
  repeated while bounds tighten, at most 8 rounds.
- **incremental, 4 visits**: only the rows mentioning a variable whose bound tightened since the last propagation,
  through a worklist, each row at most 4 times per propagation.
- **incremental, 1 visit**: the same, each row at most once per propagation. Kept.

| Benchmark | n | no propagation | every row, 8 rounds | incremental, 4 visits | incremental, 1 visit |
|---|---:|---:|---:|---:|---:|
| `backtrack` | 10 | 1.98 ± 0.15 | 113.43 ± 2.87 (0.02×) | 24.71 ± 0.59 (0.08×) | 9.70 ± 0.15 (0.20×) |
| `backtrack` | 20 | 78.31 ± 2.06 | 1509.83 ± 32.30 (0.05×) | 265.09 ± 6.12 (0.30×) | 105.08 ± 2.83 (0.75×) |
| `incremental` | 10 | 2.35 ± 0.08 | 102.19 ± 3.34 (0.02×) | 25.81 ± 0.60 (0.09×) | 10.33 ± 0.25 (0.23×) |
| `incremental` | 20 | 43.15 ± 2.56 | 1675.04 ± 30.22 (0.03×) | 394.06 ± 33.35 (0.11×) | 171.59 ± 10.51 (0.25×) |
| `solve` | 10 | 0.93 ± 0.02 | 4.70 ± 0.11 (0.20×) | 2.89 ± 0.08 (0.32×) | 1.61 ± 0.05 (0.58×) |
| `solve` | 20 | 9.54 ± 0.23 | 2.69 ± 0.06 (3.55×) | 7.17 ± 0.18 (1.33×) | 4.06 ± 0.10 (2.35×) |

The `stepChains` workload (chains `x_{i+1} = x_i + a_i × y_i` with a narrow window on the last sum):

| Benchmark | n | no propagation | incremental, 1 visit |
|---|---:|---:|---:|
| `stepChains` | 10 | 0.19 ± 0.01 | 0.80 ± 0.02 (0.24×) |
| `stepChains` | 20 | 1.01 ± 0.04 | 4.34 ± 0.09 (0.23×) |

On the random workloads propagation only costs: every row of a small dense system in a tight box derives something,
so each check cascades through most rows, and the simplex with branch and bound needs no help there. On the SMT-LIB
QF_LIA instances (one process each, 60 s limit, see
[smt-bench/results/2026-08-29-laraserver4-compare-lia.md](../../smt-bench/results/2026-08-29-laraserver4-compare-lia.md))
it is decisive: `fft/Sz64_1160` 17 s → 0.7 s, `nec-smt/prp-2-17` timeout → 1.4 s, `cut_lemmas/20-vars` 18 s → 5 s (where
z3 and cvc5 time out), `rings` 0.75 s → 0.5 s. Kept for that reason; the cost per check is the next thing to reduce
(reasons are recomputed as set unions at every row visit).
