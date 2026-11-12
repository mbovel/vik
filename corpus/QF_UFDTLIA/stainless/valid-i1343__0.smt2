; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/i1343.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!1 () Bool)
(assert start!1)
(declare-datatypes ((Unit!1 0))(
  ( (Unit!2) )
))
(declare-fun lt!2 () Unit!1)
(declare-fun x!22 () Int)
(declare-fun myLemma!0 (Int) Unit!1)
(assert (=> start!1 (= lt!2 (myLemma!0 x!22))))
(assert (=> start!1 (and (>= (+ x!22 3 2) 0) (< x!22 (- 5)))))
(assert (=> start!1 true))
(declare-fun bs!1 () Bool)
(assert (= bs!1 start!1))
(declare-fun m!1 () Bool)
(assert (=> bs!1 m!1))
(set-info :status unsat)
(check-sat-assuming ((not start!1)))
(set-info :status unsat)
(check-sat)
