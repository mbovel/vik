; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/Overrides.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!15 () Bool)
(assert start!15)
(declare-fun a!1 () Int)
(declare-datatypes ((B!6 0))(
  ( (C!5 (c!14 Int)) (D!5) )
))
(declare-fun thiss!6 () B!6)
(get-info :version)
(assert (=> start!15 (and (> a!1 0) ((_ is C!5) thiss!6) (< a!1 0))))
(assert (=> start!15 true))
(declare-fun inv!1 (B!6) Bool)
(assert (=> start!15 (inv!1 thiss!6)))
(declare-fun bs!6 () Bool)
(assert (= bs!6 start!15))
(declare-fun m!19 () Bool)
(assert (=> bs!6 m!19))
(set-info :status unsat)
(check-sat-assuming ((not start!15)))
(set-info :status unsat)
(check-sat)
