; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/Overrides.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFBVDTLIA)
(declare-fun start!13 () Bool)
(assert start!13)
(assert (=> start!13 false))
(declare-datatypes ((A!13 0))(
  ( (C!4 (c!24 (_ BitVec 32))) (BExt!3 (__x!16 Int)) (AExt!3 (__x!17 Int)) )
))
(declare-fun lt!12 () A!13)
(declare-fun thiss!20 () A!13)
(assert (=> start!13 (= lt!12 thiss!20)))
(get-info :version)
(assert (=> start!13 (or ((_ is C!4) lt!12) ((_ is BExt!3) lt!12))))
(assert (=> start!13 true))
(set-info :status unsat)
(check-sat)
