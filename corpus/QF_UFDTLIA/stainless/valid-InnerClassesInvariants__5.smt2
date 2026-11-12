; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/InnerClassesInvariants.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!19 () Bool)
(assert start!19)
(assert (=> start!19 false))
(declare-datatypes ((Local!54 0))(
  ( (Local!55 (w!13 Int) (x!72 Int)) )
))
(declare-fun thiss!27 () Local!54)
(declare-fun inv!8 (Local!54) Bool)
(assert (=> start!19 (inv!8 thiss!27)))
(declare-fun bs!10 () Bool)
(assert (= bs!10 start!19))
(declare-fun m!17 () Bool)
(assert (=> bs!10 m!17))
(set-info :status unsat)
(check-sat-assuming ((not start!19)))
(set-info :status unsat)
(check-sat)
