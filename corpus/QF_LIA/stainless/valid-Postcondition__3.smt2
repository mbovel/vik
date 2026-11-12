; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/Postcondition.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_LIA)
(declare-fun start!7 () Bool)
(assert start!7)
(declare-fun inst!11 () Bool)
(declare-fun x!34 () Int)
(assert (=> start!7 (= inst!11 (=> true (> (+ x!34 1) x!34)))))
(assert (=> start!7 (not inst!11)))
(set-info :status unsat)
(check-sat)
