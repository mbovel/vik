; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/BasicReal.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_LRA)
(declare-fun start!5 () Bool)
(assert start!5)
(declare-fun x!12 () Real)
(declare-fun y!3 () Real)
(assert (=> start!5 (and (>= x!12 y!3) (< x!12 x!12))))
(assert (=> start!5 true))
(set-info :status unsat)
(check-sat)
