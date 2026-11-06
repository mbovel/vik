; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/IsInstanceOfInt2.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_BV)
(declare-fun start!1 () Bool)
(assert start!1)
(declare-fun x!8 () (_ BitVec 32))
(assert (=> start!1 (and (= (bvand x!8 #b10000000000000000000000000000000) #b00000000000000000000000000000000) (not (= (bvand x!8 #b10000000000000000000000000000000) (bvand x!8 #b10000000000000000000000000000000))))))
(assert (=> start!1 true))
(set-info :status unsat)
(check-sat)
