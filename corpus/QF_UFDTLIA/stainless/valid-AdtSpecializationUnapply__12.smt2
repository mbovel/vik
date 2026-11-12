; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/AdtSpecializationUnapply.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!41 () Bool)
(assert start!41)
(assert (=> start!41 false))
(declare-datatypes ((A!21 0))(
  ( (E!6 (b!52 Bool)) (C!6 (x!87 Int)) (D!6 (x!88 Int)) )
))
(declare-fun lt!14 () A!21)
(declare-fun thiss!4 () A!21)
(assert (=> start!41 (= lt!14 thiss!4)))
(get-info :version)
(assert (=> start!41 (or ((_ is C!6) lt!14) ((_ is D!6) lt!14))))
(set-info :status unsat)
(check-sat)
