#!/usr/bin/env python3
"""A markdown table from the cold history of the evaluation: cold2md.py bench/results/evaluation

The directory holds the files of bench/history.py, and a solver's column is its newest run among the cold
ones, those of cold.sh (`history.latest`); the table's caption names each column's version and run. One
row per instance — linked to its corpus file, with its logic, whether the script is incremental and its
status — and one column per solver: the median of the repetitions in seconds, the best of the row highlighted
(`<span class="best">`, styled by the site's `tables.css`) and the worst too (`<span class="worst">`: the 🐢
of a solver that did not finish, else the slowest). A row the history holds as a word has no time: 🐢 for
`timeout`, `—` for `error`, and `wrong` or `unknown` in bold, a wrong answer being named on stderr as
well. A cell with a time also has it in seconds, unrounded, as `data-seconds` (`history.cell`): the chart of
the page is drawn from the two tables. `--order file`
(bench/evaluation-instances.txt: a corpus path first on each line) fixes the rows and their order: an
instance measured but not listed is left out, one listed but not measured gets a row of `—`. `--columns a,b`
shows those solvers instead of every one measured. Run from the repository root, where `corpus/` is.
"""
import collections, pathlib, re, statistics, sys

import history


def corpus(base):
    """The corpus file of an instance, and what the table says about it: logic, incremental, status."""
    path = next(iter(pathlib.Path("corpus").glob(f"*/*/{base}")), None)
    if path is None:
        return None, "?", "", ""
    text = path.read_text(errors="replace")
    logic = m.group(1) if (m := re.search(r"\(set-logic ([A-Z_]+)\)", text)) else str(path.parts[1])
    incremental = "✓" if text.count("(check-sat") > 1 or "(push" in text else "✗"
    checks = text.count("(check-sat")
    m = re.search(r"\(set-info :status (sat|unsat)\)", text)  # the command: a comment can name statuses too
    status = f"{checks} checks" if checks > 1 else m.group(1) if m else "unknown"
    return path, logic, incremental, status


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

rows = history.latest(arguments[0], "cold")
measured = {r["solver"] for r in rows}
# The columns in an order that puts like next to like: the JVM solvers, then VIK's other platforms, then the
# native binaries of z3 and cvc5; a column per solver the history holds. VIK's go from the slowest to start
# to the fastest: the chart of the page takes the last of them as the 1× of its relative times.
solvers = columns or [s for s in [
    "princess", "smtinterpol", "vik-jvm", "vik-jvm-aot", "vik-node", "vik-native", "vik-graal", "z3", "cvc5"
] if s in measured]
names = {"princess": "Princess (JVM)", "smtinterpol": "SMTInterpol (JVM)", "vik-jvm": "VIK (JVM)",
         "vik-jvm-aot": "VIK (JVM AOT)", "vik-node": "VIK (Node)", "vik-native": "VIK (Scala Native)",
         "vik-graal": "VIK (Native Image)", "z3": "Z3", "cvc5": "CVC5"}
by = collections.defaultdict(dict)  # instance -> solver -> the median of the row, or the word it holds instead
for r in rows:
    cell = r["samples_s"]
    by[r["instance"]][r["solver"]] = cell if cell in history.WORDS else statistics.median(map(float, cell.split()))

print('<div class="table-scroll">')
print('<table id="cold-times">')
print(f"<caption>{history.provenance(rows, solvers, names)}</caption>")
print("<thead><tr>" + "".join(f"<th>{h}</th>" for h in ["Instance", "Theory", "Incremental", "Status"])
      + "".join(f"<th>{names[s]}</th>" for s in solvers) + "</tr></thead>")
print("<tbody>")
for instance in (order_of if order_of is not None else list(by)):
    row = by.get(instance, {})
    medians = [cell for cell in row.values() if cell not in history.WORDS]
    best = min(medians, default=None)
    # The worst of the row: a timeout when there is one, else the slowest.
    worst = None if "timeout" in row.values() else max(medians, default=None)
    cells = []
    for s in solvers:
        cell = row.get(s, "error")  # not measured reads as an error does: `—`
        if cell == "timeout":
            cells.append(history.cell('<span class="worst">🐢</span>'))
        elif cell == "error":
            cells.append(history.cell("—"))
        elif cell in history.WORDS:
            cells.append(history.cell(f"<strong>{cell}</strong>"))
            if cell == "wrong":
                print(f"cold2md: the table shows a wrong answer, of {names[s]} on {instance}", file=sys.stderr)
        else:
            text = fmt(cell)
            cells.append(history.cell(f'<span class="best">{text}</span>' if cell == best
                                      else f'<span class="worst">{text}</span>' if cell == worst and best != worst
                                      else text, cell))
    path, logic, incremental, status = corpus(instance + ".smt2")
    short = instance if len(instance) <= 20 else instance[:19] + "…"  # the link's title carries the full name
    label = short.replace("__", "__<wbr>")  # long names may wrap there, and only there
    name = f'<a href="https://github.com/mbovel/vik/blob/main/{path}" title="{instance}">{label}</a>' if path else label
    print(f"<tr><td>{name}</td><td>{logic}</td><td>{incremental}</td><td>{status}</td>"
          + "".join(cells) + "</tr>")

print("</tbody>")
print("</table>")
print('</div>')
