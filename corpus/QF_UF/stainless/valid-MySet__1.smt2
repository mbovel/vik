; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/MySet.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UF)
(declare-fun start!4 () Bool)
(assert start!4)
(assert (=> start!4 false))
(set-info :status unsat)
(check-sat)
