#!/usr/bin/env bash
# The solver's `smt2` as a GraalVM native image: bench/graal.sh, from the repository root, is
# `sbt vik/nativeImage` — target/graal/vik, a binary that starts in a few milliseconds where the JVM takes a
# third of a second (cold.sh measures it as `vik-graal` when it exists). sbt-native-image fetches GraalVM
# (graalvm-java25) through coursier, unless NATIVE_IMAGE_INSTALLED=true and GRAALVM_HOME name one; the build
# takes under a minute.
set -eu
cd "$(dirname "$0")/.."
mkdir -p target/graal
sbt --server "vik/nativeImageCopy target/graal/vik"
target/graal/vik /dev/null > /dev/null
echo "built target/graal/vik ($(du -h target/graal/vik | cut -f1))"
