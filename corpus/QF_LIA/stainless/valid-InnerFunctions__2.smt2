; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/InnerFunctions.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_LIA)
(declare-fun start!5 () Bool)
(assert start!5)
(declare-fun x!33 () Int)
(declare-fun a!23 () Int)
(assert (=> start!5 (and (>= x!33 0) (= a!23 (- x!33 2)) (< a!23 (- 2)))))
(assert (=> start!5 true))
(declare-fun lt!2 () Int)
(assert (=> start!5 (= lt!2 a!23)))
(assert (=> start!5 (= lt!2 (- x!33 2))))
(set-info :status unsat)
(check-sat)
