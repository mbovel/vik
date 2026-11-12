; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/ParentInvariant.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!87 () Bool)
(assert start!87)
(assert (=> start!87 false))
(declare-datatypes ((Foo!16 0))(
  ( (FooExt!15 (__x!17 Int)) (Bar!16 (y!40 Int)) (Baz!16 (y!41 Int)) )
))
(declare-fun lt!17 () Foo!16)
(declare-fun thiss!43 () Foo!16)
(assert (=> start!87 (= lt!17 thiss!43)))
(declare-fun inv!7 (Foo!16) Bool)
(get-info :version)
(assert (=> start!87 (and (inv!7 thiss!43) ((_ is FooExt!15) lt!17))))
(declare-fun bs!28 () Bool)
(assert (= bs!28 start!87))
(declare-fun m!109 () Bool)
(assert (=> bs!28 m!109))
(set-info :status unsat)
(check-sat-assuming ((not start!87)))
(set-info :status unsat)
(check-sat)
