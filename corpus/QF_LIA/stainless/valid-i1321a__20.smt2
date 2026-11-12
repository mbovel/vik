; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/i1321a.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_LIA)
(declare-fun start!147 () Bool)
(assert start!147)
(declare-fun n!2 () Int)
(assert (=> start!147 (and (<= 0 n!2) (> n!2 0) (> 0 (- n!2 1)))))
(assert (=> start!147 true))
(set-info :status unsat)
(check-sat)
