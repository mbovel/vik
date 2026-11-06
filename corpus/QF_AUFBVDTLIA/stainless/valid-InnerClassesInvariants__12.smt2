; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/InnerClassesInvariants.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_AUFBVDTLIA)
(declare-fun start!43 () Bool)
(assert start!43)
(declare-fun x!66 () (_ BitVec 32))
(declare-datatypes ((array!5 0))(
  ( (array!6 (arr!8 (Array (_ BitVec 32) Int)) (size!2 (_ BitVec 32))) )
))
(declare-fun arr!4 () array!5)
(assert (=> start!43 (and (bvslt #b00000000000000000000000000000000 x!66) (bvslt x!66 (size!2 arr!4)) (bvslt x!66 #b00000000000000000000000000000000))))
(assert (=> start!43 true))
(declare-fun array_inv!2 (array!5) Bool)
(assert (=> start!43 (array_inv!2 arr!4)))
(declare-fun bs!24 () Bool)
(assert (= bs!24 start!43))
(declare-fun m!35 () Bool)
(assert (=> bs!24 m!35))
(set-info :status unsat)
(check-sat-assuming ((not start!43)))
(set-info :status unsat)
(check-sat)
