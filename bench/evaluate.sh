#!/bin/bash
# The whole evaluation, one command: measures the warm and cold tables and
# regenerates docs/_docs/evaluation.md. Does not commit.
#
#   bench/evaluate.sh all     # warm + cold + regen
#   bench/evaluate.sh warm    # the warm JMH run + regen
#   bench/evaluate.sh cold    # the cold binaries run + regen
#   bench/evaluate.sh regen   # tables from the dated results already on disk
#
# Every single measurement lives under the ten-second budget: a warm operation
# past 10 s is halted and noted on bench/timeouts.txt (see bench Budget.scala),
# never run again, and shown as 🐢 in the table; a cold run is cut by
# `timeout 10` (cold.sh) and shown the same way. The instances are
# bench/evaluation-instances.txt, in the order of both tables; which solver
# runs on which is in the suites (bench/src/main/scala/io/bovel/bench/evaluation).
set -euo pipefail
cd "$(dirname "$0")/.."

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
R=bench/results/smt
DATE=$(date +%F)
HOST=$(hostname -s)
OUT=$R/$DATE-$HOST

# The suites of io.bovel.bench.evaluation, every instance of every solver, in one JMH run: the
# forks and iterations are their annotations (3 forks, 3 warm-up and 3 measurement iterations).
# -foe true: a failure ends the run, JMH's results file included. A fork the Budget watchdog halts
# is one — the combo is on bench/timeouts.txt by then, so the next run passes it at once (the
# ledger, not its measurement, decides its cell: 🐢) — and a new one past the budget is rare.
# No -gc true: a forced GC between iterations measured the JVM solvers 27–51% slower (2026-09-02,
# VIK and SMTInterpol on icl662 and CAV_2009), and z3's binding needs none (see Z3Replay.check).
warm() {
  local chain="bench/Jmh/run -foe true -rf json -rff $OUT-warm.json io.bovel.bench.evaluation.*"
  # The ceiling catches a wedged run; -k because sbt can ignore TERM while its fork lives.
  # --server: the whole chain in one foreground JVM, no thin client, no background server left.
  # --foreground: keep sbt in the terminal's process group. Put in its own group (the default),
  # sbt restoring the terminal at exit gets SIGTTOU and stops forever, the whole chain done.
  local ledger_before; ledger_before=$(cat bench/timeouts.txt)
  timeout --foreground -k 30 14400 sbt --server "$chain"
  # A combo the watchdog noted during this run is news: name it, once, here.
  if [ "$(cat bench/timeouts.txt)" != "$ledger_before" ]; then
    echo "newly past the ten-second budget (bench/timeouts.txt, skipped from now on):"
    comm -13 <(sort <<<"$ledger_before") <(sort bench/timeouts.txt)
  fi
}

cold() { bench/cold.sh > "$OUT-cold.tsv"; }  # reads bench/evaluation-instances.txt itself

regen() {
  # The newest dated warm file and the newest cold one: one JMH run measures every cell of the warm
  # table, so a table is one file, and a date's file is the whole run. (Names sort chronologically;
  # mtimes lie after a checkout.)
  local warm_json; warm_json=$(ls $R/*-warm.json | tail -1)
  local cold_tsv; cold_tsv=$(ls $R/*-cold.tsv | tail -1)
  echo "regen from: $warm_json $cold_tsv"
  local tmp; tmp=$(mktemp -d)
  bench/.venv/bin/python bench/warm2md.py "$warm_json" --order bench/evaluation-instances.txt --timeouts bench/timeouts.txt > "$tmp/warm.md"
  bench/.venv/bin/python bench/cold2md.py "$cold_tsv" --order bench/evaluation-instances.txt > "$tmp/cold.md"
  python3 bench/splice.py "$tmp/warm.md" "$tmp/cold.md"
  # The raw-data links of the page name the files the tables were built from.
  sed -i -E "s|results/smt/[0-9-]+-laraserver4-cold.tsv|results/smt/$(basename "$cold_tsv")|" docs/_docs/evaluation.md
  sed -i -E "s|results/smt/[0-9-]+-laraserver4-warm\.json|results/smt/$(basename "$warm_json")|" docs/_docs/evaluation.md
  rm -r "$tmp"
}

case "${1:-}" in
  warm) preflight; warm; regen ;;
  cold) preflight; preflight_cold; cold; regen ;;
  regen) preflight; regen ;;
  all) preflight; preflight_cold; cold; warm; regen ;;
  *) echo "usage: bench/evaluate.sh warm|cold|regen|all"; exit 2 ;;
esac
