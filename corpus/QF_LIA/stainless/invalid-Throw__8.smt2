; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/Throw.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_LIA)
(declare-fun start!17 () Bool)
(assert start!17)
(declare-fun x!25 () Int)
(declare-fun i!39 () Int)
(declare-fun acc!39 () Int)
(assert (=> start!17 (and (< (+ x!25 i!39) 2147483647) (or (< (+ i!39 1) 0) (< (+ acc!39 x!25 i!39) 0)))))
(assert (=> start!17 true))
(set-info :status sat)
(check-sat)
(get-model)
