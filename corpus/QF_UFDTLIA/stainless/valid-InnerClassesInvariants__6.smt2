; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/InnerClassesInvariants.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!21 () Bool)
(assert start!21)
(declare-fun lt!5 () Int)
(declare-datatypes ((Local!56 0))(
  ( (Local!57 (w!14 Int) (x!73 Int)) )
))
(declare-fun thiss!27 () Local!56)
(assert (=> start!21 (= lt!5 (+ (x!73 thiss!27) 10))))
(assert (=> start!21 (and (< 0 (x!73 thiss!27)) (< (x!73 thiss!27) 10) (or (>= 10 lt!5) (>= lt!5 20)))))
(declare-fun inv!8 (Local!56) Bool)
(assert (=> start!21 (inv!8 thiss!27)))
(declare-fun bs!12 () Bool)
(assert (= bs!12 start!21))
(declare-fun m!19 () Bool)
(assert (=> bs!12 m!19))
(set-info :status unsat)
(check-sat-assuming ((not start!21)))
(set-info :status unsat)
(check-sat)
