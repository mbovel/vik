; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/Havoc.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!13 () Bool)
(assert start!13)
(declare-datatypes ((T!4 0))(
  ( (TExt!3 (__x!5 Int) (x!36 Int)) )
))
(declare-fun lt!14 () T!4)
(declare-fun thiss!25 () T!4)
(declare-datatypes ((Unit!5 0))(
  ( (Unit!6) )
))
(declare-datatypes ((tuple2!4 0))(
  ( (tuple2!5 (_1!2 T!4) (_2!2 Unit!5)) )
))
(declare-fun havoc!1 (T!4) tuple2!4)
(assert (=> start!13 (= lt!14 (_1!2 (havoc!1 thiss!25)))))
(assert (=> start!13 false))
(assert (=> start!13 true))
(declare-fun bs!7 () Bool)
(assert (= bs!7 start!13))
(declare-fun m!21 () Bool)
(assert (=> bs!7 m!21))
(set-info :status unsat)
(check-sat-assuming ((not start!13)))
(set-info :status unsat)
(check-sat)
