; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/BinarySearch.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_AUFBVDT)
(declare-fun start!7 () Bool)
(assert start!7)
(declare-fun lo!1 () (_ BitVec 32))
(declare-fun hi!1 () (_ BitVec 32))
(declare-datatypes ((array!9 0))(
  ( (array!10 (arr!12 (Array (_ BitVec 32) (_ BitVec 32))) (size!4 (_ BitVec 32))) )
))
(declare-fun arr!1 () array!9)
(assert (=> start!7 (and (bvsle #b00000000000000000000000000000000 lo!1) (bvslt hi!1 (size!4 arr!1)) (bvsle lo!1 (bvadd hi!1 #b00000000000000000000000000000001)) (not (= (bvand hi!1 #b10000000000000000000000000000000) (bvand lo!1 #b10000000000000000000000000000000))) (not (= (bvand hi!1 #b10000000000000000000000000000000) (bvand (bvsub hi!1 lo!1) #b10000000000000000000000000000000))))))
(assert (=> start!7 true))
(declare-fun array_inv!4 (array!9) Bool)
(assert (=> start!7 (array_inv!4 arr!1)))
(declare-fun bs!7 () Bool)
(assert (= bs!7 start!7))
(declare-fun m!11 () Bool)
(assert (=> bs!7 m!11))
(set-info :status unsat)
(check-sat-assuming ((not start!7)))
(set-info :status unsat)
(check-sat)
