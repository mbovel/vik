; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/BigIntMonoidLaws.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!49 () Bool)
(assert start!49)
(assert (=> start!49 false))
(declare-datatypes ((Monoid!6 0))(
  ( (MonoidExt!5 (__x!7 Int)) (WrongProductMonoid!6) (WrongAdditiveMonoid!6) )
))
(declare-fun lt!8 () Monoid!6)
(declare-fun thiss!93 () Monoid!6)
(assert (=> start!49 (= lt!8 thiss!93)))
(get-info :version)
(assert (=> start!49 ((_ is MonoidExt!5) lt!8)))
(set-info :status unsat)
(check-sat)
