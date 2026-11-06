; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/Havoc.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!21 () Bool)
(assert start!21)
(declare-datatypes ((T!6 0))(
  ( (TExt!5 (__x!7 Int) (x!40 Int)) )
))
(declare-fun lt!22 () T!6)
(declare-fun thiss!14 () T!6)
(declare-datatypes ((Unit!9 0))(
  ( (Unit!10) )
))
(declare-datatypes ((tuple2!8 0))(
  ( (tuple2!9 (_1!4 T!6) (_2!4 Unit!9)) )
))
(declare-fun choose!0 (T!6) tuple2!8)
(assert (=> start!21 (= lt!22 (_1!4 (choose!0 thiss!14)))))
(assert (=> start!21 false))
(declare-fun lt!23 () T!6)
(assert (=> start!21 (= lt!23 thiss!14)))
(get-info :version)
(assert (=> start!21 ((_ is TExt!5) lt!23)))
(declare-fun bs!12 () Bool)
(assert (= bs!12 start!21))
(declare-fun m!33 () Bool)
(assert (=> bs!12 m!33))
(set-info :status unsat)
(check-sat-assuming ((not start!21)))
(set-info :status unsat)
(check-sat)
