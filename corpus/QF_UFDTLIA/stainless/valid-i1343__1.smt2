; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/i1343.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!3 () Bool)
(assert start!3)
(declare-datatypes ((Unit!3 0))(
  ( (Unit!4) )
))
(declare-fun lt!5 () Unit!3)
(declare-fun x!13 () Int)
(declare-fun myLemma!0 (Int) Unit!3)
(assert (=> start!3 (= lt!5 (myLemma!0 x!13))))
(assert (=> start!3 (and (>= x!13 0) (< x!13 0))))
(assert (=> start!3 true))
(declare-fun bs!3 () Bool)
(assert (= bs!3 start!3))
(declare-fun m!3 () Bool)
(assert (=> bs!3 m!3))
(set-info :status unsat)
(check-sat-assuming ((not start!3)))
(set-info :status unsat)
(check-sat)
