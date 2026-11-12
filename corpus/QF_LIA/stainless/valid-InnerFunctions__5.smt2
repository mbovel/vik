; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/InnerFunctions.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_LIA)
(declare-fun start!11 () Bool)
(assert start!11)
(declare-fun x!36 () Int)
(declare-fun y!25 () Int)
(declare-fun a!26 () Int)
(assert (=> start!11 (and (<= 0 x!36) (<= x!36 y!25) (= a!26 (+ x!36 1)) (< y!25 0))))
(assert (=> start!11 true))
(declare-fun lt!11 () Int)
(assert (=> start!11 (= lt!11 a!26)))
(assert (=> start!11 (= lt!11 (+ x!36 1))))
(set-info :status unsat)
(check-sat)
