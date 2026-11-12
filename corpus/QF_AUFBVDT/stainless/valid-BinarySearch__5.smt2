; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/BinarySearch.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_AUFBVDT)
(declare-fun start!11 () Bool)
(assert start!11)
(declare-fun lo!1 () (_ BitVec 32))
(declare-fun hi!1 () (_ BitVec 32))
(declare-datatypes ((array!13 0))(
  ( (array!14 (arr!14 (Array (_ BitVec 32) (_ BitVec 32))) (size!6 (_ BitVec 32))) )
))
(declare-fun arr!1 () array!13)
(assert (=> start!11 (and (bvsle #b00000000000000000000000000000000 lo!1) (bvslt hi!1 (size!6 arr!1)) (bvsle lo!1 (bvadd hi!1 #b00000000000000000000000000000001)) (bvslt (bvadd (bvsub hi!1 lo!1) #b00000000000000000000000000000001) #b00000000000000000000000000000000))))
(assert (=> start!11 true))
(declare-fun array_inv!6 (array!13) Bool)
(assert (=> start!11 (array_inv!6 arr!1)))
(declare-fun bs!11 () Bool)
(assert (= bs!11 start!11))
(declare-fun m!15 () Bool)
(assert (=> bs!11 m!15))
(set-info :status unsat)
(check-sat-assuming ((not start!11)))
(set-info :status unsat)
(check-sat)
