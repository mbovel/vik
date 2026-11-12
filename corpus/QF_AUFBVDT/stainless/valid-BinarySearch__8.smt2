; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/BinarySearch.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_AUFBVDT)
(declare-fun start!17 () Bool)
(assert start!17)
(declare-fun lo!1 () (_ BitVec 32))
(declare-fun hi!1 () (_ BitVec 32))
(declare-datatypes ((array!19 0))(
  ( (array!20 (arr!17 (Array (_ BitVec 32) (_ BitVec 32))) (size!9 (_ BitVec 32))) )
))
(declare-fun arr!1 () array!19)
(assert (=> start!17 (and (bvsle #b00000000000000000000000000000000 lo!1) (bvslt hi!1 (size!9 arr!1)) (bvsle lo!1 (bvadd hi!1 #b00000000000000000000000000000001)) (bvsle lo!1 hi!1) (let ((i!1 (bvadd lo!1 (bvsdiv (bvsub hi!1 lo!1) #b00000000000000000000000000000010)))) (or (bvslt i!1 #b00000000000000000000000000000000) (bvsge i!1 (size!9 arr!1)))))))
(assert (=> start!17 true))
(declare-fun array_inv!9 (array!19) Bool)
(assert (=> start!17 (array_inv!9 arr!1)))
(declare-fun bs!17 () Bool)
(assert (= bs!17 start!17))
(declare-fun m!21 () Bool)
(assert (=> bs!17 m!21))
(set-info :status unsat)
(check-sat-assuming ((not start!17)))
(set-info :status unsat)
(check-sat)
