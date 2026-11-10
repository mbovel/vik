#!/bin/bash
# The evaluation, one command: measures VIK, the other solvers or both, appends the run to the history
# (bench/results/evaluation, four CSV files, see bench/history.py) and regenerates
# docs/_docs/evaluation.md. Does not commit.
#
#   bench/evaluate.sh vik [warm|cold]      # VIK alone: what a change to VIK calls for
#   bench/evaluate.sh others [warm|cold]   # z3, cvc5, princess and SMTInterpol alone
#   bench/evaluate.sh all [warm|cold]      # both families in one run
#   bench/evaluate.sh regen                # the tables from the history already on disk
#
# Without `warm` or `cold` both tables are measured, the cold one first. The other solvers are pinned, so
# their columns outlive a change to VIK: a table shows each solver's newest run, and says below it which
# version that was and when. `others` is due after a new version of a solver or of the JDK, a change to a
# replay (bench/src/main/scala/io/bovel/bench/evaluation) or to the instances, and on another machine;
# `all` when the columns must come from the same hours, the machine being shared.
#
# Every single measurement lives under the ten-second budget: a warm operation
# past 10 s is halted and noted on bench/timeouts.txt (see bench Budget.scala),
# never run again, and shown as 🐢 in the table; a cold run is cut by
# `timeout 10` (cold.sh) and shown the same way. The instances are
# bench/evaluation-instances.txt, in the order of both tables; which solver
# runs on which is in the suites (bench/src/main/scala/io/bovel/bench/evaluation).
#
# For a dry run: VIK_EVALUATION_RESULTS names another directory than bench/results/evaluation (regen reads
# it too, and rewrites the page from it), VIK_EVALUATION_INSTANCES another list for the cold run and the
# tables' rows, VIK_EVALUATION_JMH more JMH options (`-f 1 -wi 0 -i 1 -p file=a.smt2,b.smt2`).
set -euo pipefail
cd "$(dirname "$0")/.."

R=${VIK_EVALUATION_RESULTS:-bench/results/evaluation}
INSTANCES=${VIK_EVALUATION_INSTANCES:-bench/evaluation-instances.txt}
T=target/evaluation # what JMH and cold.sh wrote, kept out of git: the history holds the samples
RUN=$(date +%FT%H:%M)

usage() { echo "usage: bench/evaluate.sh vik|others|all [warm|cold] | regen"; exit 2; }
preflight() {
  [ -x bench/.venv/bin/python ] ||
    { echo "missing bench/.venv — run: python3 -m venv bench/.venv && bench/.venv/bin/pip install -r bench/requirements.txt"; exit 1; }
  [ -s bench/native/libz3.so ] ||
    { echo "missing native libraries — run: bench/fetch-native.sh"; exit 1; }
}
preflight_cold() {
  [ -x bench/native/bin/z3 ] && [ -x bench/native/bin/cvc5 ] ||
    { echo "missing the z3 and cvc5 binaries — run: bench/fetch-native.sh"; exit 1; }
}

# What the rows of this run will say was measured, taken before anything is built or written: VIK's commit,
# `-dirty` when the tree differs from it in more than results and pages, and the classpath, whose jar names
# carry the versions of the other solvers.
start() {
  VIK=$(git rev-parse --short HEAD)
  [ -z "$(git status --porcelain --untracked-files=no -- . ':!bench/results' ':!docs/_docs')" ] || VIK=$VIK-dirty
  CP=$(sbt --server --no-colors "print bench/runtimeClasspath" | grep -E "^/" | tail -1) ||
    { echo "sbt printed no bench/runtimeClasspath"; exit 1; }
  mkdir -p "$T"
}

# A step's rows join the history once the step is over, whole: a run that fails leaves no row. If the
# recording itself fails, the measurements are still in the file named here.
record() { # warm|cold, the results file
  python3 bench/history.py "$1" --into "$R" --run "$RUN" --vik "$VIK" --classpath "$CP" "$2" ||
    { echo "the run is in $2, not in the history: bench/history.py $1 --into $R --run $RUN --vik $VIK --classpath … $2"; exit 1; }
}

