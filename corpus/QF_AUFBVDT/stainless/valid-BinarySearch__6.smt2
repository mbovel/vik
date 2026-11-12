; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/BinarySearch.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_AUFBVDT)
(declare-fun start!13 () Bool)
(assert start!13)
(declare-fun lo!1 () (_ BitVec 32))
(declare-fun hi!1 () (_ BitVec 32))
(declare-datatypes ((array!15 0))(
  ( (array!16 (arr!15 (Array (_ BitVec 32) (_ BitVec 32))) (size!7 (_ BitVec 32))) )
))
(declare-fun arr!1 () array!15)
(assert (=> start!13 (and (bvsle #b00000000000000000000000000000000 lo!1) (bvslt hi!1 (size!7 arr!1)) (bvsle lo!1 (bvadd hi!1 #b00000000000000000000000000000001)) (bvsle lo!1 hi!1) (not (= (bvand hi!1 #b10000000000000000000000000000000) (bvand lo!1 #b10000000000000000000000000000000))) (not (= (bvand hi!1 #b10000000000000000000000000000000) (bvand (bvsub hi!1 lo!1) #b10000000000000000000000000000000))))))
(assert (=> start!13 true))
(declare-fun array_inv!7 (array!15) Bool)
(assert (=> start!13 (array_inv!7 arr!1)))
(declare-fun bs!13 () Bool)
(assert (= bs!13 start!13))
(declare-fun m!17 () Bool)
(assert (=> bs!13 m!17))
(set-info :status unsat)
(check-sat-assuming ((not start!13)))
(set-info :status unsat)
(check-sat)
