; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/AnonymousClasses3.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!13 () Bool)
(assert start!13)
(assert (=> start!13 false))
(declare-datatypes ((MyClass!5 0))(
  ( (MyClassExt!4 (__x!6 Int)) ($anon!8) (normal!5) ($anon!9 (something!11 Int)) )
))
(declare-fun thiss!69 () MyClass!5)
(declare-fun inv!1 (MyClass!5) Bool)
(assert (=> start!13 (inv!1 thiss!69)))
(declare-fun bs!7 () Bool)
(assert (= bs!7 start!13))
(declare-fun m!13 () Bool)
(assert (=> bs!7 m!13))
(set-info :status unsat)
(check-sat-assuming ((not start!13)))
(set-info :status unsat)
(check-sat)
