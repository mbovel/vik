#!/usr/bin/env python3
"""The history of the evaluation: the CSV files of every run, each with a JSON file about the run beside it.

    history.py warm --into DIR --run RUN --vik VERSION --classpath CP results.json   # a JMH run of the suites
    history.py cold --into DIR --run RUN --vik VERSION --classpath CP results.tsv    # the output of cold.sh

Each call writes `<run>-<host>-<solvers>-<table>.csv` (`2026-11-11T0200Z-laraserver4-vik-warm.csv`) and the
`.json` of the same name, once for VIK and once for the other solvers: `vik` and `others` are two series
of files, because the other solvers are pinned and bench/evaluate.sh measures either family alone. A run
of both writes both, under the same `run`. The name says the minute the run started, in UTC, the machine,
the family and the table the rows feed. The CSV file holds the measurements, a row per solver and
instance: the raw samples, or one of `WORDS` when there is no time to show. The JSON file holds what is
the same for every row: the commit of this repository, the JDK and, for the other solvers, their versions,
read off the classpath (the JVM solvers and the JavaSMT bindings) and off bench/native/VERSIONS (the z3 and
cvc5 binaries). Both are written once and never changed; bench/results/evaluation/README.md describes them.

`latest` picks the rows the tables show (bench/warm2md.py, bench/cold2md.py), `provenance` says which.
"""
import argparse, csv, decimal, json, pathlib, re, socket, subprocess, sys

BENCH = pathlib.Path(__file__).resolve().parent
COLUMNS = {"warm": ["solver", "instance", "samples_ms"], "cold": ["solver", "instance", "samples_s"]}
RUN = r"\d{4}-\d\d-\d\dT\d{4}Z"
NAME = re.compile(rf"(?P<run>{RUN})-(?P<host>.+)-(?P<solvers>vik|others)-(?:warm|cold)\.csv")
# What a row holds in place of its samples, the worst first: an answer against the file's status, a run
# past the ten-second budget, a run that did not answer every check, a solver that gave up.
WORDS = ["wrong", "timeout", "error", "unknown"]


def is_vik(solver):
    """VIK's suites and platforms (`VikBench.api`, `vik-native`), as opposed to the other solvers."""
    return solver.lower().startswith("vik")


def latest(directory, mode):
    """The rows a table shows: on the host of the newest run, each solver's rows from its own newest run.

    A solver's column is one whole run, never a mix: a combination its suite stopped measuring does not
    come back from an older run. Each row also says where it comes from: its `run` and its `host`, read
    off the name of its `file`, and the `version` measured, read from the JSON file beside it.
    """
    files = sorted(((m["run"], m["host"], path) for path in pathlib.Path(directory).glob(f"*-{mode}.csv")
                    if (m := NAME.fullmatch(path.name))), reverse=True)
    if not files:
        raise SystemExit(f"history: no {mode} run recorded in {directory}")
    host = files[0][1]
    rows, seen = [], set()
    for run, where, path in files:  # the newest first
        if where == host:
            about = json.loads(path.with_suffix(".json").read_text())
            new = [r for r in csv.DictReader(open(path, newline="")) if r["solver"] not in seen]
            for r in new:
                version = about["commit"] if is_vik(r["solver"]) else about["versions"][r["solver"]]
                rows.append(r | {"run": run, "host": host, "file": path, "version": version})
            seen |= {r["solver"] for r in new}
    return rows


def provenance(rows, solvers, names):
    """The runs behind the columns `solvers` of a table, and the version each measured.

    `Measured on laraserver4. Run of 2026-11-12 09:14 UTC (data: VIK): VIK (JVM) and VIK (Node) 1a2b3c4. Run
    of 2026-11-10 14:30 UTC (data: others): Z3 4.14.0; CVC5 1.3.3.` The newest run first, with the files of
    it that the columns come from, VIK's and the other solvers', each a link when the file is in the
    repository; the solvers of a run that share a version (VIK's platforms) are named together.
    """
    def listing(items):
        return items[0] if len(items) == 1 else ", ".join(items[:-1]) + " and " + items[-1]

    def data(path):
        text = "VIK" if NAME.fullmatch(path.name)["solvers"] == "vik" else "others"
        try:
            inside = path.resolve().relative_to(BENCH.parent)
        except ValueError:  # a dry run's directory, outside the repository
            return text
        return f'<a href="https://github.com/mbovel/vik/blob/main/{inside}">{text}</a>'

    of = {r["solver"]: r for r in rows}
    runs = {}  # run -> version -> the columns' names, in the table's order
    files = {}  # run -> the files of the run behind these columns
    for s in solvers:
        if s in of:
            runs.setdefault(of[s]["run"], {}).setdefault(of[s]["version"], []).append(names[s])
            files.setdefault(of[s]["run"], set()).add(of[s]["file"])
    parts = []
    for run in sorted(runs, reverse=True):
        linked = ", ".join(data(path) for path in sorted(files[run], reverse=True))  # `vik` before `others`
        parts.append(f"Run of {run[:10]} {run[11:13]}:{run[13:15]} UTC (data: {linked}): "
                     + "; ".join(f"{listing(shown)} {version}" for version, shown in runs[run].items()) + ".")
    return f"Measured on {rows[0]['host']}. {' '.join(parts)}"


