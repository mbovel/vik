; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/ParentInvariant.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!17 () Bool)
(assert start!17)
(assert (=> start!17 false))
(declare-datatypes ((Foo!4 0))(
  ( (FooExt!3 (__x!5 Int)) (Bar!4 (y!16 Int)) (Baz!4 (y!17 Int)) )
))
(declare-fun thiss!24 () Foo!4)
(declare-fun inv!7 (Foo!4) Bool)
(assert (=> start!17 (inv!7 thiss!24)))
(declare-fun bs!5 () Bool)
(assert (= bs!5 start!17))
(declare-fun m!13 () Bool)
(assert (=> bs!5 m!13))
(set-info :status unsat)
(check-sat-assuming ((not start!17)))
(set-info :status unsat)
(check-sat)
