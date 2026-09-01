#!/usr/bin/env bash
# Cold-start comparison, as a competition measures it: a new process per instance and solver, the whole
# answer end to end (start-up, parsing, solving). VIK is measured on the three platforms it targets.
#
# Usage, from the repository root: bench/cold.sh [instances-file] [repetitions] [timeout-seconds] > results.tsv
# The default instances are the ones of the evaluation page, in bench/evaluation-instances.txt.
# The instances file holds one path per line. Every run is repeated, and the median (with the extremes) is
# reported, so a scheduling hiccup does not decide a number.
set -u
cd "$(dirname "$0")/.."
INSTANCES="${1:-bench/evaluation-instances.txt}"
REPETITIONS="${2:-3}"
TIMEOUT="${3:-5}"  # the five-second budget: past it the cell says `timeout`, and nobody waits

CP=$(sbt "print bench/runtimeClasspath" 2>/dev/null | sed -E $'s/\x1b\\[[0-9;]*[A-Za-z]//g' | grep -E "^/" | tail -1)
# A broken classpath must fail here, loudly — not as thirty silent `error` rows.
java -cp "$CP" net.bovel.vik.smt2 /dev/null >/dev/null 2>&1 ||
  { echo "cold.sh: classpath canary failed (CP length ${#CP}: ${CP:0:120})" >&2; exit 1; }
NATIVE=$(find target/out/native0.5 -path "*/native/net.bovel.vik.smt2" -type f 2>/dev/null | head -1)
MODULE=docs/_assets/js/vik/main.js
NODE_RUNNER=bench/vik-node.mjs

seconds() { # the wall-clock time of one run, and the answers (comma-separated for an incremental script);
  # `timeout` when it does not finish, `error` when it stops without answering
  local start end raw code out
  start=$(date +%s.%N)
  raw=$(timeout "$TIMEOUT" "$@" 2>/dev/null)
  code=$?
  end=$(date +%s.%N)
  out=$(grep -E '^(sat|unsat|unknown)$' <<< "$raw" | paste -sd, -)
  if [ -z "$out" ]; then if [ "$code" = 124 ]; then out=timeout; else out=error; fi; fi
  printf "%s\t%.3f" "$out" "$(echo "$end - $start" | bc)"
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
  [ -r "$f" ] || { echo "cold.sh: no such instance: $f" >&2; exit 1; }
  expected=$(grep -o ':status [a-z]*' "$f" | cut -d' ' -f2 | paste -sd, -)
  inc=$(grep -q "(push" "$f" && echo --incremental)
  base=$(basename "$f")
  {
    measure vik-jvm java -cp "$CP" net.bovel.vik.smt2 "$f"
    [ -n "$NATIVE" ] && measure vik-native "$NATIVE" "$f"
    [ -f "$MODULE" ] && measure vik-node node "$NODE_RUNNER" "$f"
    measure z3 z3 "$f"
    measure cvc5 cvc5 $inc "$f"
    measure princess java -cp "$CP" ap.CmdlMain "$f"
    measure smtinterpol java -cp "$CP" de.uni_freiburg.informatik.ultimate.smtinterpol.Main -q "$f"
  } | while read -r line; do printf "%s\t%s\t%s\n" "$base" "$expected" "$line"; done
done < "$INSTANCES"
