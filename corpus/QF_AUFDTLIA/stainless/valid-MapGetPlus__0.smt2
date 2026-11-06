; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/MapGetPlus.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_AUFDTLIA)
(declare-fun start!1 () Bool)
(assert start!1)
(declare-datatypes ((Option!6 0))(
  ( (None!5) (Some!5 (v!27 Int)) )
))
(declare-fun a!2 () (Array Int Option!6))
(get-info :version)
(assert (=> start!1 (and (= (select a!2 0) None!5) (not ((_ is Some!5) (select (store a!2 0 (Some!5 1)) 0))))))
(assert (=> start!1 true))
(declare-fun bs!1 () Bool)
(assert (= bs!1 start!1))
(declare-fun m!1 () Bool)
(assert (=> bs!1 m!1))
(declare-fun m!3 () Bool)
(assert (=> bs!1 m!3))
(declare-fun m!5 () Bool)
(assert (=> bs!1 m!5))
(set-info :status unsat)
(check-sat)
