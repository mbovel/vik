; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/ForallExistsTest.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_LIA)
(declare-fun start!51 () Bool)
(assert start!51)
(declare-fun lt!98 () Int)
(declare-fun x!40 () Int)
(assert (=> start!51 (= lt!98 (+ x!40 x!40 x!40 x!40))))
(assert (=> start!51 (>= lt!98 (+ lt!98 1))))
(assert (=> start!51 true))
(set-info :status unsat)
(check-sat)
