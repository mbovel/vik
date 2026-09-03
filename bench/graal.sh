#!/usr/bin/env bash
# The solver's `smt2` as a GraalVM native image: bench/graal.sh, from the repository root, writes
# target/graal/vik — a binary that starts in a few milliseconds where the JVM takes a third of a second
# (cold.sh measures it as `vik-graal` when it exists). Needs `native-image` on the PATH or a GraalVM at
# $GRAALVM_HOME (`cs java-home --jvm graalvm-community:25` gives one); the build takes under a minute.
set -eu
cd "$(dirname "$0")/.."
NATIVE_IMAGE=$(command -v native-image || echo "${GRAALVM_HOME:-}/bin/native-image")
[ -x "$NATIVE_IMAGE" ] || { echo "graal.sh: no native-image on the PATH and no GRAALVM_HOME" >&2; exit 1; }
CP=$(sbt --server --no-colors "vik/package; print vik/runtimeClasspath" | grep -E "^/" | tail -1)
[ -n "$CP" ] || { echo "graal.sh: no classpath from sbt" >&2; exit 1; }
mkdir -p target/graal
# --no-fallback: a binary, or a failure — never a launcher that needs a JVM after all.
"$NATIVE_IMAGE" --no-fallback -O2 -cp "$CP" net.bovel.vik.smt2 -o target/graal/vik
target/graal/vik /dev/null > /dev/null
echo "built target/graal/vik ($(du -h target/graal/vik | cut -f1))"
