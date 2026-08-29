#!/usr/bin/env bash
# Wall-clock comparison of VIK's smt2 solver with z3, cvc5, princess and SMTInterpol on the benchmark files.
# Usage, from the repository root: bench/compare.sh [timeout-seconds] [file-regex] > results.tsv
set -u
cd "$(dirname "$0")/.."
TIMEOUT="${1:-60}"
FILES=$(ls bench/src/main/resources/smtlib/*.smt2 | grep -E "${2:-.}") # optional second argument: a regex selecting files
# One classpath for the three JVM solvers (VIK, princess, SMTInterpol): bench depends on all of them.
CP=$(sbt "print bench/runtimeClasspath" 2>/dev/null | grep -E "^/" | tail -1)
run() { # name, command...
  local name=$1; shift
  local start end out
  start=$(date +%s.%N)
  out=$(timeout "$TIMEOUT" "$@" 2>&1 | grep -E '^(sat|unsat|unknown)$' | head -1)
  end=$(date +%s.%N)
  printf "%s\t%s\t%.2f\n" "$name" "${out:-timeout}" "$(echo "$end - $start" | bc)"
}
printf "file\texpected\tsolver\tanswer\tseconds\n"
for f in $FILES; do
  expected=$(grep -m1 -o ':status [a-z]*' "$f" | cut -d' ' -f2)
  base=$(basename "$f")
  for line in \
    "$(run vik java -cp "$CP" net.bovel.smt2.smt2 "$f")" \
    "$(run z3 z3 "$f")" \
    "$(run cvc5 cvc5 "$f")" \
    "$(run princess java -cp "$CP" ap.CmdlMain "$f")" \
    "$(run smtinterpol java -cp "$CP" de.uni_freiburg.informatik.ultimate.smtinterpol.Main -q "$f")"; do
    printf "%s\t%s\t%s\n" "$base" "$expected" "$line"
  done
done
