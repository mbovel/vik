; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/Overrides.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFBVDTLIA)
(declare-fun start!37 () Bool)
(assert start!37)
(declare-datatypes ((A!23 0))(
  ( (C!14 (c!46 (_ BitVec 32))) (BExt!13 (__x!36 Int)) (AExt!13 (__x!37 Int)) )
))
(declare-fun thiss!26 () A!23)
(declare-fun i!3 () (_ BitVec 32))
(get-info :version)
(assert (=> start!37 (and ((_ is C!14) thiss!26) (bvsge i!3 #b00000000000000000000000000000000) (not (= i!3 #b00000000000000000000000000000000)) (not (= (bvand i!3 #b10000000000000000000000000000000) #b00000000000000000000000000000000)) (not (= (bvand i!3 #b10000000000000000000000000000000) (bvand (bvsub i!3 #b00000000000000000000000000000001) #b10000000000000000000000000000000))))))
(declare-fun lt!51 () A!23)
(assert (=> start!37 (= lt!51 thiss!26)))
(assert (=> start!37 ((_ is C!14) lt!51)))
(assert (=> start!37 true))
(set-info :status unsat)
(check-sat)
