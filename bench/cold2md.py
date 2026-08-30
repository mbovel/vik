#!/usr/bin/env python3
"""A markdown table from the output of cold.sh: cold2md.py results.tsv [solver,...]

The cell is the median of the repetitions, in seconds; `timeout` when the solver did not finish, and the
answer in bold when it disagrees with the file's status.
"""
import collections, csv, sys

rows = list(csv.DictReader(open(sys.argv[1]), delimiter="\t"))
solvers = sys.argv[2].split(",") if len(sys.argv) > 2 else [
    "vik-jvm", "vik-native", "vik-node", "z3", "cvc5", "princess", "smtinterpol"
]
names = {"vik-jvm": "VIK (JVM)", "vik-native": "VIK (native)", "vik-node": "VIK (Node)", "z3": "z3",
         "cvc5": "cvc5", "princess": "princess", "smtinterpol": "SMTInterpol"}
by = collections.defaultdict(dict)
status = {}
for r in rows:
    by[r["file"]][r["solver"]] = (r["answer"], float(r["median"]))
    status[r["file"]] = r["expected"]

print("| Instance | Status | " + " | ".join(names[s] for s in solvers) + " |")
print("|---|---|" + "---:|" * len(solvers))
for f, answers in by.items():
    cells = []
    for s in solvers:
        answer, median = answers.get(s, ("missing", 0.0))
        if answer == "timeout":
            cells.append("timeout")
        elif answer not in ("sat", "unsat"):
            cells.append(f"**{answer}**")
        elif answer != status[f]:
            cells.append(f"**{answer}** {median:.3f}")
        else:
            cells.append(f"{median:.3f}")
    print(f"| `{f[:-5]}` | {status[f]} | " + " | ".join(cells) + " |")
