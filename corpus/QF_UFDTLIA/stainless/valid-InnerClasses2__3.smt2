; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/InnerClasses2.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!21 () Bool)
(assert start!21)
(assert (=> start!21 false))
(declare-datatypes ((Test!3 0))(
  ( (FooBarBaz!11 (a!7 Bool) (b!13 Bool) (c!10 Bool) (l!22 Int) (m!34 Int) (o!13 Int)) (TestExt!2 (__x!4 Int)) )
))
(declare-fun thiss!17 () Test!3)
(declare-fun inv!1 (Test!3) Bool)
(assert (=> start!21 (inv!1 thiss!17)))
(declare-fun bs!8 () Bool)
(assert (= bs!8 start!21))
(declare-fun m!33 () Bool)
(assert (=> bs!8 m!33))
(set-info :status unsat)
(check-sat-assuming ((not start!21)))
(set-info :status unsat)
(check-sat)
