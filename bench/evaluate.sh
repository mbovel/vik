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
MAIN="keymaera__division_dijkstra-node701.smt2,Chua__1__VC2__L__Chua-1-VC2-L-chunk-0005.smt2,RandomDecoupled__pb_real_30_60_45_28.smt2,QG-classification__qg5__iso_brn001.smt2,QG-classification__qg5__iso_icl662.smt2,2018-Goel-hwbench__QF_UF_bug-1_ab_reg_max.smt2,20170829-Rodin__smt3534838651727683253.smt2,20190906-CLEARSY__0016__00003.smt2,TypeSafe__z3.1184131.smt2,CAV_2009_benchmarks__20-vars__problem__001.smt2,cut_lemmas__10-vars__cut_lemma_01_001.smt2,dillig__20-4.smt2,mathsat__FISCHER1-1-fair.smt2,convert__convert-tiff2jpg-query-1831.smt2,prime-cone__prime_cone_unsat_3.smt2,sage__app7__bench_8505.smt2,bmc-bv__adpcm.smt2,20172804-Barrett__tests__v1__v1l20086.cvc.smt2,20172804-Barrett__tests__v10__v10l20080.cvc.smt2,20230720-blocksworld__from_0_1_3_to_1_1_2_bmc_0.smt2,TwoSquares__smtlib.598294.smt2,mathsat__Wisa__xs-09-05-2-4-4-5.smt2,wisas__xs_8_8.smt2"
INC="20190906-CLEARSY__0020__00009.smt2,kind__MESI_i4.ec.smt2,2018-Goel-hwbench__QF_BV_cav14_example_cc_ref_max.smt2"
INC_SLOW="lustre__two_counters.bmc_k100.smt2,lustre__ex3.bmc_k100.smt2"  # VIK: tens of seconds per op
NEQ="NEQ__NEQ004_size4.smt2"
HOF="regress0__define-fun-model.smt2,regressions__3775.smt2"  # higher-order: only the solvers with lambdas
ARR="storeinv__storeinv_t2_np_nf_ai_00001_001.cvc.smt2,swap__swap_invalid_t3_np_nf_ai_00002_009.cvc.smt2,check__array_incompleteness1.smt2"  # arrays: the replays lack the store axioms

# Blanked cells: the API columns on the bit-vector instances (the tree is
# already lowered: not the same question), and princess where it needs minutes.
BLANK_WARM="z3Direct:sage__app7__bench_8505,z3Direct:bmc-bv__adpcm,smtinterpol:sage__app7__bench_8505,smtinterpol:bmc-bv__adpcm,princess:sage__app7__bench_8505,princess:bmc-bv__adpcm,princess:NEQ__NEQ004_size4,z3Direct:2018-Goel-hwbench__QF_BV_cav14_example_cc_ref_max,princess:2018-Goel-hwbench__QF_BV_cav14_example_cc_ref_max,smtinterpol:2018-Goel-hwbench__QF_BV_cav14_example_cc_ref_max"
BLANK_COLD="smtinterpol:sage__app7__bench_8505,smtinterpol:bmc-bv__adpcm,smtinterpol:pspace__ndist.a.20000"

# Each group is one `bench/Jmh/run` segment; warm() chains all segments into a single sbt
# invocation — one client, one server session. Reattaching a fresh client per group wedged twice
# on 2026-09-01: the server accepted the command and never launched the fork. The CI bench job's
# chained shape never wedged.
# -foe: any unrecoverable benchmark error fails the run — combos that cannot run are excluded by
# the groups, never tolerated as failures. A segment's fork flags stand alone (JMH refuses
# duplicate options, so defaults and overrides must never be concatenated).
seg() { # seg <rff> <files> <methods regex> [fork flags — replace the defaults entirely]
  local rff=$1 files=$2 methods=$3; shift 3
  local forks="$*"; [ -z "$forks" ] && forks="-f 2 -wi 3 -i 3"
  printf '%s' "bench/Jmh/run -foe true $forks -rf json -rff $rff -p file=$files .*OtherSolversBench.$methods\$; "
}

