; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/AdtSpecializationUnapply2.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!21 () Bool)
(assert start!21)
(assert (=> start!21 false))
(declare-datatypes ((Root!5 0))(
  ( (C!5 (x!92 Int)) (D!5 (x!93 Int)) )
))
(declare-fun lt!11 () Root!5)
(declare-fun thiss!5 () Root!5)
(assert (=> start!21 (= lt!11 thiss!5)))
(get-info :version)
(assert (=> start!21 (or ((_ is C!5) lt!11) ((_ is D!5) lt!11))))
(set-info :status unsat)
(check-sat)
