; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/PartialSplit.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!168 () Bool)
(assert start!168)
(declare-datatypes ((P!5 0))(
  ( (Charlie!5) (Bob!5) (Alice!5) )
))
(declare-datatypes ((S!10 0))(
  ( (S!11 (who!7 P!5) (amount!7 Int)) )
))
(declare-fun _$2!10 () S!10)
(assert (=> start!168 (< (amount!7 _$2!10) 0)))
(assert (=> start!168 true))
(set-info :status sat)
(check-sat)
(get-model)
