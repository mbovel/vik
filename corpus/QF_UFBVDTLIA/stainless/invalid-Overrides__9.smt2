; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/Overrides.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFBVDTLIA)
(declare-fun start!23 () Bool)
(assert start!23)
(assert (=> start!23 false))
(declare-datatypes ((A!18 0))(
  ( (C!9 (c!29 (_ BitVec 32))) (BExt!8 (__x!26 Int)) (AExt!8 (__x!27 Int)) )
))
(declare-fun lt!27 () A!18)
(declare-fun thiss!9 () A!18)
(assert (=> start!23 (= lt!27 thiss!9)))
(get-info :version)
(assert (=> start!23 ((_ is AExt!8) lt!27)))
(set-info :status unsat)
(check-sat)
