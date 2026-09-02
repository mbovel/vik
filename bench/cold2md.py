#!/usr/bin/env python3
"""A markdown table from the output of cold.sh: cold2md.py results.tsv [solver,...]

One row per instance — linked to its corpus file, with its logic, whether the script is incremental and its
status — and one column per solver: the median of the repetitions in seconds, the best of the row highlighted
(`<span class="best">`, styled by the site's `tables.css`); `timeout` when the solver did not finish, and the
answer in bold when it disagrees with the file's status. `--blank solver:instance,...` leaves out the cells
where the measurement would not mean what the column says (see the evaluation page). Run from the repository
root, where `corpus/` is.
"""
import collections, csv, pathlib, re, sys


def corpus(base):
    """The corpus file of an instance, and what the table says about it: logic, incremental."""
    path = next(iter(pathlib.Path("corpus").glob(f"*/*/{base}")), None)
    if path is None:
        return None, "?", ""
    text = path.read_text(errors="replace")
    logic = m.group(1) if (m := re.search(r"\(set-logic ([A-Z_]+)\)", text)) else str(path.parts[1])
    incremental = "✓" if text.count("(check-sat") > 1 or "(push" in text else "✗"
    return path, logic, incremental


def fmt(x):
    """Two decimals; below half a hundredth the cell says <0.01 rather than a misleading 0.00."""
    return "<0.01" if 0 < x < 0.005 else f"{x:.2f}"


arguments = sys.argv[1:]
blank = set()
if "--blank" in arguments:
    i = arguments.index("--blank")
    blank = {tuple(pair.split(":")) for pair in arguments[i + 1].split(",")}
    arguments = arguments[:i] + arguments[i + 2:]

rows = list(csv.DictReader(open(arguments[0]), delimiter="\t"))
solvers = arguments[1].split(",") if len(arguments) > 1 else [
    "vik-jvm", "vik-native", "vik-node", "z3", "cvc5", "princess", "smtinterpol"
]
names = {"vik-jvm": "VIK (JVM)", "vik-native": "VIK (native)", "vik-node": "VIK (Node)", "z3": "z3",
         "cvc5": "cvc5", "princess": "princess", "smtinterpol": "SMTInterpol"}
def agrees(answer, expected):
    """Answer sequences match the statuses; a check whose status is unknown accepts either answer."""
    a, e = answer.split(","), expected.split(",")
    return len(a) == len(e) and all(x == y or y == "unknown" for x, y in zip(a, e))


by = collections.defaultdict(dict)
status = {}
for r in rows:
    by[r["file"]][r["solver"]] = (r["answer"], float(r["median"]))
    status[r["file"]] = r["expected"]

print('<div class="table-scroll">')
print("<table>")
print("<thead><tr>" + "".join(f"<th>{h}</th>" for h in ["Instance", "Theory", "Incremental", "Status"])
      + "".join(f"<th>{names[s]}</th>" for s in solvers) + "</tr></thead>")
print("<tbody>")
for f, answers in by.items():
    best = min((median for answer, median in answers.values() if agrees(answer, status[f])), default=None)
    cells = []
    for s in solvers:
        answer, median = answers.get(s, ("missing", 0.0))
        if (s, f[:-5]) in blank:
            cells.append("—")
        elif answer == "timeout":
            cells.append("timeout")
        elif answer == "error":
            cells.append("—")
        elif agrees(answer, status[f]):
            text = fmt(median)
            cells.append(f'<span class="best">{text}</span>' if median == best else text)
        elif "," in answer or "," in status[f]:
            cells.append(f"<strong>wrong</strong> {fmt(median)}")
        else:
            cells.append(f"<strong>{answer}</strong> {fmt(median)}")
    path, logic, incremental = corpus(f)
    instance = f[:-5]
    short = instance if len(instance) <= 20 else instance[:19] + "…"  # the link's title carries the full name
    label = short.replace("__", "__<wbr>")  # long names may wrap there, and only there
    name = f'<a href="https://github.com/mbovel/vik/blob/main/{path}" title="{instance}">{label}</a>' if path else label
    checks = status[f].count(",") + 1
    shown = status[f] if checks == 1 else f"{checks} checks"
    print(f"<tr><td>{name}</td><td>{logic}</td><td>{incremental}</td><td>{shown}</td>"
          + "".join(f'<td align="right">{c}</td>' for c in cells) + "</tr>")

print("</tbody>")
print("</table>")
print('</div>')
