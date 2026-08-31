#!/usr/bin/env python3
"""A markdown table from the output of cold.sh: cold2md.py results.tsv [solver,...]

One row per instance — linked to its corpus file, with its logic, whether the script is incremental and its
status — and one column per solver: the median of the repetitions in seconds, the best of the row highlighted
(`<span class="best">`, styled by the site's `tables.css`); `timeout` when the solver did not finish, and the
answer in bold when it disagrees with the file's status. Run from the repository root, where `smtlib/` is.
"""
import collections, csv, pathlib, re, sys


def corpus(base):
    """The corpus file of an instance, and what the table says about it: logic, incremental."""
    path = next(iter(pathlib.Path("smtlib").glob(f"*/*/{base}")), None)
    if path is None:
        return None, "?", ""
    text = path.read_text(errors="replace")
    logic = m.group(1) if (m := re.search(r"\(set-logic ([A-Z_]+)\)", text)) else str(path.parts[1])
    incremental = "✓" if text.count("(check-sat") > 1 or "(push" in text else "✗"
    return path, logic, incremental


def sig2(x):
    """Two significant digits, as a plain decimal (three decimals at most: the input has no more)."""
    import math
    if x == 0:
        return "0"
    exponent = math.floor(math.log10(abs(x)))
    return f"{round(x, min(3, -(exponent - 1))):.{min(3, max(0, 1 - exponent))}f}"


rows = list(csv.DictReader(open(sys.argv[1]), delimiter="\t"))
solvers = sys.argv[2].split(",") if len(sys.argv) > 2 else [
    "vik-jvm", "vik-native", "vik-node", "z3", "cvc5", "princess", "smtinterpol"
]
names = {"vik-jvm": "VIK<br>(JVM)", "vik-native": "VIK<br>(native)", "vik-node": "VIK<br>(Node)", "z3": "z3",
         "cvc5": "cvc5", "princess": "princess", "smtinterpol": "SMTInterpol"}
by = collections.defaultdict(dict)
status = {}
for r in rows:
    by[r["file"]][r["solver"]] = (r["answer"], float(r["median"]))
    status[r["file"]] = r["expected"]

print("| Instance | Theory | Incremental | Status | " + " | ".join(names[s] for s in solvers) + " |")
print("|---|---|---|---|" + "---:|" * len(solvers))
for f, answers in by.items():
    best = min((median for answer, median in answers.values() if answer == status[f]), default=None)
    cells = []
    for s in solvers:
        answer, median = answers.get(s, ("missing", 0.0))
        if answer == "timeout":
            cells.append("timeout")
        elif answer not in ("sat", "unsat"):
            cells.append(f"**{answer}**")
        elif answer != status[f]:
            cells.append(f"**{answer}** {sig2(median)}")
        else:
            text = sig2(median)
            cells.append(f'<span class="best">{text}</span>' if median == best else text)
    path, logic, incremental = corpus(f)
    name = f"[`{f[:-5]}`](https://github.com/mbovel/vik/blob/main/{path})" if path else f"`{f[:-5]}`"
    print(f"| {name} | {logic} | {incremental} | {status[f]} | " + " | ".join(cells) + " |")
