; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/InnerClassesInvariants.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!7 () Bool)
(assert start!7)
(assert (=> start!7 false))
(declare-datatypes ((Local!48 0))(
  ( (Local!49 (x!69 Int)) )
))
(declare-fun thiss!33 () Local!48)
(declare-fun inv!4 (Local!48) Bool)
(assert (=> start!7 (inv!4 thiss!33)))
(declare-fun bs!4 () Bool)
(assert (= bs!4 start!7))
(declare-fun m!7 () Bool)
(assert (=> bs!4 m!7))
(set-info :status unsat)
(check-sat-assuming ((not start!7)))
(set-info :status unsat)
(check-sat)
