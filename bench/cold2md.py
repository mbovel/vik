#!/usr/bin/env python3
"""A markdown table from the cold history of the evaluation: cold2md.py vik-cold.csv others-cold.csv

The files are those of bench/history.py (the runs of cold.sh), and a solver's column is its newest run in
them (`history.latest`); the table's caption names each column's version and run. One row per instance —
linked to its corpus file, with its logic, whether the script is incremental and its
status — and one column per solver: the median of the repetitions in seconds, the best of the row highlighted
(`<span class="best">`, styled by the site's `tables.css`) and the worst too (`<span class="worst">`: the 🐢
of a solver that did not finish, else the slowest correct answer); the answer in bold when it disagrees
with the file's status. `--order file`
(bench/evaluation-instances.txt: a corpus path first on each line) fixes the rows and their order: an
instance measured but not listed is left out, one listed but not measured gets a row of `—`. `--columns a,b`
shows those solvers instead of every one measured. Run from the repository root, where `corpus/` is.
"""
import collections, pathlib, re, statistics, sys

import history


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
order_of = None  # --order file: the instances of the table, in its order
if "--order" in arguments:
    i = arguments.index("--order")
    order_of = [line.split()[0].rsplit("/", 1)[-1].removesuffix(".smt2")
                for line in pathlib.Path(arguments[i + 1]).read_text().splitlines() if line.startswith("corpus/")]
    arguments = arguments[:i] + arguments[i + 2:]
columns = None  # --columns a,b: the solvers to show, by their names in the files
if "--columns" in arguments:
    i = arguments.index("--columns")
    columns = arguments[i + 1].split(",")
    arguments = arguments[:i] + arguments[i + 2:]

rows = history.latest(arguments)
measured = {r["solver"] for r in rows}
# The columns in an order that puts like next to like: the JVM solvers, then VIK's other platforms, then the
# native binaries of z3 and cvc5; a column per solver the history holds.
solvers = columns or [s for s in [
    "princess", "smtinterpol", "vik-jvm", "vik-jvm-aot", "vik-node", "vik-native", "vik-graal", "z3", "cvc5"
] if s in measured]
names = {"princess": "Princess (JVM)", "smtinterpol": "SMTInterpol (JVM)", "vik-jvm": "VIK (JVM)",
         "vik-jvm-aot": "VIK (JVM AOT)", "vik-node": "VIK (Node)", "vik-native": "VIK (Scala Native)",
         "vik-graal": "VIK (Native Image)", "z3": "Z3", "cvc5": "CVC5"}
def agrees(answer, expected):
    """Answer sequences match the statuses; a check whose status is unknown accepts either answer."""
    a, e = answer.split(","), expected.split(",")
    return len(a) == len(e) and all(x == y or y == "unknown" for x, y in zip(a, e))


by = collections.defaultdict(dict)
status = {}
for r in rows:
    by[r["instance"]][r["solver"]] = (r["answer"], statistics.median(float(x) for x in r["samples_s"].split()))
    status[r["instance"]] = r["expected"]

print('<div class="table-scroll">')
print("<table>")
print(f"<caption>{history.provenance(rows, solvers, names)}</caption>")
print("<thead><tr>" + "".join(f"<th>{h}</th>" for h in ["Instance", "Theory", "Incremental", "Status"])
      + "".join(f"<th>{names[s]}</th>" for s in solvers) + "</tr></thead>")
print("<tbody>")
for instance in (order_of if order_of is not None else list(by)):
    answers = by.get(instance, {})
    expected = status.get(instance, "")
    correct = [median for answer, median in answers.values() if agrees(answer, expected)]
    best = min(correct, default=None)
    # The worst of the row: a timeout when there is one, else the slowest correct answer.
    timed_out = any(answer == "timeout" for answer, _ in answers.values())
    worst = None if timed_out else max(correct, default=None)
    cells = []
    for s in solvers:
        answer, median = answers.get(s, ("missing", 0.0))
        if answer == "timeout":
            cells.append('<span class="worst">🐢</span>')
        elif answer in ("error", "missing"):
            cells.append("—")
        elif agrees(answer, expected):
            text = fmt(median)
            cells.append(f'<span class="best">{text}</span>' if median == best
                         else f'<span class="worst">{text}</span>' if median == worst and best != worst else text)
        elif "," in answer or "," in expected:
            cells.append(f"<strong>wrong</strong> {fmt(median)}")
        else:
            cells.append(f"<strong>{answer}</strong> {fmt(median)}")
    path, logic, incremental = corpus(instance + ".smt2")
    short = instance if len(instance) <= 20 else instance[:19] + "…"  # the link's title carries the full name
    label = short.replace("__", "__<wbr>")  # long names may wrap there, and only there
    name = f'<a href="https://github.com/mbovel/vik/blob/main/{path}" title="{instance}">{label}</a>' if path else label
    checks = expected.count(",") + 1
    shown = (expected or "—") if checks == 1 else f"{checks} checks"
    print(f"<tr><td>{name}</td><td>{logic}</td><td>{incremental}</td><td>{shown}</td>"
          + "".join(f'<td align="right">{c}</td>' for c in cells) + "</tr>")

print("</tbody>")
print("</table>")
print('</div>')
