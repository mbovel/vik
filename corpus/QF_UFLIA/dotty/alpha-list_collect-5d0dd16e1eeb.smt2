; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `list_collect.scala`
; original path: tests/*-custom-args/qualified-types/list_collect.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; logic: QF_UFLIA, narrowed from QF_UFDTLIA, which claims more than the script uses (prune logic)
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFLIA)
(declare-sort scala.Int 0)
(declare-fun x3 () scala.Int)
(declare-fun Any.$isInstanceOf$@scala.Int_Int_with_boundArg_0_____0 (scala.Int) Bool)
(declare-fun x0 () Int)
(assert (Any.$isInstanceOf$@scala.Int_Int_with_boundArg_0_____0 x3))
(assert (<= 0 x0))
(assert (not (<= 0 x0)))
(check-sat)
