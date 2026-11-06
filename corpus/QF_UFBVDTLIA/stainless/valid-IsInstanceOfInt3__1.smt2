; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/IsInstanceOfInt3.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFBVDTLIA)
(declare-fun start!3 () Bool)
(assert start!3)
(declare-datatypes ((Object!2 0))(
  ( (SignedBitvector32!2 (value!24 (_ BitVec 32))) (Open!2 (value!25 Int)) )
))
(declare-fun lt!5 () Object!2)
(declare-fun lambda!5 () Int)
(declare-fun test!0 (Int Object!2 Object!2) Object!2)
(assert (=> start!3 (= lt!5 (test!0 lambda!5 (SignedBitvector32!2 #b00000000000000000000000000000010) (SignedBitvector32!2 #b00000000000000000000000000000000)))))
(assert (=> start!3 (and (= (bvand (value!24 lt!5) #b10000000000000000000000000000000) #b00000000000000000000000000000000) (not (= (bvand (value!24 lt!5) #b10000000000000000000000000000000) (bvand (value!24 lt!5) #b10000000000000000000000000000000))))))
(declare-fun bs!3 () Bool)
(assert (= bs!3 start!3))
(declare-fun m!3 () Bool)
(assert (=> bs!3 m!3))
(set-info :status unsat)
(check-sat-assuming ((not start!3)))
(set-info :status unsat)
(check-sat)
