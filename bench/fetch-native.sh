#!/usr/bin/env bash
# Downloads the native libraries of the z3 and cvc5 Java bindings published by SoSy-Lab (the JavaSMT project) on
# Maven Central into bench/native/, for the in-process benchmarks (`OtherSolversBench.z3`, `z3Direct`, `cvc5`).
# Linux x86-64 only; the Java parts of the bindings are ordinary dependencies of the bench project.
set -eu
cd "$(dirname "$0")"
Z3=4.14.0
CVC5=1.2.1-g8594a8e4dc
BASE=https://repo1.maven.org/maven2/org/sosy-lab
mkdir -p native
fetch() { # url, file
  [ -s "native/$2" ] && { echo "native/$2 present"; return; }
  echo "fetching $2"; curl -sSfL --max-time 600 -o "native/$2" "$1"
}
fetch "$BASE/javasmt-solver-z3/$Z3/javasmt-solver-z3-$Z3-libz3-x64.so" libz3.so
fetch "$BASE/javasmt-solver-z3/$Z3/javasmt-solver-z3-$Z3-libz3java-x64.so" libz3java.so
fetch "$BASE/javasmt-solver-cvc5/$CVC5/javasmt-solver-cvc5-$CVC5-libcvc5jni-x64.so" libcvc5jni.so
ls -la native
