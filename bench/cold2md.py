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


def fmt(x):
    """Two decimals; below half a hundredth the cell says <0.01 rather than a misleading 0.00."""
    return "<0.01" if 0 < x < 0.005 else f"{x:.2f}"


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

print("| Instance | Theory | Incremental | Status | " + " | ".join(names[s] for s in solvers) + " |")
print("|---|---|---|---|" + "---:|" * len(solvers))
for f, answers in by.items():
    best = min((median for answer, median in answers.values() if answer == status[f]), default=None)
    cells = []
    for s in solvers:
        answer, median = answers.get(s, ("missing", 0.0))
        if answer == "timeout":
            cells.append("timeout")
        elif answer == "error":
            cells.append("—")
        elif answer == status[f]:
            text = fmt(median)
            cells.append(f'<span class="best">{text}</span>' if median == best else text)
        elif "," in answer or "," in status[f]:
            cells.append(f"**wrong** {fmt(median)}")
        else:
            cells.append(f"**{answer}** {fmt(median)}")
    path, logic, incremental = corpus(f)
    label = f[:-5].replace("__", "__<wbr>")  # long names may wrap there, and only there
    name = f'<a href="https://github.com/mbovel/vik/blob/main/{path}"><code>{label}</code></a>' if path else f"<code>{label}</code>"
    checks = status[f].count(",") + 1
    shown = status[f] if checks == 1 else f"{checks} checks"
    print(f"| {name} | {logic} | {incremental} | {shown} | " + " | ".join(cells) + " |")
