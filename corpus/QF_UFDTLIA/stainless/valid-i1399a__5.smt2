; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/i1399a.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!28 () Bool)
(assert start!28)
(declare-datatypes ((Option!11 0))(
  ( (Some!10 (v!44 Int)) (None!10) )
))
(declare-datatypes ((List!14 0))(
  ( (Nil!12) (Cons!12 (h!75 Option!11) (t!90 List!14)) )
))
(declare-fun lt!29 () List!14)
(declare-datatypes ((tuple2!10 0))(
  ( (tuple2!11 (_1!5 Int) (_2!5 Option!11)) )
))
(declare-datatypes ((List!15 0))(
  ( (Nil!13) (Cons!13 (h!76 tuple2!10) (t!91 List!15)) )
))
(declare-fun l!110 () List!15)
(declare-fun bar!0 (List!15) List!14)
(assert (=> start!28 (= lt!29 (bar!0 l!110))))
(declare-fun v!22 () Option!11)
(get-info :version)
(assert (=> start!28 (and ((_ is None!10) v!22) (not ((_ is None!10) v!22)))))
(assert (=> start!28 true))
(declare-fun bs!23 () Bool)
(assert (= bs!23 start!28))
(declare-fun m!67 () Bool)
(assert (=> bs!23 m!67))
(set-info :status unsat)
(check-sat-assuming ((not start!28)))
(set-info :status unsat)
(check-sat)
