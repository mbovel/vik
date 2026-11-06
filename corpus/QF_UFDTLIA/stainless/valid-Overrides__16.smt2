; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/Overrides.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!63 () Bool)
(assert start!63)
(assert (=> start!63 false))
(declare-datatypes ((B!19 0))(
  ( (C!18 (c!40 Int)) (D!18) )
))
(declare-fun lt!60 () B!19)
(declare-fun thiss!9 () B!19)
(assert (=> start!63 (= lt!60 thiss!9)))
(declare-fun inv!1 (B!19) Bool)
(get-info :version)
(assert (=> start!63 (and (inv!1 thiss!9) ((_ is C!18) lt!60))))
(declare-fun bs!27 () Bool)
(assert (= bs!27 start!63))
(declare-fun m!79 () Bool)
(assert (=> bs!27 m!79))
(set-info :status unsat)
(check-sat-assuming ((not start!63)))
(set-info :status unsat)
(check-sat)
