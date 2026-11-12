; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/DisrespectfulOverride.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!13 () Bool)
(assert start!13)
(assert (=> start!13 false))
(declare-datatypes ((A!13 0))(
  ( (AExt!3 (__x!5 Int)) (C!4) )
))
(declare-fun lt!5 () A!13)
(declare-fun thiss!4 () A!13)
(assert (=> start!13 (= lt!5 thiss!4)))
(get-info :version)
(assert (=> start!13 ((_ is AExt!3) lt!5)))
(set-info :status unsat)
(check-sat)
