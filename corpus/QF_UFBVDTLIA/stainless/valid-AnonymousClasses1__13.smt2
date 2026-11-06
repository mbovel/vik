; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/AnonymousClasses1.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFBVDTLIA)
(declare-fun start!53 () Bool)
(assert start!53)
(assert (=> start!53 false))
(declare-datatypes ((Foo!6 0))(
  ( ($anon!5 (m!271 (_ BitVec 32)) (x!49 (_ BitVec 32))) (FooExt!5 (__x!9 Int)) )
))
(declare-fun lt!22 () Foo!6)
(declare-fun thiss!7 () Foo!6)
(assert (=> start!53 (= lt!22 thiss!7)))
(declare-fun inv!1 (Foo!6) Bool)
(get-info :version)
(assert (=> start!53 (and (inv!1 thiss!7) ((_ is FooExt!5) lt!22))))
(declare-fun bs!19 () Bool)
(assert (= bs!19 start!53))
(declare-fun m!270 () Bool)
(assert (=> bs!19 m!270))
(set-info :status unsat)
(check-sat-assuming ((not start!53)))
(set-info :status unsat)
(check-sat)
