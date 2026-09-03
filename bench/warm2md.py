#!/usr/bin/env python3
"""A markdown table from a JMH run of the evaluation suites: warm2md.py results.json [more.json ...]

One row per instance — linked to its corpus file, with its logic, whether the script is incremental and its
status — and one column per solver: the mean over the samples in milliseconds, the best of the row
highlighted (`<span class="best">`, styled by the site's `tables.css`) and the worst too (`<span class="worst">`:
the 🐢 of a solver past the budget, else the slowest). A note after the table summarizes the
95% confidence intervals (Student's t over the samples of a cell) and names the widest. `--timeouts file` reads the ledger the benchmarks' Budget watchdog keeps (one
`solver:instance` per line): those cells say 🐢, whatever an older result file measured. `--order file`
(bench/evaluation-instances.txt: a corpus path first on each line) fixes the rows and their order: an
instance measured but not listed is left out, one listed but not measured gets a row of `—`. Run from the
repository root, where `corpus/` is.
"""
import collections, json, pathlib, re, statistics, sys
from scipy.stats import t

# The columns, like next to like as in the cold table: the JVM solvers, VIK among them, then the native ones.
order = ["PrincessBench.api", "SmtInterpolBench.api", "VikBench.api", "VikBench.text", "Z3Bench.api", "Z3TextBench.text",
         "Cvc5Bench.api", "Cvc5Bench.text"]
names = {"PrincessBench.api": "Princess (API)", "SmtInterpolBench.api": "SMTInterpol (API)", "VikBench.api": "VIK (API)",
         "VikBench.text": "VIK (SMT2)", "Z3Bench.api": "Z3 (API)", "Z3TextBench.text": "Z3 (SMT2)",
         "Cvc5Bench.api": "CVC5 (API)", "Cvc5Bench.text": "CVC5 (SMT2)"}


def corpus(base):
    """The corpus file of an instance, and what the table says about it: logic, incremental, status."""
    path = next(iter(pathlib.Path("corpus").glob(f"*/*/{base}")), None)
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
order_of = None  # --order file: the instances of the table, in its order
if "--order" in arguments:
    i = arguments.index("--order")
    order_of = [line.split()[0].rsplit("/", 1)[-1].removesuffix(".smt2")
                for line in pathlib.Path(arguments[i + 1]).read_text().splitlines() if line.startswith("corpus/")]
    arguments = arguments[:i] + arguments[i + 2:]
timeouts = set()  # (solver, instance) past the ten-second budget: the cell says 🐢
if "--timeouts" in arguments:
    i = arguments.index("--timeouts")
    lines = pathlib.Path(arguments[i + 1]).read_text().splitlines()
    timeouts = {tuple(line.split(":", 1)) for line in lines if line}
    arguments = arguments[:i] + arguments[i + 2:]

by = collections.defaultdict(dict)
samples = {}  # per solver, the samples of its cells (forks × iterations)
for path in arguments:
    try:
        records = json.load(open(path))
    except json.JSONDecodeError:
        print(f"warm2md: skipping unreadable {path} (a run in progress?)", file=sys.stderr)
        continue
    for r in records:
        cls, method = r["benchmark"].split(".")[-2:]
        solver = f"{cls}.{method}"
        instance = r["params"]["file"].removesuffix(".smt2")
        xs = [x for iteration in r["primaryMetric"]["rawData"] for x in iteration]
        mean = statistics.mean(xs)
        ci = t.ppf(0.975, len(xs) - 1) * statistics.stdev(xs) / len(xs) ** 0.5 if len(xs) > 1 else 0.0
        by[instance][solver] = (mean, ci)
        samples[solver] = min(samples.get(solver, len(xs)), len(xs))  # the fewest a cell of the solver has

# A real HTML table in a scrolling wrapper: narrow screens scroll the wrapper (a table made its own
# scroll container renders badly on iOS), wide screens keep the sticky vertical headers.
print('<div class="table-scroll">')
print("<table>")
print("<thead><tr>" + "".join(f"<th>{h}</th>" for h in ["Instance", "Theory", "Incremental", "Status"])
      + "".join(f"<th>{names[s]}</th>" for s in order) + "</tr></thead>")
print("<tbody>")
intervals = []  # (ci / mean, solver, instance) of every cell shown
for instance in (order_of if order_of is not None else list(by)):
    solvers = by.get(instance, {})
    shown = {s: solvers[s] for s in order
             if s in solvers and (s, instance) not in timeouts}
    best = min((mean for mean, _ in shown.values()), default=None)
    # The worst of the row: a timeout when there is one, else the slowest.
    timed_out = any((s, instance) in timeouts for s in order)
    worst = None if timed_out else max((mean for mean, _ in shown.values()), default=None)
    cells = []
    for s in order:
        if (s, instance) in timeouts:
            cells.append('<span class="worst">🐢</span>')
        elif s not in shown:
            cells.append("—")
        else:
            mean, ci = shown[s]
            intervals.append((ci / mean, s, instance))
            text = fmt(mean)
            cells.append(f'<span class="best">{text}</span>' if mean == best
                         else f'<span class="worst">{text}</span>' if mean == worst and best != worst else text)
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
    # A solver wide on many cells (princess: one fork of three samples, so a Student's t of 4.3) is
    # summed up; the others are named one by one.
    parts = []
    for s in order:
        wide = [(p, i) for p, sv, i in widest if sv == s]
        if len(wide) >= 4:
            parts.append(f"{names[s]} on {len(wide)} instances ({100 * min(wide)[0]:.0f}–{100 * max(wide)[0]:.0f}%,"
                         f" from {samples[s]} samples per cell)")
        else:
            parts.extend(f"{names[s]} on `{i}` ({100 * p:.0f}%)" for p, i in wide)
    print(f"The 95% confidence intervals (Student's t over the samples of a cell) average {average:.0f}% of"
          f" the mean; every cell is within 20% of its mean except {', '.join(parts)}.")
else:
    p, s, i = max(intervals)
    print(f"The 95% confidence intervals (Student's t over the samples of a cell) average {average:.0f}% of"
          f" the mean, at most {100 * p:.0f}% ({names[s]} on `{i}`).")
