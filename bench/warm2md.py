#!/usr/bin/env python3
"""A markdown table from a JMH run of OtherSolversBench: warm2md.py results.json [more.json ...] [--blank solver:instance,...]

One row per instance, one column per solver, the mean of the samples in milliseconds with its 95% confidence
interval (Student's t over the iterations of every fork). `--blank` leaves out the cells of a solver on an
instance where the measurement would not mean what the column says (see the evaluation page).
"""
import collections, json, statistics, sys
from scipy.stats import t

order = ["vik", "vikText", "z3Direct", "z3", "cvc5", "princess", "smtinterpol"]
names = {"vik": "VIK", "vikText": "VIK, parsing", "z3Direct": "z3, API", "z3": "z3, parsing", "cvc5": "cvc5",
         "princess": "princess", "smtinterpol": "SMTInterpol"}
arguments = sys.argv[1:]
blank = set()
if "--blank" in arguments:
    i = arguments.index("--blank")
    blank = {tuple(pair.split(":")) for pair in arguments[i + 1].split(",")}
    arguments = arguments[:i] + arguments[i + 2:]

by = collections.defaultdict(dict)
for path in arguments:
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
        if (s, instance) in blank:
            cells.append("—")
        elif s in solvers:
            mean, ci = solvers[s]
            cells.append(f"{mean:.3f} ± {ci:.3f}" if mean < 1000 else f"{mean:.0f} ± {ci:.0f}")
        else:
            cells.append("—")
    print(f"| `{instance}` | " + " | ".join(cells) + " |")
