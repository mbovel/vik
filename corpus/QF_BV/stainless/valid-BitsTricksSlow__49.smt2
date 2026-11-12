; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/BitsTricksSlow.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_BV)
(declare-fun start!155 () Bool)
(assert start!155)
(declare-fun x!95 () (_ BitVec 32))
(declare-fun y!11 () (_ BitVec 32))
(assert (=> start!155 (and (or (bvslt x!95 #b00000000000000000000000000000000) (bvsgt y!11 #b00000000000000000000000000000000)) (or (bvsgt x!95 #b00000000000000000000000000000000) (bvslt y!11 #b00000000000000000000000000000000)) (or (bvslt x!95 #b00000000000000000000000000000000) (bvsgt x!95 y!11)) (bvsge x!95 #b00000000000000000000000000000000) (bvsle y!11 x!95) (let ((rhs!9 (bvsdiv (bvsub x!95 y!11) #b00000000000000000000000000000010))) (and (= (bvand y!11 #b10000000000000000000000000000000) (bvand rhs!9 #b10000000000000000000000000000000)) (not (= (bvand y!11 #b10000000000000000000000000000000) (bvand (bvadd y!11 rhs!9) #b10000000000000000000000000000000))))))))
(assert (=> start!155 true))
(set-info :status unsat)
(check-sat)
