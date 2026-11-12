; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/InnerClasses2.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!27 () Bool)
(assert start!27)
(assert (=> start!27 false))
(declare-datatypes ((Test!6 0))(
  ( (FooBarBaz!14 (a!10 Bool) (b!16 Bool) (c!13 Bool) (l!25 Int) (m!43 Int) (o!16 Int)) (TestExt!5 (__x!7 Int)) )
))
(declare-fun lt!5 () Test!6)
(declare-fun thiss!13 () Test!6)
(assert (=> start!27 (= lt!5 thiss!13)))
(declare-fun inv!1 (Test!6) Bool)
(get-info :version)
(assert (=> start!27 (and (inv!1 thiss!13) ((_ is FooBarBaz!14) lt!5))))
(declare-fun bs!14 () Bool)
(assert (= bs!14 start!27))
(declare-fun m!42 () Bool)
(assert (=> bs!14 m!42))
(set-info :status unsat)
(check-sat-assuming ((not start!27)))
(set-info :status unsat)
(check-sat)
