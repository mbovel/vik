#!/usr/bin/env bash
# Downloads what the other solvers need into bench/native/: the native libraries of the z3 and cvc5 Java
# bindings published by SoSy-Lab (the JavaSMT project) on Maven Central, for the in-process benchmarks
# (`Z3Bench`, `Z3TextBench`, `Cvc5Bench`), and the z3 and cvc5 binaries of the same
# versions from their GitHub releases into bench/native/bin, for the cold table (bench/cold.sh runs them from
# there). Linux x86-64 only; the Java parts of the bindings are ordinary dependencies of the bench project.
set -eu
cd "$(dirname "$0")"
Z3=4.14.0
CVC5=1.3.3
CVC5_BINDING=2026-02-26-d22638a # cvc5's main the day after the 1.3.3 tag; see build.sbt
BASE=https://repo1.maven.org/maven2/org/sosy-lab
STAMP="z3 $Z3, cvc5 $CVC5, cvc5 binding $CVC5_BINDING"
[ "$(cat native/VERSIONS 2>/dev/null)" = "$STAMP" ] || rm -rf native # a bump above refetches everything
mkdir -p native/bin
fetch() { # url, file
  [ -s "native/$2" ] && { echo "native/$2 present"; return; }
  echo "fetching $2"; curl -sSfL --retry 5 --retry-all-errors --retry-delay 15 --max-time 600 -o "native/$2" "$1"
}
fetch "$BASE/javasmt-solver-z3/$Z3/javasmt-solver-z3-$Z3-libz3-x64.so" libz3.so
fetch "$BASE/javasmt-solver-z3/$Z3/javasmt-solver-z3-$Z3-libz3java-x64.so" libz3java.so
fetch "$BASE/javasmt-solver-cvc5/$CVC5_BINDING/javasmt-solver-cvc5-$CVC5_BINDING-libcvc5jni-x64.so" libcvc5jni.so
binary() { # url, member, file: one executable out of a release archive
  [ -x "native/bin/$3" ] && { echo "native/bin/$3 present"; return; }
  echo "fetching $3"; curl -sSfL --retry 5 --retry-all-errors --retry-delay 15 --max-time 600 -o native/bin/archive.zip "$1"
  unzip -p native/bin/archive.zip "$2" > "native/bin/$3" && chmod +x "native/bin/$3"
  rm native/bin/archive.zip
}
binary "https://github.com/Z3Prover/z3/releases/download/z3-$Z3/z3-$Z3-x64-glibc-2.35.zip" "z3-$Z3-x64-glibc-2.35/bin/z3" z3
binary "https://github.com/cvc5/cvc5/releases/download/cvc5-$CVC5/cvc5-Linux-x86_64-static.zip" "cvc5-Linux-x86_64-static/bin/cvc5" cvc5
echo "$STAMP" > native/VERSIONS
ls -la native native/bin
