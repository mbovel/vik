; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/PartialSplit.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!170 () Bool)
(assert start!170)
(declare-datatypes ((P!6 0))(
  ( (Charlie!6) (Bob!6) (Alice!6) )
))
(declare-datatypes ((S!12 0))(
  ( (S!13 (who!8 P!6) (amount!8 Int)) )
))
(declare-fun _$2!10 () S!12)
(assert (=> start!170 (>= (amount!8 _$2!10) 0)))
(assert (=> start!170 true))
(set-info :status sat)
(check-sat)
(get-model)
