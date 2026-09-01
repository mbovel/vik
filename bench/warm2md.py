#!/usr/bin/env python3
"""A markdown table from a JMH run of OtherSolversBench: warm2md.py results.json [more.json ...] [--blank solver:instance,...]

One row per instance — linked to its corpus file, with its logic, whether the script is incremental and its
status — and one column per solver: the mean over the samples in milliseconds, the best of the row
highlighted (`<span class="best">`, styled by the site's `tables.css`). A note after the table summarizes the
95% confidence intervals (Student's t over the samples of a cell) and names the widest. `--blank` leaves out
the cells of a solver on an instance where the measurement would not mean what the column says (see the
evaluation page). Run from the repository root, where `smtlib/` is.
"""
import collections, json, pathlib, re, statistics, sys
from scipy.stats import t

order = ["vik", "vikText", "z3Direct", "z3", "cvc5", "princess", "smtinterpol"]
names = {"vik": "VIK (API)", "vikText": "VIK (SMT2)", "z3Direct": "z3 (API)", "z3": "z3 (SMT2)",
         "cvc5": "cvc5 (SMT2)", "princess": "princess (API)", "smtinterpol": "SMTInterpol (API)"}


def corpus(base):
    """The corpus file of an instance, and what the table says about it: logic, incremental, status."""
    path = next(iter(pathlib.Path("smtlib").glob(f"*/*/{base}")), None)
    if path is None:
        return None, "?", "", ""
    text = path.read_text(errors="replace")
    logic = m.group(1) if (m := re.search(r"\(set-logic ([A-Z_]+)\)", text)) else str(path.parts[1])
    incremental = "✓" if text.count("(check-sat") > 1 or "(push" in text else "✗"
    checks = text.count("(check-sat")
    m = re.search(r":status (sat|unsat)", text)
    status = f"{checks} checks" if checks > 1 else m.group(1) if m else "unknown"
    return path, logic, incremental, status


def fmt(x):
    """Two decimals; below half a hundredth the cell says <0.01 rather than a misleading 0.00."""
    return "<0.01" if 0 < x < 0.005 else f"{x:.2f}"


arguments = sys.argv[1:]
blank = set()
if "--blank" in arguments:
    i = arguments.index("--blank")
    blank = {tuple(pair.split(":")) for pair in arguments[i + 1].split(",")}
    arguments = arguments[:i] + arguments[i + 2:]

by = collections.defaultdict(dict)
for path in arguments:
    try:
        records = json.load(open(path))
    except json.JSONDecodeError:
        print(f"warm2md: skipping unreadable {path} (a run in progress?)", file=sys.stderr)
        continue
    for r in records:
        solver = r["benchmark"].split(".")[-1]
        instance = r["params"]["file"].removesuffix(".smt2")
        xs = [x for iteration in r["primaryMetric"]["rawData"] for x in iteration]
        mean = statistics.mean(xs)
        ci = t.ppf(0.975, len(xs) - 1) * statistics.stdev(xs) / len(xs) ** 0.5 if len(xs) > 1 else 0.0
        by[instance][solver] = (mean, ci)

# A real HTML table in a scrolling wrapper: narrow screens scroll the wrapper (a table made its own
# scroll container renders badly on iOS), wide screens keep the sticky vertical headers.
print('<div class="table-scroll">')
print("<table>")
print("<thead><tr>" + "".join(f"<th>{h}</th>" for h in ["Instance", "Theory", "Incremental", "Status"])
      + "".join(f"<th>{names[s]}</th>" for s in order) + "</tr></thead>")
print("<tbody>")
intervals = []  # (ci / mean, solver, instance) of every cell shown
for instance, solvers in by.items():
    shown = {s: solvers[s] for s in order if s in solvers and (s, instance) not in blank}
    best = min((mean for mean, _ in shown.values()), default=None)
    cells = []
    for s in order:
        if s not in shown:
            cells.append("—")
        else:
            mean, ci = shown[s]
            intervals.append((ci / mean, s, instance))
            text = fmt(mean)
            cells.append(f'<span class="best">{text}</span>' if mean == best else text)
    path, logic, incremental, status = corpus(instance + ".smt2")
    short = instance if len(instance) <= 20 else instance[:19] + "…"  # the link's title carries the full name
    label = short.replace("__", "__<wbr>")  # long names may wrap there, and only there
    name = f'<a href="https://github.com/mbovel/vik/blob/main/{path}" title="{instance}">{label}</a>' if path else label
    print(f"<tr><td>{name}</td><td>{logic}</td><td>{incremental}</td><td>{status}</td>"
          + "".join(f'<td align="right">{c}</td>' for c in cells) + "</tr>")

print("</tbody>")
print("</table>")
print("</div>")

average = 100 * statistics.mean(p for p, _, _ in intervals)
widest = sorted((x for x in intervals if 100 * x[0] >= 20), reverse=True)
print()
if widest:
    named = ", ".join(f"{names[s]} on `{i}` ({100 * p:.0f}%)" for p, s, i in widest)
    print(f"The 95% confidence intervals (Student's t over the samples of a cell) average {average:.0f}% of"
          f" the mean; every cell is within 20% of its mean except {named}.")
else:
    p, s, i = max(intervals)
    print(f"The 95% confidence intervals (Student's t over the samples of a cell) average {average:.0f}% of"
          f" the mean, at most {100 * p:.0f}% ({names[s]} on `{i}`).")
