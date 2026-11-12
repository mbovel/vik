; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/Asserts1.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_BV)
(declare-fun start!17 () Bool)
(assert start!17)
(declare-fun a!5 () (_ BitVec 32))
(assert (=> start!17 (and (bvsgt a!5 #b00000000000000000000000000000000) (or (not (= (bvand a!5 #b10000000000000000000000000000000) #b00000000000000000000000000000000)) (= (bvand a!5 #b10000000000000000000000000000000) (bvand (bvadd a!5 #b00000000000000000000000000000010) #b10000000000000000000000000000000))) (let ((res!5 (bvadd a!5 #b00000000000000000000000000000010))) (or (bvsle res!5 a!5) (bvsle res!5 #b00000000000000000000000000000010))))))
(assert (=> start!17 true))
(set-info :status unsat)
(check-sat)
