; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/Throw.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_LIA)
(declare-fun start!3 () Bool)
(assert start!3)
(declare-fun lt!2 () Int)
(assert (=> start!3 (= lt!2 0)))
(declare-fun x!18 () Int)
(assert (=> start!3 (and (< (+ x!18 lt!2) 2147483647) (>= (+ x!18 lt!2) 2147483647))))
(assert (=> start!3 true))
(set-info :status unsat)
(check-sat)
