; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/i1399a.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!32 () Bool)
(assert start!32)
(declare-datatypes ((Option!13 0))(
  ( (Some!12 (v!46 Int)) (None!12) )
))
(declare-datatypes ((List!18 0))(
  ( (Nil!16) (Cons!16 (h!79 Option!13) (t!94 List!18)) )
))
(declare-fun lt!38 () List!18)
(declare-datatypes ((tuple2!14 0))(
  ( (tuple2!15 (_1!7 Int) (_2!7 Option!13)) )
))
(declare-datatypes ((List!19 0))(
  ( (Nil!17) (Cons!17 (h!80 tuple2!14) (t!95 List!19)) )
))
(declare-fun l!110 () List!19)
(declare-fun lambda!26 () Int)
(declare-fun map!12 (List!19 Int) List!18)
(assert (=> start!32 (= lt!38 (map!12 l!110 lambda!26))))
(declare-fun lt!37 () List!18)
(declare-fun bar!0 (List!19) List!18)
(assert (=> start!32 (= lt!37 (bar!0 l!110))))
(assert (=> start!32 false))
(assert (=> start!32 true))
(declare-fun bs!27 () Bool)
(assert (= bs!27 start!32))
(declare-fun m!71 () Bool)
(assert (=> bs!27 m!71))
(declare-fun m!73 () Bool)
(assert (=> bs!27 m!73))
(set-info :status unsat)
(check-sat-assuming ((not start!32)))
(set-info :status unsat)
(check-sat)
