; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/Methods.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!27 () Bool)
(assert start!27)
(assert (=> start!27 false))
(declare-datatypes ((A!17 0))(
  ( (D!8) (C!8 (x!35 Int)) (E!8) )
))
(declare-fun thiss!11 () A!17)
(declare-fun inv!2 (A!17) Bool)
(assert (=> start!27 (inv!2 thiss!11)))
(declare-fun bs!13 () Bool)
(assert (= bs!13 start!27))
(declare-fun m!27 () Bool)
(assert (=> bs!13 m!27))
(set-info :status unsat)
(check-sat-assuming ((not start!27)))
(set-info :status unsat)
(check-sat)