warm() {
  local chain=""
  chain+=$(seg "$OUT-warm.json"          "$MAIN"           "(vik|vikText|z3|z3Direct|cvc5|smtinterpol)")
  chain+=$(seg "$OUT-warm-p.json"        "$MAIN"           "princess" -f 1 -wi 1 -i 3)
  chain+=$(seg "$OUT-warm-inc.json"      "$INC"            "(vik|vikText|z3Direct|cvc5|smtinterpol)")
  chain+=$(seg "$OUT-warm-inc-s.json"    "$INC_SLOW"       "(z3Direct|cvc5|smtinterpol)")
  chain+=$(seg "$OUT-warm-inc-slow.json" "$INC_SLOW"       "(vik|vikText)" -f 1 -wi 1 -i 2)
  chain+=$(seg "$OUT-warm-inc-p.json"    "$INC,$INC_SLOW"  "princess" -f 1 -wi 1 -i 2)
  chain+=$(seg "$OUT-warm-neq.json"      "$NEQ"            "(vik|vikText|z3|z3Direct|cvc5|smtinterpol)")
  # cvc5's parser rejects regressions__3775 (z3-dialect lambdas), so it gets only the file it speaks.
  chain+=$(seg "$OUT-warm-ho.json"       "$HOF"            "(vik|vikText)")
  chain+=$(seg "$OUT-warm-ho-c.json"     "regress0__define-fun-model.smt2" "cvc5")
  chain+=$(seg "$OUT-warm-arr.json"      "$ARR"            "(vik|vikText|z3|cvc5)")
  # The ceiling catches a wedged run; -k because an sbt client can ignore TERM while its fork lives.
  timeout -k 30 14400 sbt "${chain%; }"
}

cold() { bench/cold.sh > "$OUT-cold.tsv"; }

regen() {
  # Per group, the newest dated file: a partial re-measurement updates its groups and leaves the
  # others at their last measured values, and the next full run heals everything to one date.
  local jsons=() suffix f
  for suffix in warm warm-p warm-inc warm-inc-s warm-inc-slow warm-inc-p warm-neq warm-ho warm-ho-c warm-arr; do
    f=$(ls -t $R/*-$suffix.json 2>/dev/null | head -1)
    [ -n "$f" ] && jsons+=("$f")
  done
  local cold_tsv; cold_tsv=$(ls -t $R/*-cold.tsv | head -1)
  echo "regen from: ${jsons[*]} $cold_tsv"
  local tmp; tmp=$(mktemp -d)
  bench/.venv/bin/python bench/warm2md.py "${jsons[@]}" --blank "$BLANK_WARM" > "$tmp/warm.md"
  bench/.venv/bin/python bench/cold2md.py "$cold_tsv" --blank "$BLANK_COLD" > "$tmp/cold.md"
  python3 bench/splice.py "$tmp/warm.md" "$tmp/cold.md"
  # The raw-data links of the page name the files the tables were built from.
  sed -i -E "s|results/smt/[0-9-]+-laraserver4-cold.tsv|results/smt/$(basename "$cold_tsv")|" docs/_docs/evaluation.md
  sed -i -E "s|results/smt/[0-9-]+-laraserver4-warm\.json|results/smt/$(basename "${jsons[0]}")|" docs/_docs/evaluation.md
  sed -i -E "s|results/smt/[0-9-]+-laraserver4-warm-inc\.json|results/smt/$(basename "$(ls -t $R/*-warm-inc.json | head -1)")|" docs/_docs/evaluation.md
  sed -i -E "s|results/smt/[0-9-]+-laraserver4-warm-neq\.json|results/smt/$(basename "$(ls -t $R/*-warm-neq.json | head -1)")|" docs/_docs/evaluation.md
  rm -r "$tmp"
}

case "${1:-}" in
  warm) preflight; warm; regen ;;
  cold) preflight; preflight_cold; cold; regen ;;
  regen) preflight; regen ;;
  all) preflight; preflight_cold; warm; cold; regen ;;
  *) echo "usage: bench/evaluate.sh warm|cold|regen|all"; exit 2 ;;
esac
