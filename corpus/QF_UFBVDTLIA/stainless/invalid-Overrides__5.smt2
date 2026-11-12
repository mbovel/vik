; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/Overrides.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFBVDTLIA)
(declare-fun start!15 () Bool)
(assert start!15)
(declare-datatypes ((A!14 0))(
  ( (C!5 (c!25 (_ BitVec 32))) (BExt!4 (__x!18 Int)) (AExt!4 (__x!19 Int)) )
))
(declare-fun thiss!20 () A!14)
(declare-fun a!18 () (_ BitVec 32))
(get-info :version)
(assert (=> start!15 (and (or ((_ is C!5) thiss!20) ((_ is BExt!4) thiss!20)) (bvsgt a!18 #b00000000000000000000000000000000) ((_ is C!5) thiss!20) (bvslt a!18 #b00000000000000000000000000000000))))
(declare-fun lt!15 () A!14)
(assert (=> start!15 (= lt!15 thiss!20)))
(assert (=> start!15 (or ((_ is C!5) lt!15) ((_ is BExt!4) lt!15))))
(assert (=> start!15 true))
(set-info :status unsat)
(check-sat)
