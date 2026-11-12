; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/ValueClassesInv.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_BV)
(declare-fun start!4 () Bool)
(assert start!4)
(declare-fun y!15 () (_ BitVec 32))
(assert (=> start!4 (and (bvsge y!15 #b00000000000000000000000000000000) (bvslt y!15 #b00000000000000000000000001100100) (or (not (= (bvand y!15 #b10000000000000000000000000000000) #b00000000000000000000000000000000)) (= (bvand y!15 #b10000000000000000000000000000000) (bvand (bvadd y!15 #b00000000000000000000000001011101) #b10000000000000000000000000000000))) (= (bvadd y!15 #b00000000000000000000000001011101) #b00000000000000000000000000101010))))
(assert (=> start!4 true))
(set-info :status unsat)
(check-sat)
