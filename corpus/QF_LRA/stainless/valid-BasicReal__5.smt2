; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/BasicReal.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_LRA)
(declare-fun start!11 () Bool)
(assert start!11)
(declare-fun x!14 () Real)
(assert (=> start!11 (and (> x!14 (/ 0 1)) (<= (* (/ 2 1) x!14) x!14))))
(assert (=> start!11 true))
(set-info :status unsat)
(check-sat)
