; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/Streams.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_LIA)
(declare-fun start!25 () Bool)
(assert start!25)
(declare-fun n!7 () Int)
(assert (=> start!25 (and (>= n!7 0) (> n!7 0) (not (= (- n!7 1) (- n!7 1))))))
(assert (=> start!25 true))
(set-info :status unsat)
(check-sat)