def digits(x):
    """Four significant digits, written out (`12350`, `0.00004512`): no exponent in the files."""
    return format(decimal.Decimal(f"{x:.4g}"), "f")


def version_of(mode, solver, classpath):
    """The version of one of the other solvers; no run is recorded without the version of each."""
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
    """The JDK of a JMH results file, and its rows: solver, instance and samples, or `timeout` for a
    combination on the ledger."""
    ledger = {tuple(line.split(":", 1)) for line in (BENCH / "timeouts.txt").read_text().splitlines() if line}
    results = json.load(open(path))
    rows = []
    for r in results:
        solver = ".".join(r["benchmark"].split(".")[-2:])
        instance = r["params"]["file"].removesuffix(".smt2")
        samples = " ".join(digits(x) for fork in r["primaryMetric"]["rawData"] for x in fork)
        rows.append([solver, instance, "timeout" if (solver, instance) in ledger else samples])
    return ", ".join(sorted({r["jdkVersion"] for r in results})), rows


def verdict(expected, answer):
    """What the answers of one run are worth against the statuses of its file: `ok`, or one of `WORDS`.

    `timeout` and `error` are cold.sh's own words for a run. Otherwise both are sequences, an entry per
    check-sat: a `sat` against an `unsat` is `wrong`, an `unknown` against either is a solver that gave
    up, and a check whose status is unknown accepts any answer.
    """
    if answer in ("timeout", "error"):
        return answer
    if answer.count(",") != expected.count(","):  # the statuses of the file do not line up with its checks
        return "error"
    pairs = list(zip(answer.split(","), expected.split(",")))
    if any({a, status} == {"sat", "unsat"} for a, status in pairs):
        return "wrong"
    if any(a == "unknown" and status in ("sat", "unsat") for a, status in pairs):
        return "unknown"
    return "ok"


def cold_rows(path):
    """The JDK cold.sh started, and the rows of its output: solver, instance and seconds.

    The seconds are those of the repetitions when every one of them answered as the statuses say; else
    the row holds the worst of their verdicts. This is where the answers are checked: they are not kept.
    """
    banner = subprocess.run(["java", "-version"], capture_output=True, text=True).stderr
    jdk = re.search(r'version "([^"]+)"', banner).group(1)  # the JDK on the PATH is the one cold.sh starts
    rows = []
    for r in csv.DictReader(open(path, newline=""), delimiter="\t"):
        verdicts = {verdict(r["expected"], answer) for answer in r["answer"].split("|")}
        word = next((w for w in WORDS if w in verdicts), None)
        if word == "wrong":
            print(f"history: WRONG ANSWER of {r['solver']} on {r['file']}: {r['answer']}, the status being"
                  f" {r['expected']}", file=sys.stderr)
        rows.append([r["solver"], r["file"].removesuffix(".smt2"), word or r["seconds"]])
    return jdk, rows


def record(mode, into, run, vik, classpath, results):
    if not re.fullmatch(RUN, run):
        raise SystemExit(f"history: the run is named by its start in UTC, as 2026-11-12T0914Z, not `{run}`")
    jdk, rows = (warm_rows if mode == "warm" else cold_rows)(results)
    if not rows:
        raise SystemExit(f"history: {results} holds no measurement")
    host = socket.gethostname().split(".")[0]
    files = {}  # the file of each family measured -> what its JSON file says, its rows
    for family in ("vik", "others"):
        mine = [row for row in rows if is_vik(row[0]) == (family == "vik")]
        if mine:
            about = {"commit": vik, "jdk": jdk}
            if family == "others":
                about["versions"] = {solver: version_of(mode, solver, classpath) for solver, _, _ in mine}
            files[pathlib.Path(into) / f"{run}-{host}-{family}-{mode}.csv"] = (about, mine)
    for path in files:  # every file checked before any is written
        if path.exists():
            raise SystemExit(f"history: {path} exists, and the files of a run are written once")
    for path, (about, mine) in files.items():
        path.parent.mkdir(parents=True, exist_ok=True)
        path.with_suffix(".json").write_text(json.dumps(about, indent=2) + "\n")
        with open(path, "x", newline="") as f:
            out = csv.writer(f, lineterminator="\n")
            out.writerow(COLUMNS[mode])
            out.writerows(mine)
        print(f"history: {len(mine)} rows written to {path}")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Writes a run of the evaluation into its history.")
    parser.add_argument("mode", choices=["warm", "cold"])
    parser.add_argument("--into", required=True, help="the directory of the history")
    parser.add_argument("--run", required=True, help="the start of the run in UTC, as 2026-11-12T0914Z")
    parser.add_argument("--vik", required=True, help="the commit measured, `-dirty` after it for a tree that differs")
    parser.add_argument("--classpath", required=True, help="bench/runtimeClasspath, for the other solvers' versions")
    parser.add_argument("results", help="JMH's JSON results (warm) or the TSV of cold.sh (cold)")
    args = parser.parse_args()
    record(args.mode, args.into, args.run, args.vik, args.classpath, args.results)