# The suites of io.bovel.bench.evaluation, one JMH run: VikBench, the suites of the other solvers, or all
# of them; the forks and iterations are their annotations (3 forks, 3 warm-up and 3 measurement iterations).
# -foe true: a failure ends the run, JMH's results file included. A fork the Budget watchdog halts
# is one — the combo is on bench/timeouts.txt by then, so the next run passes it at once (the
# ledger, not its measurement, decides its cell: 🐢) — and a new one past the budget is rare.
# No -gc true: a forced GC between iterations measured the JVM solvers 27–51% slower (2026-09-02,
# VIK and SMTInterpol on icl662 and CAV_2009), and z3's binding needs none (see Z3Replay.check).
warm() {
  local suites
  case "$WHO" in
    vik) suites="io.bovel.bench.evaluation.VikBench" ;;
    others) suites="-e io.bovel.bench.evaluation.VikBench io.bovel.bench.evaluation.*" ;;
    all) suites="io.bovel.bench.evaluation.*" ;;
  esac
  local json="$PWD/$T/${RUN/:/}-$WHO-warm.json"
  local chain="bench/Jmh/run -foe true -rf json -rff $json $suites ${VIK_EVALUATION_JMH:-}"
  # The ceiling catches a wedged run; -k because sbt can ignore TERM while its fork lives. Six hours: the
  # 454 benchmarks of `all` take three at the least (three forks of six one-second iterations each), and
  # an operation of several seconds stretches its iterations.
  # --server: the whole chain in one foreground JVM, no thin client, no background server left.
  # --foreground: keep sbt in the terminal's process group. Put in its own group (the default),
  # sbt restoring the terminal at exit gets SIGTTOU and stops forever, the whole chain done.
  local ledger_before; ledger_before=$(cat bench/timeouts.txt)
  timeout --foreground -k 30 21600 sbt --server "$chain"
  # A combo the watchdog noted during this run is news: name it, once, here.
  if [ "$(cat bench/timeouts.txt)" != "$ledger_before" ]; then
    echo "newly past the ten-second budget (bench/timeouts.txt, skipped from now on):"
    comm -13 <(sort <<<"$ledger_before") <(sort bench/timeouts.txt)
  fi
  record warm "$json"
}

cold() {
  local tsv="$T/${RUN/:/}-$WHO-cold.tsv"
  bench/cold.sh "$INSTANCES" 3 10 "$WHO" > "$tsv"
  record cold "$tsv"
}

# Each table from its two files, a solver's column being its newest run (history.py); a table that has no
# run in the history yet stays as the page has it.
regen() {
  local tmp; tmp=$(mktemp -d)
  local warm_md=- cold_md=-
  if [ -s "$R/vik-warm.csv" ] || [ -s "$R/others-warm.csv" ]; then
    warm_md=$tmp/warm.md
    bench/.venv/bin/python bench/warm2md.py "$R/vik-warm.csv" "$R/others-warm.csv" \
      --order "$INSTANCES" --timeouts bench/timeouts.txt > "$warm_md"
  fi
  if [ -s "$R/vik-cold.csv" ] || [ -s "$R/others-cold.csv" ]; then
    cold_md=$tmp/cold.md
    bench/.venv/bin/python bench/cold2md.py "$R/vik-cold.csv" "$R/others-cold.csv" --order "$INSTANCES" > "$cold_md"
  fi
  [ "$warm_md$cold_md" != "--" ] || { echo "no run in $R yet — run: bench/evaluate.sh all"; exit 1; }
  python3 bench/splice.py "$warm_md" "$cold_md"
  rm -r "$tmp"
}

WHO=${1:-}
TABLE=${2:-both}
case "$WHO" in
  regen) [ $# = 1 ] || usage; preflight; regen; exit ;;
  vik|others|all) ;;
  *) usage ;;
esac
case "$TABLE" in warm|cold|both) ;; *) usage ;; esac
preflight
[ "$TABLE" = warm ] || [ "$WHO" = vik ] || preflight_cold
start
[ "$TABLE" = warm ] || cold
[ "$TABLE" = cold ] || warm
regen
