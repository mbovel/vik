#!/usr/bin/env bash
# A JDK 25 AOT cache for the solver's `smt2` (JEP 514: one training run records the loaded and linked
# classes, the profiles, and writes the cache): bench/aot.sh [instances-file], from the repository root,
# trains on the given instances (default: the evaluation's, one process solving them all) and writes
# target/aot/vik.aot with the classpath it was trained on (target/aot/classpath.txt: the cache is only
# valid for that classpath, and a dirty build renames the jars). cold.sh then measures `vik-jvm-aot`.
set -eu
cd "$(dirname "$0")/.."
INSTANCES="${1:-bench/evaluation-instances.txt}"
java -XX:+PrintFlagsFinal -version 2>/dev/null | grep -q AOTCacheOutput ||
  { echo "aot.sh: this java has no AOTCacheOutput (JDK 25 or later is needed)" >&2; exit 1; }
CP=$(sbt --server --no-colors "vik/package; print vik/runtimeClasspath" | grep -E "^/" | tail -1)
[ -n "$CP" ] || { echo "aot.sh: no classpath from sbt" >&2; exit 1; }
mkdir -p target/aot
files=$(grep -o '^corpus/[^ ]*' "$INSTANCES" | tr '\n' ' ')
# shellcheck disable=SC2086
java -XX:AOTCacheOutput=target/aot/vik.aot -cp "$CP" io.bovel.vik.smt2 $files > /dev/null
echo "$CP" > target/aot/classpath.txt
echo "trained target/aot/vik.aot ($(du -h target/aot/vik.aot | cut -f1)) on $(wc -w <<< "$files") instances"
