; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/Methods.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!25 () Bool)
(assert start!25)
(assert (=> start!25 false))
(declare-datatypes ((A!16 0))(
  ( (D!7) (C!7 (x!34 Int)) (E!7) )
))
(declare-fun lt!14 () A!16)
(declare-fun thiss!14 () A!16)
(assert (=> start!25 (= lt!14 thiss!14)))
(declare-fun inv!2 (A!16) Bool)
(get-info :version)
(assert (=> start!25 (and (inv!2 thiss!14) (or ((_ is C!7) lt!14) ((_ is E!7) lt!14)))))
(declare-fun bs!11 () Bool)
(assert (= bs!11 start!25))
(declare-fun m!25 () Bool)
(assert (=> bs!11 m!25))
(set-info :status unsat)
(check-sat-assuming ((not start!25)))
(set-info :status unsat)
(check-sat)
