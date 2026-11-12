; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/InnerClasses3.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!21 () Bool)
(assert start!21)
(assert (=> start!21 false))
(declare-datatypes ((Foo!7 0))(
  ( (BarExt!6 (__x!16 Int)) (Baz!9) (FooExt!6 (__x!17 Int)) )
))
(declare-fun lt!14 () Foo!7)
(declare-fun thiss!10 () Foo!7)
(assert (=> start!21 (= lt!14 thiss!10)))
(get-info :version)
(assert (=> start!21 ((_ is Baz!9) lt!14)))
(set-info :status unsat)
(check-sat)
