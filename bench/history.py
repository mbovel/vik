#!/usr/bin/env python3
"""The history of the evaluation: four append-only CSV files, a row per solver, instance and run.

    history.py warm --into DIR --run RUN --vik VERSION --classpath CP results.json   # a JMH run of the suites
    history.py cold --into DIR --run RUN --vik VERSION --classpath CP results.tsv    # the output of cold.sh

`vik-warm.csv` and `others-warm.csv` take the rows of a JMH run, `vik-cold.csv` and `others-cold.csv` those of
cold.sh. VIK and the other solvers are kept apart because they are measured apart: a change to VIK moves
VIK's times only, and the other solvers are pinned (bench/evaluate.sh runs either family). A row says what
was measured and on what, and holds the raw samples; bench/results/evaluation/README.md describes the
columns. The versions of the other solvers are read off the classpath (the JVM solvers and the JavaSMT
bindings) and off bench/native/VERSIONS (the z3 and cvc5 binaries), so a row names what ran.

`latest` picks the rows the tables show (bench/warm2md.py, bench/cold2md.py), `provenance` says which.
"""
import argparse, csv, decimal, json, pathlib, re, socket, subprocess

BENCH = pathlib.Path(__file__).resolve().parent
COLUMNS = {
    "warm": ["run", "host", "jdk", "solver", "version", "instance", "samples_ms"],
    "cold": ["run", "host", "jdk", "solver", "version", "instance", "expected", "answer", "samples_s"],
}


def is_vik(solver):
    """VIK's suites and platforms (`VikBench.api`, `vik-native`), as opposed to the other solvers."""
    return solver.lower().startswith("vik")


def latest(paths):
    """The rows a table shows: on the host of the newest run, each solver's rows from its own newest run.

    A solver's column is one whole run, never a mix: a combination its suite stopped measuring does not
    come back from an older run. Files that do not exist yet hold no rows.
    """
    rows = [r for p in paths if pathlib.Path(p).exists() for r in csv.DictReader(open(p, newline=""))]
    if not rows:
        raise SystemExit(f"history: no run recorded in {', '.join(map(str, paths))}")
    host = max(rows, key=lambda r: r["run"])["host"]
    rows = [r for r in rows if r["host"] == host]
    newest = {}
    for r in rows:
        newest[r["solver"]] = max(newest.get(r["solver"], ""), r["run"])
    return [r for r in rows if r["run"] == newest[r["solver"]]]


def provenance(rows, solvers, names):
    """The runs behind the columns `solvers` of a table, and the version each measured.

    `Run of 2026-11-12 09:14: VIK (JVM) and VIK (Node) 1a2b3c4. Run of 2026-11-10 14:30: Z3 4.14.0; CVC5
    1.3.3.` The newest run first; the solvers of a run that share a version (VIK's platforms) are named
    together.
    """
    def listing(items):
        return items[0] if len(items) == 1 else ", ".join(items[:-1]) + " and " + items[-1]

    of = {r["solver"]: (r["run"], r["version"]) for r in rows}
    runs = {}  # run -> version -> the columns' names, in the table's order
    for s in solvers:
        if s in of:
            run, version = of[s]
            runs.setdefault(run, {}).setdefault(version, []).append(names[s])
    parts = [f"Run of {run.replace('T', ' ')}: "
             + "; ".join(f"{listing(shown)} {version}" for version, shown in runs[run].items()) + "."
             for run in sorted(runs, reverse=True)]
    return f"Measured on {rows[0]['host']}. {' '.join(parts)}"


def digits(x):
    """Four significant digits, written out (`12350`, `0.00004512`): no exponent in the files."""
    return format(decimal.Decimal(f"{x:.4g}"), "f")


