#!/bin/sh
# check-modules.sh DIR [JOBS]: checks an exported proof split into modules
# (`DIR/Data.lean`, `DIR/S_*.lean`, `DIR/Main.lean`), the slices JOBS at a
# time (2 by default; each can take several GB). The compiled modules go to
# `DIR/../.olean`.
set -e
dir=$(cd "$1" && pwd)
jobs=${2:-2}
root=$(dirname "$dir")
name=$(basename "$dir")
cd "$(dirname "$0")"
olean="$root/.olean"
mkdir -p "$olean/$name"
LEAN_PATH="$(lake env printenv LEAN_PATH):$olean"
export LEAN_PATH
lean --root="$root" -o "$olean/$name/Data.olean" "$dir/Data.lean"
ls "$dir" | grep '^S_.*\.lean$' | sed 's/\.lean$//' |
  xargs -P "$jobs" -I{} lean --root="$root" -o "$olean/$name/{}.olean" "$dir/{}.lean"
lean --root="$root" "$dir/Main.lean"
