; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/BinarySearch.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_AUFBVDT)
(declare-fun start!5 () Bool)
(assert start!5)
(declare-fun lo!1 () (_ BitVec 32))
(declare-fun hi!1 () (_ BitVec 32))
(declare-datatypes ((array!7 0))(
  ( (array!8 (arr!11 (Array (_ BitVec 32) (_ BitVec 32))) (size!3 (_ BitVec 32))) )
))
(declare-fun arr!1 () array!7)
(assert (=> start!5 (and (bvsle #b00000000000000000000000000000000 lo!1) (bvslt hi!1 (size!3 arr!1)) (= (bvand hi!1 #b10000000000000000000000000000000) #b00000000000000000000000000000000) (not (= (bvand hi!1 #b10000000000000000000000000000000) (bvand (bvadd hi!1 #b00000000000000000000000000000001) #b10000000000000000000000000000000))))))
(assert (=> start!5 true))
(declare-fun array_inv!3 (array!7) Bool)
(assert (=> start!5 (array_inv!3 arr!1)))
(declare-fun bs!5 () Bool)
(assert (= bs!5 start!5))
(declare-fun m!9 () Bool)
(assert (=> bs!5 m!9))
(set-info :status unsat)
(check-sat-assuming ((not start!5)))
(set-info :status unsat)
(check-sat)
