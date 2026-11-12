; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/PreInSpecs.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_LIA)
(declare-fun start!13 () Bool)
(assert start!13)
(declare-fun i!5 () Int)
(assert (=> start!13 (and (> i!5 0) (< i!5 (- 1)))))
(assert (=> start!13 true))
(set-info :status unsat)
(check-sat)
