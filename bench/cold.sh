#!/usr/bin/env bash
# Cold-start comparison, as a competition measures it: a new process per instance and solver, the whole
# answer end to end (start-up, parsing, solving). VIK is measured on the three platforms it targets, as a
# GraalVM native image (bench/graal.sh) and on the JVM with a JDK AOT cache (bench/aot.sh) — every one of
# them built here first, from the tree being measured: a binary left behind by a rename or an older build
# lost a column, or measured older code, in silence.
#
# Usage, from the repository root: bench/cold.sh [instances-file] [repetitions] [timeout-seconds] > results.tsv
# The default instances are the ones of the evaluation page, in bench/evaluation-instances.txt.
# The instances file holds one path per line (what follows the path on the line, and `#` lines, are
# skipped). Every run is repeated, and the median (with the extremes) is reported, so a scheduling hiccup
# does not decide a number.
set -u
cd "$(dirname "$0")/.."
PATH="$PWD/bench/native/bin:$PATH" # the z3 and cvc5 binaries of bench/fetch-native.sh, pinned versions
INSTANCES="${1:-bench/evaluation-instances.txt}"
REPETITIONS="${2:-3}"
TIMEOUT="${3:-10}"  # the ten-second budget: past it the cell says 🐢, and nobody waits

# The builds first, their output on stderr (stdout is the table). --server runs them in this process, no
# thin client — a client racing a half-dead background server answered with empty stdout on three of four
# CI runs. A build that fails ends the run here, loudly — not as thirty silent `error` rows or a lost column.
sbt --server --no-colors "vikNative/nativeLink; docsAssets" 1>&2 ||
  { echo "cold.sh: linking the native binary or the WebAssembly module failed" >&2; exit 1; }
bench/graal.sh 1>&2 || { echo "cold.sh: building the GraalVM image failed" >&2; exit 1; }
bench/aot.sh "$INSTANCES" 1>&2 || { echo "cold.sh: training the AOT cache failed" >&2; exit 1; }
CP=$(sbt --server --no-colors "print bench/runtimeClasspath" | grep -E "^/" | tail -1)
java -cp "$CP" io.bovel.vik.run /dev/null >/dev/null 2>&1 ||
  { echo "cold.sh: classpath canary failed (CP length ${#CP}: ${CP:0:120})" >&2; exit 1; }
NATIVE=$(find target/out/native0.5 -path "*/native/io.bovel.vik.run" -type f 2>/dev/null | head -1)
GRAAL=target/graal/vik
AOT=target/aot/vik.aot # with the classpath it was trained on: the cache holds for no other
AOT_CP=$(cat target/aot/classpath.txt)
MODULE=docs/_assets/js/vik/main.js
NODE_RUNNER=bench/vik-node.mjs
for built in "$NATIVE" "$GRAAL" "$AOT" "$MODULE"; do
  [ -s "$built" ] || { echo "cold.sh: built nothing at '$built'" >&2; exit 1; }
done

seconds() { # the wall-clock time of one run, and the answers (comma-separated for an incremental script);
  # `timeout` when it does not finish, `error` when it stops before answering every check-sat
  local start end raw code out
  start=$(date +%s.%N)
  raw=$(timeout "$TIMEOUT" "$@" 2>/dev/null)
  code=$?
  end=$(date +%s.%N)
  out=$(grep -E '^(sat|unsat|unknown)$' <<< "$raw" | paste -sd, -)
  if [ "$code" = 124 ]; then out=timeout
  elif [ "$(tr -cd , <<< "$out" | wc -c)" -lt $((CHECKS - 1)) ] || [ -z "$out" ]; then out=error; fi
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
while read -r f _; do
  [ -z "$f" ] || [ "${f:0:1}" = "#" ] && continue
  [ -r "$f" ] || { echo "cold.sh: no such instance: $f" >&2; exit 1; }
  expected=$(grep -o '(set-info :status [a-z]*)' "$f" | cut -d' ' -f3 | tr -d ')' | paste -sd, -)
  # More than one check-sat, with or without push: cvc5 and princess answer only the first otherwise.
  inc=""; pinc=""
  CHECKS=$(grep -c "(check-sat" "$f")
  if [ "$CHECKS" -gt 1 ]; then inc=--incremental; pinc=+incremental; fi
  base=$(basename "$f")
  {
    measure vik-jvm java -cp "$CP" io.bovel.vik.run "$f"
    measure vik-native "$NATIVE" "$f"
    measure vik-graal "$GRAAL" "$f"
    measure vik-jvm-aot java -XX:AOTCache="$AOT" -cp "$AOT_CP" io.bovel.vik.run "$f"
    measure vik-node node "$NODE_RUNNER" "$f"
    measure z3 z3 "$f"
    measure cvc5 cvc5 $inc "$f"
    measure princess java -cp "$CP" ap.CmdlMain $pinc "$f"
    measure smtinterpol java -cp "$CP" de.uni_freiburg.informatik.ultimate.smtinterpol.Main -q "$f"
  } | while read -r line; do printf "%s\t%s\t%s\n" "$base" "$expected" "$line"; done
done < "$INSTANCES"
