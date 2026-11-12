; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/Laws1.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!27 () Bool)
(assert start!27)
(assert (=> start!27 false))
(declare-datatypes ((A!14 0))(
  ( (BExt!4 (__x!12 Int)) (C!5) (AExt!4 (__x!13 Int)) )
))
(declare-fun lt!11 () A!14)
(declare-fun thiss!34 () A!14)
(assert (=> start!27 (= lt!11 thiss!34)))
(get-info :version)
(assert (=> start!27 ((_ is AExt!4) lt!11)))
(set-info :status unsat)
(check-sat)
