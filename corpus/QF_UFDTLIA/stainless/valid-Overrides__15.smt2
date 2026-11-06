; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/Overrides.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!61 () Bool)
(assert start!61)
(assert (=> start!61 false))
(declare-datatypes ((B!18 0))(
  ( (C!17 (c!39 Int)) (D!17) )
))
(declare-fun thiss!10 () B!18)
(declare-fun inv!1 (B!18) Bool)
(assert (=> start!61 (inv!1 thiss!10)))
(declare-fun bs!25 () Bool)
(assert (= bs!25 start!61))
(declare-fun m!77 () Bool)
(assert (=> bs!25 m!77))
(set-info :status unsat)
(check-sat-assuming ((not start!61)))
(set-info :status unsat)
(check-sat)
