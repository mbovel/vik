; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/i1321a.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!101 () Bool)
(assert start!101)
(assert (=> start!101 false))
(declare-datatypes ((MyInt!23 0))(
  ( (PosInt!14 (v!75 Int)) (MyIntExt!13 (__x!26 Int)) )
))
(declare-fun lt!153 () MyInt!23)
(declare-fun thiss!36 () MyInt!23)
(assert (=> start!101 (= lt!153 thiss!36)))
(declare-fun inv!1 (MyInt!23) Bool)
(get-info :version)
(assert (=> start!101 (and (inv!1 thiss!36) ((_ is MyIntExt!13) lt!153))))
(declare-fun bs!24 () Bool)
(assert (= bs!24 start!101))
(declare-fun m!411 () Bool)
(assert (=> bs!24 m!411))
(set-info :status unsat)
(check-sat-assuming ((not start!101)))
(set-info :status unsat)
(check-sat)
