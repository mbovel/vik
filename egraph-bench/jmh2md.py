#!/usr/bin/env python3
# /// script
# dependencies = ["scipy"]
# ///
"""Turn a JMH JSON result file (`-rf json`) into a Markdown table with 95% confidence intervals.

JMH's own "Error" column is a 99.9% interval; this recomputes a 95% one from the raw per-iteration
samples of all forks, using Student's t distribution (`scipy.stats.t`).

Usage: python3 egraph-bench/jmh2md.py target/jmh-result.json
"""
import json
import statistics
import sys

from scipy.stats import t


def main(path):
    rows = []
    for r in json.load(open(path)):
        name = r["benchmark"].rsplit(".", 1)[-1]
        params = ", ".join(f"{k}={v}" for k, v in sorted(r.get("params", {}).items()))
        m = r["primaryMetric"]
        samples = [x for fork in m["rawData"] for x in fork]
        n = len(samples)
        mean = statistics.fmean(samples)
        ci = t.ppf(0.975, n - 1) * statistics.stdev(samples) / n**0.5 if n > 1 else 0.0
        rows.append((name, params, mean, ci, min(samples), max(samples), len(m["rawData"]), n, m["scoreUnit"]))

    unit = rows[0][8] if rows else ""
    print(f"| Benchmark | Params | Mean ({unit}) | 95% CI | CI / mean | Min | Max | Forks × iterations |")
    print("|---|---|---:|---:|---:|---:|---:|---:|")
    for name, params, mean, ci, lo, hi, forks, n, _ in rows:
        print(f"| {name} | {params} | {mean:.3f} | ± {ci:.3f} | {100 * ci / mean:.1f}% | {lo:.3f} | {hi:.3f} | "
              f"{forks} × {n // forks} |")


if __name__ == "__main__":
    main(sys.argv[1] if len(sys.argv) > 1 else "target/jmh-result.json")
