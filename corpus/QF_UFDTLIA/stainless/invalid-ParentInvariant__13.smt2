; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/ParentInvariant.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!85 () Bool)
(assert start!85)
(assert (=> start!85 false))
(declare-datatypes ((Foo!15 0))(
  ( (FooExt!14 (__x!16 Int)) (Bar!15 (y!38 Int)) (Baz!15 (y!39 Int)) )
))
(declare-fun lt!14 () Foo!15)
(declare-fun thiss!75 () Foo!15)
(assert (=> start!85 (= lt!14 thiss!75)))
(declare-fun inv!7 (Foo!15) Bool)
(get-info :version)
(assert (=> start!85 (and (inv!7 thiss!75) ((_ is Baz!15) lt!14))))
(declare-fun bs!26 () Bool)
(assert (= bs!26 start!85))
(declare-fun m!107 () Bool)
(assert (=> bs!26 m!107))
(set-info :status unsat)
(check-sat-assuming ((not start!85)))
(set-info :status unsat)
(check-sat)
