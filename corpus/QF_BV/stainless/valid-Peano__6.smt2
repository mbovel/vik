; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/Peano.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_BV)
(declare-fun start!17 () Bool)
(assert start!17)
(declare-fun n!6 () (_ BitVec 32))
(assert (=> start!17 (and (bvsge n!6 #b00000000000000000000000000000000) (not (= n!6 #b00000000000000000000000000000000)) (not (= (bvand n!6 #b10000000000000000000000000000000) #b00000000000000000000000000000000)) (not (= (bvand n!6 #b10000000000000000000000000000000) (bvand (bvsub n!6 #b00000000000000000000000000000001) #b10000000000000000000000000000000))))))
(assert (=> start!17 true))
(set-info :status unsat)
(check-sat)
