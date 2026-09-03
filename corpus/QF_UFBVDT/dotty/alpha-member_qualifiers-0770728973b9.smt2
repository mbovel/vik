; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `member_qualifiers.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/member_qualifiers.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFBVDT)
(declare-datatypes ((ParamsCase 0)) (((new_ParamsCase (ParamsCase.a@ParamsCase (_ BitVec 32)) (ParamsCase.b@ParamsCase (_ BitVec 32))))))
(declare-fun this_ParamsCase () ParamsCase)
(declare-fun x0 () (_ BitVec 32))
(assert (bvslt (ParamsCase.a@ParamsCase this_ParamsCase) (ParamsCase.b@ParamsCase this_ParamsCase)))
(assert true)
(assert (not (bvslt x0 (ParamsCase.b@ParamsCase this_ParamsCase))))
(check-sat)
