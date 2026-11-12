; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/ADTWithArray1.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_AUFBVDT)
(declare-fun start!1 () Bool)
(assert start!1)
(declare-datatypes ((A!13 0))(
  ( (A!14 (x!19 (_ BitVec 32))) )
))
(declare-datatypes ((array!3 0))(
  ( (array!4 (arr!1 (Array (_ BitVec 32) A!13)) (size!1 (_ BitVec 32))) )
))
(declare-datatypes ((B!5 0))(
  ( (B!6 (content!5 array!3)) )
))
(declare-fun thiss!5 () B!5)
(assert (=> start!1 (and (bvsgt (size!1 (content!5 thiss!5)) #b00000000000000000000000000000000) (bvsge #b00000000000000000000000000000000 (size!1 (content!5 thiss!5))))))
(declare-fun e!4 () Bool)
(declare-fun inv!0 (B!5) Bool)
(assert (=> start!1 (and (inv!0 thiss!5) e!4)))
(declare-fun b!2 () Bool)
(declare-fun array_inv!1 (array!3) Bool)
(assert (=> b!2 (= e!4 (array_inv!1 (content!5 thiss!5)))))
(assert (= start!1 b!2))
(declare-fun m!1 () Bool)
(assert (=> start!1 m!1))
(declare-fun m!3 () Bool)
(assert (=> b!2 m!3))
(set-info :status unsat)
(check-sat-assuming ((not b!2) (not start!1)))
(set-info :status unsat)
(check-sat)
