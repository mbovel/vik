; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/Overrides.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFBVDTLIA)
(declare-fun start!21 () Bool)
(assert start!21)
(assert (=> start!21 false))
(declare-datatypes ((A!17 0))(
  ( (C!8 (c!28 (_ BitVec 32))) (BExt!7 (__x!24 Int)) (AExt!7 (__x!25 Int)) )
))
(declare-fun lt!24 () A!17)
(declare-fun thiss!17 () A!17)
(assert (=> start!21 (= lt!24 thiss!17)))
(get-info :version)
(assert (=> start!21 ((_ is BExt!7) lt!24)))
(assert (=> start!21 true))
(set-info :status unsat)
(check-sat)
