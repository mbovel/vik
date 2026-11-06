; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/Overrides.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!25 () Bool)
(assert start!25)
(declare-fun a!1 () Int)
(declare-datatypes ((B!8 0))(
  ( (C!7 (c!20 Int)) (D!7) )
))
(declare-fun thiss!6 () B!8)
(get-info :version)
(assert (=> start!25 false))
(assert (=> start!25 true))
(declare-fun inv!1 (B!8) Bool)
(assert (=> start!25 (inv!1 thiss!6)))
(declare-fun bs!9 () Bool)
(assert (= bs!9 start!25))
(declare-fun m!31 () Bool)
(assert (=> bs!9 m!31))
(set-info :status unsat)
(check-sat-assuming ((not start!25)))
(set-info :status unsat)
(check-sat)
