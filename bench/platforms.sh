#!/usr/bin/env bash
# The solver on another platform against the corpus: every file of a list run through a runner (the
# Scala Native binary, `node bench/vik-node.mjs`, a GraalVM image), its answers compared with the file's
# `:status` stamps, the disagreements reported and the exit code set on any. The corpus test runs the
# JVM only; this is what says the other builds answer the same.
#
# Usage: bench/platforms.sh <files> <runner...>   e.g. bench/platforms.sh <(ls corpus/*/vik/*.smt2) node bench/vik-node.mjs
# `VIK_PLATFORM_SECONDS` per file (30 by default), `VIK_PLATFORM_JOBS` in parallel (8 by default).
set -u
LIST=$(mktemp); cat "$1" > "$LIST"; FILES=$LIST; shift # a copy: the list may be a pipe, read twice below
SECONDS_PER_FILE=${VIK_PLATFORM_SECONDS:-30}
JOBS=${VIK_PLATFORM_JOBS:-8}
export RUNNER="$*" SECONDS_PER_FILE
# The status of each check-sat as the corpus test reads it (Script.expectedStatuses): the last
# `set-info :status` before it, none for a check after one that had a stamp; `?` where there is none.
statuses() {
  grep -oE '\(set-info :status [a-z]+\)|\(check-sat' "$1" | awk '
    /set-info/ { status = $3; sub(/\)/, "", status); if (status != "sat" && status != "unsat") status = "?"; seen = 1; next }
    /check-sat/ { printf "%s%s", (n++ ? "," : ""), (status == "" ? "?" : status); if (seen) status = "" }
  '
}
one() { # one file: "ok", or the disagreement; an answer at a check without a stamp is not compared
  local f=$1 expected got code
  expected=$(statuses "$f")
  local out
  out=$(mktemp)
  timeout "$SECONDS_PER_FILE" $RUNNER "$f" > "$out" 2>/dev/null
  code=$?
  got=$(grep -E '^(sat|unsat|unknown)$' "$out" | paste -sd, -)
  rm -f "$out"
  [ "$code" = 124 ] && got=timeout
  [ "$code" != 0 ] && [ "$code" != 124 ] && [ -z "$got" ] && got="exit $code"
  local same=1 i e g
  IFS=, read -ra es <<< "$expected"; IFS=, read -ra gs <<< "$got"
  [ "${#es[@]}" = "${#gs[@]}" ] || same=0
  for i in "${!es[@]}"; do e=${es[$i]}; g=${gs[$i]:-}; [ "$e" = "?" ] || [ "$e" = "$g" ] || same=0; done
  if [ "$same" = 1 ]; then echo "ok $f"; else echo "DIFF $f: expected [$expected], got [$got]"; fi
}
export -f one statuses
DIFFS=$(xargs -a "$FILES" -P "$JOBS" -I{} bash -c 'one "$@"' _ {} | grep -v "^ok " | sort)
[ -n "$DIFFS" ] && echo "$DIFFS"
echo "$(wc -l < "$FILES") files run through: $RUNNER; $(echo -n "$DIFFS" | grep -c "^DIFF") disagree"
rm -f "$LIST"
[ -z "$DIFFS" ]
