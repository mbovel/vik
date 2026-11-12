; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/InnerFunctions.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_LIA)
(declare-fun start!9 () Bool)
(assert start!9)
(declare-fun x!35 () Int)
(declare-fun y!24 () Int)
(declare-fun a!25 () Int)
(assert (=> start!9 (and (<= 0 x!35) (<= x!35 y!24) (= a!25 (+ x!35 1)) (< (+ a!25 1) 2))))
(assert (=> start!9 true))
(declare-fun lt!8 () Int)
(assert (=> start!9 (= lt!8 a!25)))
(assert (=> start!9 (= lt!8 (+ x!35 1))))
(set-info :status unsat)
(check-sat)
