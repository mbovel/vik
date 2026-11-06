; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/ParentInvariant.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!67 () Bool)
(assert start!67)
(assert (=> start!67 false))
(declare-datatypes ((Foo!13 0))(
  ( (FooExt!12 (__x!14 Int)) (Bar!13 (y!34 Int)) (Baz!13 (y!35 Int)) )
))
(declare-fun lt!8 () Foo!13)
(declare-fun thiss!64 () Foo!13)
(assert (=> start!67 (= lt!8 thiss!64)))
(declare-fun inv!7 (Foo!13) Bool)
(get-info :version)
(assert (=> start!67 (and (inv!7 thiss!64) ((_ is Bar!13) lt!8))))
(declare-fun bs!22 () Bool)
(assert (= bs!22 start!67))
(declare-fun m!89 () Bool)
(assert (=> bs!22 m!89))
(set-info :status unsat)
(check-sat-assuming ((not start!67)))
(set-info :status unsat)
(check-sat)
