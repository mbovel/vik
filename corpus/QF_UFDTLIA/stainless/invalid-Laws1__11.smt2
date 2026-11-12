; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/Laws1.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!75 () Bool)
(assert start!75)
(assert (=> start!75 false))
(declare-datatypes ((A!20 0))(
  ( (BExt!10 (__x!24 Int)) (C!11) (AExt!10 (__x!25 Int)) )
))
(declare-fun lt!32 () A!20)
(declare-fun thiss!63 () A!20)
(assert (=> start!75 (= lt!32 thiss!63)))
(get-info :version)
(assert (=> start!75 ((_ is BExt!10) lt!32)))
(set-info :status unsat)
(check-sat)
