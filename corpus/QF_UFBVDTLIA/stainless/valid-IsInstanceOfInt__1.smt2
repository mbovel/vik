; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/IsInstanceOfInt.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFBVDTLIA)
(declare-fun start!3 () Bool)
(assert start!3)
(declare-datatypes ((Object!2 0))(
  ( (SignedBitvector32!2 (value!21 (_ BitVec 32))) (Open!2 (value!22 Int)) )
))
(declare-fun lt!10 () Object!2)
(declare-fun empty!8 () Object!2)
(assert (=> start!3 (= lt!10 empty!8)))
(assert (=> start!3 true))
(assert (=> start!3 false))
(declare-fun lt!11 () Object!2)
(assert (=> start!3 (= lt!11 empty!8)))
(get-info :version)
(assert (=> start!3 (and ((_ is SignedBitvector32!2) lt!10) (= (bvand (value!21 lt!10) #b10000000000000000000000000000000) #b00000000000000000000000000000000) (not (= (bvand (value!21 lt!10) #b10000000000000000000000000000000) (bvand (bvadd (value!21 lt!10) #b00000000000000000000000000000010) #b10000000000000000000000000000000))))))
(set-info :status unsat)
(check-sat)
