# LIA solver experiments, 2026-08-29, laraserver4

`SolverBench` in the quick configuration (2 forks, 5 warmup and 5 measurement iterations of 1 s), ms/op with the
95% confidence interval over the 10 measurements; each ratio is against the previous column. Raw JMH JSON:
`2026-08-29-laraserver4-experiments-{bigint,fast,branching}.json`.

- **BigInt**: rationals as normalized pairs of `BigInt`.
- **Long fast path**: rationals whose numerator and denominator fit in a `Long` use `Long` arithmetic with overflow
  detection (`Math.multiplyExact`), falling back to `BigInt`. Kept: 1.4–1.6× everywhere.
- **most fractional**: branch and bound splits the unknown whose value is farthest from an integer instead of the
  first fractional one (measured on top of the fast path). Mixed (1.6× on `backtrack`/20, 0.7× on
  `incremental`/10, the rest within noise): not kept.

| Benchmark | n | BigInt | Long fast path | most fractional |
|---|---:|---:|---:|---:|
| `backtrack` | 10 | 2.74 ± 0.08 | 1.98 ± 0.15 (1.38×) | 2.31 ± 0.04 (0.86×) |
| `backtrack` | 20 | 123.46 ± 3.48 | 78.31 ± 2.06 (1.58×) | 49.36 ± 1.24 (1.59×) |
| `incremental` | 10 | 3.62 ± 0.23 | 2.35 ± 0.08 (1.54×) | 3.26 ± 0.08 (0.72×) |
| `incremental` | 20 | 61.12 ± 1.14 | 43.15 ± 2.56 (1.42×) | 48.18 ± 4.05 (0.90×) |
| `solve` | 10 | 1.36 ± 0.05 | 0.93 ± 0.02 (1.46×) | 0.87 ± 0.01 (1.06×) |
| `solve` | 20 | 14.42 ± 0.30 | 9.54 ± 0.23 (1.51×) | 10.37 ± 1.36 (0.92×) |

AMD EPYC 9254 (shared machine), OpenJDK 17.0.20.
