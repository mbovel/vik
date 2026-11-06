; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/IsInstanceOfInt3.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFBVDTLIA)
(declare-fun start!1 () Bool)
(assert start!1)
(declare-datatypes ((Object!1 0))(
  ( (SignedBitvector32!1 (value!21 (_ BitVec 32))) (Open!1 (value!22 Int)) )
))
(declare-fun lt!2 () Object!1)
(declare-fun lambda!2 () Int)
(declare-fun x!32 () Object!1)
(declare-fun test!0 (Int Object!1 Object!1) Object!1)
(assert (=> start!1 (= lt!2 (test!0 lambda!2 x!32 (SignedBitvector32!1 #b00000000000000000000000000000000)))))
(assert (=> start!1 (and (= (bvand (value!21 lt!2) #b10000000000000000000000000000000) #b00000000000000000000000000000000) (not (= (bvand (value!21 lt!2) #b10000000000000000000000000000000) (bvand (value!21 lt!2) #b10000000000000000000000000000000))))))
(assert (=> start!1 true))
(declare-fun bs!1 () Bool)
(assert (= bs!1 start!1))
(declare-fun m!1 () Bool)
(assert (=> bs!1 m!1))
(set-info :status unsat)
(check-sat-assuming ((not start!1)))
(set-info :status unsat)
(check-sat)
