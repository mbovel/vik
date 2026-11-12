; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/Throw.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_LIA)
(declare-fun start!3 () Bool)
(assert start!3)
(declare-fun x!17 () Int)
(assert (=> start!3 (and (>= x!17 0) (< (+ x!17 1) 1))))
(assert (=> start!3 true))
(set-info :status unsat)
(check-sat)
