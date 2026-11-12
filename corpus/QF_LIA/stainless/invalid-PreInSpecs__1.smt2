; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/PreInSpecs.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_LIA)
(declare-fun start!3 () Bool)
(assert start!3)
(declare-fun i!6 () Int)
(assert (=> start!3 (and (not (= i!6 0)) (> i!6 0) (< (- i!6 1) 0))))
(assert (=> start!3 true))
(set-info :status unsat)
(check-sat)
