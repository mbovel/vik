; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/Laws1.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!59 () Bool)
(assert start!59)
(assert (=> start!59 false))
(declare-datatypes ((A!18 0))(
  ( (BExt!8 (__x!20 Int)) (C!9) (AExt!8 (__x!21 Int)) )
))
(declare-fun lt!26 () A!18)
(declare-fun thiss!70 () A!18)
(assert (=> start!59 (= lt!26 thiss!70)))
(get-info :version)
(assert (=> start!59 (or ((_ is BExt!8) lt!26) ((_ is C!9) lt!26))))
(set-info :status unsat)
(check-sat)
