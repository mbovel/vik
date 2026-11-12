; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/InnerClasses1.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!21 () Bool)
(assert start!21)
(assert (=> start!21 false))
(declare-datatypes ((Test!8 0))(
  ( (FooBarBaz!10 (a!12 Int) (l!14 Int)) (TestExt!7 (__x!9 Int)) )
))
(declare-fun lt!8 () Test!8)
(declare-fun thiss!10 () Test!8)
(assert (=> start!21 (= lt!8 thiss!10)))
(declare-fun inv!1 (Test!8) Bool)
(get-info :version)
(assert (=> start!21 (and (inv!1 thiss!10) ((_ is TestExt!7) lt!8))))
(declare-fun bs!15 () Bool)
(assert (= bs!15 start!21))
(declare-fun m!19 () Bool)
(assert (=> bs!15 m!19))
(set-info :status unsat)
(check-sat-assuming ((not start!21)))
(set-info :status unsat)
(check-sat)
