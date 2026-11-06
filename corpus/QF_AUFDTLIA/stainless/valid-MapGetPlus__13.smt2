; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/MapGetPlus.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_AUFDTLIA)
(declare-fun start!27 () Bool)
(assert start!27)
(declare-datatypes ((Option!19 0))(
  ( (None!18) (Some!18 (v!40 Int)) )
))
(declare-fun a!3 () (Array Int Option!19))
(assert (=> start!27 (and (= (select a!3 0) None!18) (not (= (v!40 (select (store a!3 0 (Some!18 1)) 0)) (v!40 (select (store a!3 0 (Some!18 1)) 0)))))))
(assert (=> start!27 true))
(declare-fun bs!27 () Bool)
(assert (= bs!27 start!27))
(declare-fun m!79 () Bool)
(assert (=> bs!27 m!79))
(declare-fun m!81 () Bool)
(assert (=> bs!27 m!81))
(declare-fun m!83 () Bool)
(assert (=> bs!27 m!83))
(set-info :status unsat)
(check-sat)
