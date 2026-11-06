; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/i1398.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!1 () Bool)
(assert start!1)
(declare-fun lambda!20 () Int)
(declare-datatypes ((A!19 0))(
  ( (A!20 (val!0 Int)) )
))
(declare-fun x!64 () A!19)
(declare-datatypes ((tuple2!0 0))(
  ( (tuple2!1 (_1!0 A!19) (_2!0 A!19)) )
))
(declare-datatypes ((tuple2!2 0))(
  ( (tuple2!3 (_1!1 tuple2!0) (_2!1 tuple2!0)) )
))
(declare-fun dynLambda!0 (Int A!19) tuple2!2)
(assert (=> start!1 (not (= (dynLambda!0 lambda!20 x!64) (dynLambda!0 lambda!20 x!64)))))
(declare-fun tp_is_empty!1 () Bool)
(assert (=> start!1 tp_is_empty!1))
(declare-fun b_lambda!1 () Bool)
(assert (=> (not b_lambda!1) (not start!1)))
(declare-fun bs!1 () Bool)
(assert (= bs!1 start!1))
(declare-fun m!1 () Bool)
(assert (=> bs!1 m!1))
(assert (=> bs!1 m!1))
(set-info :status unsat)
(check-sat-assuming (tp_is_empty!1 (not b_lambda!1)))
(set-info :status unsat)
(check-sat)
