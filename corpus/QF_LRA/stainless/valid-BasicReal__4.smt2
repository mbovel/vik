; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/BasicReal.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_LRA)
(declare-fun start!9 () Bool)
(assert start!9)
(declare-fun x!11 () Real)
(declare-fun y!2 () Real)
(assert (=> start!9 (and (>= x!11 (/ 0 1)) (>= y!2 (/ 0 1)) (< (+ x!11 y!2) (/ 0 1)))))
(assert (=> start!9 true))
(set-info :status unsat)
(check-sat)
