; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/BitsTricksSlow.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_BV)
(declare-fun start!137 () Bool)
(assert start!137)
(declare-fun lt!584 () (_ BitVec 32))
(declare-fun x!96 () (_ BitVec 32))
(declare-fun y!12 () (_ BitVec 32))
(assert (=> start!137 (= lt!584 (bvashr (bvxor x!96 y!12) #b00000000000000000000000000000001))))
(declare-fun lt!583 () (_ BitVec 32))
(assert (=> start!137 (= lt!583 (bvand x!96 y!12))))
(assert (=> start!137 (and (= (bvand lt!583 #b10000000000000000000000000000000) (bvand lt!584 #b10000000000000000000000000000000)) (not (= (bvand lt!583 #b10000000000000000000000000000000) (bvand (bvadd lt!583 lt!584) #b10000000000000000000000000000000))))))
(assert (=> start!137 true))
(set-info :status unsat)
(check-sat)
