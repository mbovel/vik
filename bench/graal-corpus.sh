#!/usr/bin/env bash
# The corpus on the GraalVM native image: every file the corpus test holds VIK to (the ones off
# corpus/ignore.txt), answered by target/graal/vik and compared to its statuses — the image runs the
# JVM's code without the JIT, so this is the test that it is the same solver. bench/graal-corpus.sh
# [seconds per file, default 20] [parallel runs, default 8]; exits 1 on any wrong answer or timeout, listing them.
set -u
cd "$(dirname "$0")/.."
SECONDS_PER_FILE="${1:-20}"
PARALLEL="${2:-8}"
[ -x target/graal/vik ] || { echo "graal-corpus.sh: no target/graal/vik (bench/graal.sh builds it)" >&2; exit 1; }
ignored=$(mktemp); grep -v '^#' corpus/ignore.txt | cut -f1 | sed 's|^|corpus/|' | sort > "$ignored"
files=$(mktemp); find corpus -name '*.smt2' -not -path '*/.import/*' | sort | comm -23 - "$ignored" > "$files"
check() { # one file: prints nothing when the answers are the statuses, a line otherwise
  local f=$1 expected got
  # The status of a check-sat: the last `(set-info :status ...)` before it (an origin's own may precede the
  # corpus's), as the corpus test reads them.
  expected=$(awk '/^\(set-info :status / { s = $3; sub(/\)/, "", s) }
                  /^\(check-sat/ { print (s == "" ? "unknown" : s); s = "" }' "$f" | paste -sd, -)
  got=$(timeout "$SECONDS_PER_FILE" target/graal/vik "$f" 2>/dev/null | grep -E '^(sat|unsat|unknown)$' | paste -sd, -)
  [ $? = 124 ] && got=timeout
  local i=0 e g; IFS=, read -ra es <<< "$expected"; IFS=, read -ra gs <<< "$got"
  if [ "$got" = timeout ] || [ "${#gs[@]}" != "${#es[@]}" ]; then echo "$f: expected $expected, got ${got:-nothing}"; return; fi
  for e in "${es[@]}"; do g=${gs[$i]}; i=$((i + 1))
    if [ "$e" != unknown ] && [ "$g" != "$e" ]; then echo "$f: expected $expected, got $got"; return; fi
  done
}
export -f check; export SECONDS_PER_FILE
failures=$(xargs -P "$PARALLEL" -I{} bash -c 'check {}' < "$files")
echo "$(wc -l < "$files") files on target/graal/vik, $(grep -c . <<< "$failures") not answered as their statuses"
[ -z "$failures" ] || { echo "$failures"; rm -f "$ignored" "$files"; exit 1; }
rm -f "$ignored" "$files"
