; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/BinarySearch.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_AUFBVDT)
(declare-fun start!15 () Bool)
(assert start!15)
(declare-fun lo!1 () (_ BitVec 32))
(declare-fun hi!1 () (_ BitVec 32))
(declare-datatypes ((array!17 0))(
  ( (array!18 (arr!16 (Array (_ BitVec 32) (_ BitVec 32))) (size!8 (_ BitVec 32))) )
))
(declare-fun arr!1 () array!17)
(assert (=> start!15 (and (bvsle #b00000000000000000000000000000000 lo!1) (bvslt hi!1 (size!8 arr!1)) (bvsle lo!1 (bvadd hi!1 #b00000000000000000000000000000001)) (bvsle lo!1 hi!1) (let ((rhs!1 (bvsdiv (bvsub hi!1 lo!1) #b00000000000000000000000000000010))) (and (= (bvand lo!1 #b10000000000000000000000000000000) (bvand rhs!1 #b10000000000000000000000000000000)) (not (= (bvand lo!1 #b10000000000000000000000000000000) (bvand (bvadd lo!1 rhs!1) #b10000000000000000000000000000000))))))))
(assert (=> start!15 true))
(declare-fun array_inv!8 (array!17) Bool)
(assert (=> start!15 (array_inv!8 arr!1)))
(declare-fun bs!15 () Bool)
(assert (= bs!15 start!15))
(declare-fun m!19 () Bool)
(assert (=> bs!15 m!19))
(set-info :status unsat)
(check-sat-assuming ((not start!15)))
(set-info :status unsat)
(check-sat)
