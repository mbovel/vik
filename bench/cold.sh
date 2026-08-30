#!/usr/bin/env bash
# Cold-start comparison, as a competition measures it: a new process per instance and solver, the whole
# answer end to end (start-up, parsing, solving). VIK is measured on the three platforms it targets.
#
# Usage, from the repository root: bench/cold.sh <instances-file> [repetitions] [timeout-seconds] > results.tsv
# The instances file holds one path per line. Every run is repeated, and the median (with the extremes) is
# reported, so a scheduling hiccup does not decide a number.
set -u
cd "$(dirname "$0")/.."
INSTANCES="$1"
REPETITIONS="${2:-3}"
TIMEOUT="${3:-30}"

CP=$(sbt "print bench/runtimeClasspath" 2>/dev/null | grep -E "^/" | tail -1)
NATIVE=$(find target/out/native0.5 -path "*/native/net.bovel.smt2.smt2" -type f | head -1)
MODULE=docs/_assets/js/vik/main.js
NODE_RUNNER=bench/vik-node.mjs

seconds() { # the wall-clock time of one run, and the answer; `timeout` when it does not finish
  local start end out status
  start=$(date +%s.%N)
  out=$(timeout "$TIMEOUT" "$@" 2>/dev/null | grep -E '^(sat|unsat|unknown)$' | head -1)
  status=$?
  end=$(date +%s.%N)
  printf "%s\t%.3f" "${out:-timeout}" "$(echo "$end - $start" | bc)"
}

measure() { # name, command...: repeats the run and reports the median, the minimum and the maximum
  local name=$1; shift
  local times=() answer="" line
  for _ in $(seq "$REPETITIONS"); do
    line=$(seconds "$@")
    answer=$(cut -f1 <<< "$line")
    times+=("$(cut -f2 <<< "$line")")
  done
  local sorted=($(printf "%s\n" "${times[@]}" | sort -n))
  printf "%s\t%s\t%s\t%s\t%s\n" "$name" "$answer" "${sorted[$((${#sorted[@]} / 2))]}" "${sorted[0]}" "${sorted[-1]}"
}

printf "file\texpected\tsolver\tanswer\tmedian\tmin\tmax\n"
while read -r f; do
  [ -z "$f" ] && continue
  expected=$(grep -m1 -o ':status [a-z]*' "$f" | cut -d' ' -f2)
  base=$(basename "$f")
  {
    measure vik-jvm java -cp "$CP" net.bovel.smt2.smt2 "$f"
    [ -n "$NATIVE" ] && measure vik-native "$NATIVE" "$f"
    [ -f "$MODULE" ] && measure vik-node node "$NODE_RUNNER" "$f"
    measure z3 z3 "$f"
    measure cvc5 cvc5 "$f"
    measure princess java -cp "$CP" ap.CmdlMain "$f"
    measure smtinterpol java -cp "$CP" de.uni_freiburg.informatik.ultimate.smtinterpol.Main -q "$f"
  } | while read -r line; do printf "%s\t%s\t%s\n" "$base" "$expected" "$line"; done
done < "$INSTANCES"
