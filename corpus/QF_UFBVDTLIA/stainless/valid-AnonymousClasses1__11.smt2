; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/AnonymousClasses1.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFBVDTLIA)
(declare-fun start!49 () Bool)
(assert start!49)
(assert (=> start!49 false))
(declare-datatypes ((Foo!4 0))(
  ( ($anon!3 (m!265 (_ BitVec 32)) (x!47 (_ BitVec 32))) (FooExt!3 (__x!7 Int)) )
))
(declare-fun thiss!10 () Foo!4)
(declare-fun inv!1 (Foo!4) Bool)
(assert (=> start!49 (inv!1 thiss!10)))
(declare-fun bs!15 () Bool)
(assert (= bs!15 start!49))
(declare-fun m!264 () Bool)
(assert (=> bs!15 m!264))
(set-info :status unsat)
(check-sat-assuming ((not start!49)))
(set-info :status unsat)
(check-sat)
