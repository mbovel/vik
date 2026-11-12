; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/InnerFunctions.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_LIA)
(declare-fun start!7 () Bool)
(assert start!7)
(assert (=> start!7 false))
(assert (=> start!7 true))
(declare-fun lt!5 () Int)
(declare-fun a!23 () Int)
(assert (=> start!7 (= lt!5 a!23)))
(declare-fun x!33 () Int)
(assert (=> start!7 (= lt!5 (- x!33 2))))
(set-info :status unsat)
(check-sat)