def version_of(mode, solver, vik, classpath):
    """The version a solver's rows carry; no row is written without one."""
    if is_vik(solver):
        return vik
    for prefix, artifact in [("princess", "princess_[0-9.]+"), ("smtinterpol", "smtinterpol"),
                             ("z3", "javasmt-solver-z3"), ("cvc5", "javasmt-solver-cvc5")]:
        if solver.lower().startswith(prefix):
            # The warm suites call z3 and cvc5 through their JavaSMT bindings; the cold table runs the
            # binaries of bench/fetch-native.sh, named by its stamp: `z3 4.14.0, cvc5 1.3.3, cvc5 binding …`.
            if mode == "cold" and prefix in ("z3", "cvc5"):
                found = re.search(rf"\b{prefix} ([^,\s]+)", (BENCH / "native" / "VERSIONS").read_text())
            else:  # a Maven path: …/<artifact>/<version>/<artifact>-<version>.jar
                found = re.search(rf"/{artifact}/([^/:]+)/", classpath)
            if found:
                return found.group(1)
    raise SystemExit(f"history: no version found for {solver} (a new solver, or a classpath of another shape?)")


def warm_rows(path):
    """(solver, instance, jdk, samples) of a JMH results file; no samples for a combination on the ledger."""
    ledger = {tuple(line.split(":", 1)) for line in (BENCH / "timeouts.txt").read_text().splitlines() if line}
    for r in json.load(open(path)):
        solver = ".".join(r["benchmark"].split(".")[-2:])
        instance = r["params"]["file"].removesuffix(".smt2")
        samples = [] if (solver, instance) in ledger else [x for fork in r["primaryMetric"]["rawData"] for x in fork]
        yield solver, instance, r["jdkVersion"], [" ".join(map(digits, samples))]


def cold_rows(path):
    """(solver, instance, jdk, expected, answer and seconds) of the output of cold.sh."""
    banner = subprocess.run(["java", "-version"], capture_output=True, text=True).stderr
    jdk = re.search(r'version "([^"]+)"', banner).group(1)  # the JDK on the PATH is the one cold.sh starts
    for r in csv.DictReader(open(path, newline=""), delimiter="\t"):
        yield r["solver"], r["file"].removesuffix(".smt2"), jdk, [r["expected"], r["answer"], r["seconds"]]


def record(mode, into, run, vik, classpath, results):
    host = socket.gethostname().split(".")[0]
    rows = {"vik": [], "others": []}
    for solver, instance, jdk, measured in (warm_rows if mode == "warm" else cold_rows)(results):
        version = version_of(mode, solver, vik, classpath)
        rows["vik" if is_vik(solver) else "others"].append([run, host, jdk, solver, version, instance] + measured)
    header = ",".join(COLUMNS[mode])
    files = {family: pathlib.Path(into) / f"{family}-{mode}.csv" for family, lines in rows.items() if lines}
    for path in files.values():  # both files checked before either is written
        if path.exists() and (first := open(path).readline().strip()) != header:
            raise SystemExit(f"history: {path} starts with `{first}`, not `{header}`")
    for family, path in files.items():
        path.parent.mkdir(parents=True, exist_ok=True)
        new = not path.exists()
        with open(path, "a", newline="") as f:
            out = csv.writer(f, lineterminator="\n")
            if new:
                out.writerow(COLUMNS[mode])
            out.writerows(rows[family])
        print(f"history: {len(rows[family])} rows of run {run} added to {path}")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Appends a run of the evaluation to its history.")
    parser.add_argument("mode", choices=["warm", "cold"])
    parser.add_argument("--into", required=True, help="the directory of the four files")
    parser.add_argument("--run", required=True, help="the start of the run, as 2026-11-12T09:14")
    parser.add_argument("--vik", required=True, help="VIK's version: the commit measured")
    parser.add_argument("--classpath", required=True, help="bench/runtimeClasspath, for the other solvers' versions")
    parser.add_argument("results", help="JMH's JSON results (warm) or the TSV of cold.sh (cold)")
    args = parser.parse_args()
    record(args.mode, args.into, args.run, args.vik, args.classpath, args.results)
