; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/InnerClasses0.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!17 () Bool)
(assert start!17)
(assert (=> start!17 false))
(declare-datatypes ((Test!6 0))(
  ( (FooBarBaz!8 (a!11 Int) (l!13 Int)) (TestExt!5 (__x!7 Int)) )
))
(declare-fun lt!2 () Test!6)
(declare-fun thiss!9 () Test!6)
(assert (=> start!17 (= lt!2 thiss!9)))
(declare-fun inv!1 (Test!6) Bool)
(get-info :version)
(assert (=> start!17 (and (inv!1 thiss!9) ((_ is FooBarBaz!8) lt!2))))
(declare-fun bs!11 () Bool)
(assert (= bs!11 start!17))
(declare-fun m!15 () Bool)
(assert (=> bs!11 m!15))
(set-info :status unsat)
(check-sat-assuming ((not start!17)))
(set-info :status unsat)
(check-sat)
