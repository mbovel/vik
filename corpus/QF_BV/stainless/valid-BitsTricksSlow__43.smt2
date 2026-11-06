; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/BitsTricksSlow.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_BV)
(declare-fun start!141 () Bool)
(assert start!141)
(declare-fun lt!589 () (_ BitVec 32))
(declare-fun lt!590 () (_ BitVec 32))
(declare-fun x!96 () (_ BitVec 32))
(declare-fun y!12 () (_ BitVec 32))
(assert (=> start!141 (= lt!590 (bvand (bvlshr lt!589 #b00000000000000000000000000011111) (bvxor x!96 y!12)))))
(assert (=> start!141 (= lt!589 (bvadd (bvand x!96 y!12) (bvashr (bvxor x!96 y!12) #b00000000000000000000000000000001)))))
(assert (=> start!141 (and (= (bvand lt!589 #b10000000000000000000000000000000) (bvand lt!590 #b10000000000000000000000000000000)) (not (= (bvand lt!589 #b10000000000000000000000000000000) (bvand (bvadd lt!589 lt!590) #b10000000000000000000000000000000))))))
(assert (=> start!141 true))
(set-info :status unsat)
(check-sat)
