; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/InnerClassesInvariants.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!9 () Bool)
(assert start!9)
(declare-fun lt!2 () Int)
(declare-datatypes ((Local!50 0))(
  ( (Local!51 (x!70 Int)) )
))
(declare-fun thiss!33 () Local!50)
(assert (=> start!9 (= lt!2 (+ (x!70 thiss!33) 10))))
(assert (=> start!9 (and (< 0 (x!70 thiss!33)) (< (x!70 thiss!33) 10) (or (>= 10 lt!2) (>= lt!2 20)))))
(declare-fun inv!4 (Local!50) Bool)
(assert (=> start!9 (inv!4 thiss!33)))
(declare-fun bs!6 () Bool)
(assert (= bs!6 start!9))
(declare-fun m!9 () Bool)
(assert (=> bs!6 m!9))
(set-info :status unsat)
(check-sat-assuming ((not start!9)))
(set-info :status unsat)
(check-sat)
