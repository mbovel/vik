; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/Overrides.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFBVDTLIA)
(declare-fun start!9 () Bool)
(assert start!9)
(declare-datatypes ((A!12 0))(
  ( (C!3 (c!23 (_ BitVec 32))) (BExt!2 (__x!14 Int)) (AExt!2 (__x!15 Int)) )
))
(declare-fun thiss!12 () A!12)
(declare-fun a!10 () (_ BitVec 32))
(get-info :version)
(assert (=> start!9 (and (or ((_ is C!3) thiss!12) ((_ is BExt!2) thiss!12)) (bvsle a!10 #b00000000000000000000000000000000))))
(assert (=> start!9 true))
(set-info :status sat)
(check-sat)
(get-model)
