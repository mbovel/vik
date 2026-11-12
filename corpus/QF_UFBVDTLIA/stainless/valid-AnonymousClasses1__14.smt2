; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/AnonymousClasses1.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFBVDTLIA)
(declare-fun start!55 () Bool)
(assert start!55)
(assert (=> start!55 false))
(declare-datatypes ((Foo!7 0))(
  ( ($anon!6 (m!274 (_ BitVec 32)) (x!50 (_ BitVec 32))) (FooExt!6 (__x!10 Int)) )
))
(declare-fun lt!25 () Foo!7)
(declare-fun thiss!6 () Foo!7)
(assert (=> start!55 (= lt!25 thiss!6)))
(declare-fun inv!1 (Foo!7) Bool)
(get-info :version)
(assert (=> start!55 (and (inv!1 thiss!6) ((_ is $anon!6) lt!25))))
(declare-fun bs!21 () Bool)
(assert (= bs!21 start!55))
(declare-fun m!273 () Bool)
(assert (=> bs!21 m!273))
(set-info :status unsat)
(check-sat-assuming ((not start!55)))
(set-info :status unsat)
(check-sat)
