; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/i1399a.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!8 () Bool)
(assert start!8)
(declare-datatypes ((Option!7 0))(
  ( (Some!6 (v!40 Int)) (None!6) )
))
(declare-datatypes ((List!6 0))(
  ( (Nil!4) (Cons!4 (h!67 Option!7) (t!82 List!6)) )
))
(declare-fun lt!5 () List!6)
(declare-datatypes ((tuple2!2 0))(
  ( (tuple2!3 (_1!1 Int) (_2!1 Option!7)) )
))
(declare-datatypes ((List!7 0))(
  ( (Nil!5) (Cons!5 (h!68 tuple2!2) (t!83 List!7)) )
))
(declare-fun l!110 () List!7)
(declare-fun bar!0 (List!7) List!6)
(assert (=> start!8 (= lt!5 (bar!0 l!110))))
(assert (=> start!8 false))
(assert (=> start!8 true))
(declare-fun bs!6 () Bool)
(assert (= bs!6 start!8))
(declare-fun m!17 () Bool)
(assert (=> bs!6 m!17))
(set-info :status unsat)
(check-sat-assuming ((not start!8)))
(set-info :status unsat)
(check-sat)
