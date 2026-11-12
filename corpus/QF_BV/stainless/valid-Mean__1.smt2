; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/Mean.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_BV)
(declare-fun start!3 () Bool)
(assert start!3)
(declare-fun x!8 () (_ BitVec 32))
(declare-fun y!1 () (_ BitVec 32))
(assert (=> start!3 (and (bvsle x!8 y!1) (bvsge x!8 #b00000000000000000000000000000000) (bvsge y!1 #b00000000000000000000000000000000) (let ((rhs!1 (bvsdiv (bvsub y!1 x!8) #b00000000000000000000000000000010))) (and (= (bvand x!8 #b10000000000000000000000000000000) (bvand rhs!1 #b10000000000000000000000000000000)) (not (= (bvand x!8 #b10000000000000000000000000000000) (bvand (bvadd x!8 rhs!1) #b10000000000000000000000000000000))))))))
(assert (=> start!3 true))
(set-info :status unsat)
(check-sat)
