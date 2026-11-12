; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/Equations3.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!1 () Bool)
(assert start!1)
(declare-datatypes ((Unit!1 0))(
  ( (Unit!2) )
))
(declare-fun lt!2 () Unit!1)
(declare-fun Unit!3 () Unit!1)
(assert (=> start!1 (= lt!2 Unit!3)))
(declare-fun x!38 () Int)
(declare-fun z!1 () Int)
(assert (=> start!1 (= x!38 z!1)))
(assert (=> start!1 (not (= x!38 z!1))))
(assert (=> start!1 true))
(set-info :status unsat)
(check-sat)
