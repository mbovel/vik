; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/i1343.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_LIA)
(declare-fun start!9 () Bool)
(assert start!9)
(declare-fun x!12 () Int)
(assert (=> start!9 (not (= (+ x!12 x!12) (* 2 x!12)))))
(assert (=> start!9 true))
(set-info :status unsat)
(check-sat)
