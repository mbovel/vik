; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/BasicReal.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_LRA)
(declare-fun start!3 () Bool)
(assert start!3)
(declare-fun x!13 () Real)
(assert (=> start!3 (not (= (/ x!13 (/ 2 1)) (* x!13 (/ 1 2))))))
(assert (=> start!3 true))
(set-info :status unsat)
(check-sat)
