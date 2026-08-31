#!/bin/bash
# The whole evaluation, one command: measures the warm and cold tables and
# regenerates docs/_docs/evaluation.md. Does not commit.
#
#   bench/evaluate.sh all     # warm + cold + regen (hours: princess pays 96 s
#                             # per MESI operation, VIK tens of seconds per
#                             # lustre script)
#   bench/evaluate.sh warm    # the three warm JMH groups + regen
#   bench/evaluate.sh cold    # the cold binaries run + regen
#   bench/evaluate.sh regen   # tables from the dated results already on disk
#
# The instance groups, the princess-separated runs (its slow instances get a
# single fork so a wedged run cannot eat the budget) and the blanked cells
# live here, as the one source of truth.
set -euo pipefail
cd "$(dirname "$0")/.."

preflight() {
  [ -x bench/.venv/bin/python ] ||
    { echo "missing bench/.venv — run: python3 -m venv bench/.venv && bench/.venv/bin/pip install -r bench/requirements.txt"; exit 1; }
  [ -s bench/native/libz3.so ] ||
    { echo "missing native libraries — run: bench/fetch-native.sh"; exit 1; }
}
preflight_cold() {
  command -v z3 >/dev/null && command -v cvc5 >/dev/null ||
    { echo "z3 and cvc5 must be on the PATH for the cold table"; exit 1; }
}
R=bench/results/smt
DATE=$(date +%F)
HOST=$(hostname -s)
OUT=$R/$DATE-$HOST

# The warm instances by group; the incremental and NEQ files are separate JMH
# runs so their results land in their own files, as the tables expect.
MAIN="QG-classification__qg5__iso_brn001.smt2,QG-classification__qg5__iso_icl662.smt2,2018-Goel-hwbench__QF_UF_bug-1_ab_reg_max.smt2,20170829-Rodin__smt3534838651727683253.smt2,20190906-CLEARSY__0016__00003.smt2,TypeSafe__z3.1184131.smt2,CAV_2009_benchmarks__20-vars__problem__001.smt2,cut_lemmas__10-vars__cut_lemma_01_001.smt2,dillig__20-4.smt2,mathsat__FISCHER1-1-fair.smt2,convert__convert-tiff2jpg-query-1831.smt2,prime-cone__prime_cone_unsat_3.smt2,sage__app7__bench_8505.smt2,bmc-bv__adpcm.smt2,20172804-Barrett__tests__v1__v1l20086.cvc.smt2,20172804-Barrett__tests__v10__v10l20080.cvc.smt2,20230720-blocksworld__from_0_1_3_to_1_1_2_bmc_0.smt2,TwoSquares__smtlib.598294.smt2,mathsat__Wisa__xs-09-05-2-4-4-5.smt2,wisas__xs_8_8.smt2"
INC="20190906-CLEARSY__0020__00009.smt2,kind__MESI_i4.ec.smt2,2018-Goel-hwbench__QF_BV_cav14_example_cc_ref_max.smt2"
INC_SLOW="lustre__two_counters.bmc_k100.smt2,lustre__ex3.bmc_k100.smt2"  # VIK: tens of seconds per op
NEQ="NEQ__NEQ004_size4.smt2"

# Blanked cells: the API columns on the bit-vector instances (the tree is
# already lowered: not the same question), and princess where it needs minutes.
BLANK_WARM="z3Direct:sage__app7__bench_8505,z3Direct:bmc-bv__adpcm,smtinterpol:sage__app7__bench_8505,smtinterpol:bmc-bv__adpcm,princess:sage__app7__bench_8505,princess:bmc-bv__adpcm,princess:NEQ__NEQ004_size4,z3Direct:2018-Goel-hwbench__QF_BV_cav14_example_cc_ref_max,princess:2018-Goel-hwbench__QF_BV_cav14_example_cc_ref_max,smtinterpol:2018-Goel-hwbench__QF_BV_cav14_example_cc_ref_max"
BLANK_COLD="smtinterpol:sage__app7__bench_8505,smtinterpol:bmc-bv__adpcm,smtinterpol:pspace__ndist.a.20000"

jmh() { # jmh <rff> <files> <methods regex> [extra jmh args]
  local rff=$1 files=$2 methods=$3; shift 3
  # The ceiling catches a wedged run; -k because an sbt client can ignore TERM while its fork lives.
  timeout -k 30 5400 sbt "bench/Jmh/run -f 2 -wi 3 -i 3 $* -rf json -rff $rff -p file=$files .*OtherSolversBench.$methods\$"
}

warm() {
  jmh "$OUT-warm.json"          "$MAIN"           "(vik|vikText|z3|z3Direct|cvc5|smtinterpol)"
  jmh "$OUT-warm-p.json"        "$MAIN"           "princess" -f 1 -wi 1 -i 3
  jmh "$OUT-warm-inc.json"      "$INC"            "(vik|vikText|z3Direct|cvc5|smtinterpol)"
  jmh "$OUT-warm-inc-s.json"    "$INC_SLOW"       "(z3Direct|cvc5|smtinterpol)"
  jmh "$OUT-warm-inc-slow.json" "$INC_SLOW"       "(vik|vikText)" -f 1 -wi 1 -i 2
  jmh "$OUT-warm-inc-p.json"    "$INC,$INC_SLOW"  "princess" -f 1 -wi 1 -i 2
  jmh "$OUT-warm-neq.json"      "$NEQ"            "(vik|vikText|z3|z3Direct|cvc5|smtinterpol)"
  python3 - "$OUT" <<'PY'
import json, sys, pathlib
out = sys.argv[1]
def cat(dst, *srcs):
    rows = []
    for s in srcs:
        p = pathlib.Path(f"{out}-{s}.json")
        if p.exists(): rows += json.load(open(p)); p.unlink()
    json.dump(rows, open(f"{out}-{dst}.json", "w"), indent=1)
cat("warm", "warm", "warm-p")
cat("warm-inc", "warm-inc", "warm-inc-s", "warm-inc-slow", "warm-inc-p")
PY
}

cold() { bench/cold.sh > "$OUT-cold.tsv"; }

regen() {
  local warm_json inc_json neq_json cold_tsv
  warm_json=$(ls -t $R/*-warm.json | head -1)
  inc_json=$(ls -t $R/*-warm-inc.json | head -1)
  neq_json=$(ls -t $R/*-warm-neq.json | head -1)
  cold_tsv=$(ls -t $R/*-cold.tsv | head -1)
  echo "regen from: $warm_json $inc_json $neq_json $cold_tsv"
  local tmp; tmp=$(mktemp -d)
  bench/.venv/bin/python bench/warm2md.py "$warm_json" "$inc_json" "$neq_json" --blank "$BLANK_WARM" > "$tmp/warm.md"
  bench/.venv/bin/python bench/cold2md.py "$cold_tsv" --blank "$BLANK_COLD" > "$tmp/cold.md"
  python3 bench/splice.py "$tmp/warm.md" "$tmp/cold.md"
  rm -r "$tmp"
}

case "${1:-}" in
  warm) preflight; warm; regen ;;
  cold) preflight; preflight_cold; cold; regen ;;
  regen) preflight; regen ;;
  all) preflight; preflight_cold; warm; cold; regen ;;
  *) echo "usage: bench/evaluate.sh warm|cold|regen|all"; exit 2 ;;
esac
