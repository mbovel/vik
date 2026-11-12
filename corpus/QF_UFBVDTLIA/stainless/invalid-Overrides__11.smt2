; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/Overrides.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFBVDTLIA)
(declare-fun start!27 () Bool)
(assert start!27)
(declare-datatypes ((A!20 0))(
  ( (C!11 (c!31 (_ BitVec 32))) (BExt!10 (__x!30 Int)) (AExt!10 (__x!31 Int)) )
))
(declare-fun thiss!26 () A!20)
(declare-fun i!3 () (_ BitVec 32))
(get-info :version)
(assert (=> start!27 (and ((_ is C!11) thiss!26) (bvsge i!3 #b00000000000000000000000000000000) (not (= i!3 #b00000000000000000000000000000000)) (bvslt (bvsub i!3 #b00000000000000000000000000000001) #b00000000000000000000000000000000))))
(declare-fun lt!33 () A!20)
(assert (=> start!27 (= lt!33 thiss!26)))
(assert (=> start!27 ((_ is C!11) lt!33)))
(assert (=> start!27 true))
(set-info :status unsat)
(check-sat)
