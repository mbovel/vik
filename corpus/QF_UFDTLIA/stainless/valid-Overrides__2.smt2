; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/Overrides.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!13 () Bool)
(assert start!13)
(assert (=> start!13 false))
(assert (=> start!13 true))
(declare-datatypes ((B!5 0))(
  ( (C!4 (c!13 Int)) (D!4) )
))
(declare-fun thiss!6 () B!5)
(declare-fun inv!1 (B!5) Bool)
(assert (=> start!13 (inv!1 thiss!6)))
(declare-fun bs!4 () Bool)
(assert (= bs!4 start!13))
(declare-fun m!17 () Bool)
(assert (=> bs!4 m!17))
(set-info :status unsat)
(check-sat-assuming ((not start!13)))
(set-info :status unsat)
(check-sat)
