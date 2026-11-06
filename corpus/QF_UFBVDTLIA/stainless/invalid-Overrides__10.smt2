; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/Overrides.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFBVDTLIA)
(declare-fun start!25 () Bool)
(assert start!25)
(assert (=> start!25 false))
(declare-datatypes ((A!19 0))(
  ( (C!10 (c!30 (_ BitVec 32))) (BExt!9 (__x!28 Int)) (AExt!9 (__x!29 Int)) )
))
(declare-fun lt!30 () A!19)
(declare-fun thiss!26 () A!19)
(assert (=> start!25 (= lt!30 thiss!26)))
(get-info :version)
(assert (=> start!25 ((_ is C!10) lt!30)))
(assert (=> start!25 true))
(set-info :status unsat)
(check-sat)
