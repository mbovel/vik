; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/Methods.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!29 () Bool)
(assert start!29)
(assert (=> start!29 false))
(declare-datatypes ((A!18 0))(
  ( (D!9) (C!9 (x!36 Int)) (E!9) )
))
(declare-fun lt!17 () A!18)
(declare-fun thiss!17 () A!18)
(assert (=> start!29 (= lt!17 thiss!17)))
(declare-fun inv!2 (A!18) Bool)
(get-info :version)
(assert (=> start!29 (and (inv!2 thiss!17) ((_ is C!9) lt!17))))
(declare-fun bs!15 () Bool)
(assert (= bs!15 start!29))
(declare-fun m!29 () Bool)
(assert (=> bs!15 m!29))
(set-info :status unsat)
(check-sat-assuming ((not start!29)))
(set-info :status unsat)
(check-sat)
