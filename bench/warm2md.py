#!/usr/bin/env python3
"""A markdown table from a JMH run of OtherSolversBench: warm2md.py results.json [more.json ...]

One row per instance, one column per solver, the mean of the samples in milliseconds with its 95% confidence
interval (Student's t over the iterations of every fork).
"""
import collections, json, statistics, sys
from scipy.stats import t

order = ["vik", "z3Direct", "z3", "cvc5", "princess", "smtinterpol"]
names = {"vik": "VIK", "z3Direct": "z3 (built)", "z3": "z3 (parsed)", "cvc5": "cvc5", "princess": "princess",
         "smtinterpol": "SMTInterpol"}
by = collections.defaultdict(dict)
for path in sys.argv[1:]:
    for r in json.load(open(path)):
        solver = r["benchmark"].split(".")[-1]
        instance = r["params"]["file"].removesuffix(".smt2")
        xs = [x for iteration in r["primaryMetric"]["rawData"] for x in iteration]
        mean = statistics.mean(xs)
        ci = t.ppf(0.975, len(xs) - 1) * statistics.stdev(xs) / len(xs) ** 0.5 if len(xs) > 1 else 0.0
        by[instance][solver] = (mean, ci)

print("| Instance | " + " | ".join(names[s] for s in order) + " |")
print("|---|" + "---:|" * len(order))
for instance, solvers in by.items():
    cells = []
    for s in order:
        if s in solvers:
            mean, ci = solvers[s]
            cells.append(f"{mean:.3f} ± {ci:.3f}" if mean < 1000 else f"{mean:.0f} ± {ci:.0f}")
        else:
            cells.append("—")
    print(f"| `{instance}` | " + " | ".join(cells) + " |")
