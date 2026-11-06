; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/Longs.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_BV)
(declare-fun start!29 () Bool)
(assert start!29)
(declare-fun y!3 () (_ BitVec 64))
(assert (=> start!29 (and (bvsge y!3 #b0000000000000000000000000000000000000000000000000000000000000000) (not (= y!3 #b0000000000000000000000000000000000000000000000000000000000000000)) (let ((rhs!11 #b0000000000000000000000000000000000000000000000000000000000000001)) (and (not (= (bvand y!3 #b1000000000000000000000000000000000000000000000000000000000000000) (bvand rhs!11 #b1000000000000000000000000000000000000000000000000000000000000000))) (not (= (bvand y!3 #b1000000000000000000000000000000000000000000000000000000000000000) (bvand (bvsub y!3 rhs!11) #b1000000000000000000000000000000000000000000000000000000000000000))))))))
(assert (=> start!29 true))
(set-info :status unsat)
(check-sat)
