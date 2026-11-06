; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/InnerClassesInvariants.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_AUFBVDTLIA)
(declare-fun start!1 () Bool)
(assert start!1)
(declare-datatypes ((array!3 0))(
  ( (array!4 (arr!6 (Array (_ BitVec 32) Int)) (size!1 (_ BitVec 32))) )
))
(declare-datatypes ((Local!44 0))(
  ( (Local!45 (arr!7 array!3) (x!67 (_ BitVec 32))) )
))
(declare-fun thiss!42 () Local!44)
(assert (=> start!1 (and (bvslt #b00000000000000000000000000000000 (x!67 thiss!42)) (bvslt (x!67 thiss!42) (size!1 (arr!7 thiss!42))) (bvslt (x!67 thiss!42) #b00000000000000000000000000000000))))
(declare-fun e!4 () Bool)
(declare-fun inv!7 (Local!44) Bool)
(assert (=> start!1 (and (inv!7 thiss!42) e!4)))
(declare-fun b!2 () Bool)
(declare-fun array_inv!1 (array!3) Bool)
(assert (=> b!2 (= e!4 (array_inv!1 (arr!7 thiss!42)))))
(assert (= start!1 b!2))
(declare-fun m!1 () Bool)
(assert (=> start!1 m!1))
(declare-fun m!3 () Bool)
(assert (=> b!2 m!3))
(set-info :status unsat)
(check-sat-assuming ((not start!1) (not b!2)))
(set-info :status unsat)
(check-sat)
