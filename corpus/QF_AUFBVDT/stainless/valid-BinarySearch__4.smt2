; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/BinarySearch.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_AUFBVDT)
(declare-fun start!9 () Bool)
(assert start!9)
(declare-fun lo!1 () (_ BitVec 32))
(declare-fun hi!1 () (_ BitVec 32))
(declare-datatypes ((array!11 0))(
  ( (array!12 (arr!13 (Array (_ BitVec 32) (_ BitVec 32))) (size!5 (_ BitVec 32))) )
))
(declare-fun arr!1 () array!11)
(assert (=> start!9 (and (bvsle #b00000000000000000000000000000000 lo!1) (bvslt hi!1 (size!5 arr!1)) (bvsle lo!1 (bvadd hi!1 #b00000000000000000000000000000001)) (let ((lhs!1 (bvsub hi!1 lo!1))) (and (= (bvand lhs!1 #b10000000000000000000000000000000) #b00000000000000000000000000000000) (not (= (bvand lhs!1 #b10000000000000000000000000000000) (bvand (bvadd lhs!1 #b00000000000000000000000000000001) #b10000000000000000000000000000000))))))))
(assert (=> start!9 true))
(declare-fun array_inv!5 (array!11) Bool)
(assert (=> start!9 (array_inv!5 arr!1)))
(declare-fun bs!9 () Bool)
(assert (= bs!9 start!9))
(declare-fun m!13 () Bool)
(assert (=> bs!9 m!13))
(set-info :status unsat)
(check-sat-assuming ((not start!9)))
(set-info :status unsat)
(check-sat)
