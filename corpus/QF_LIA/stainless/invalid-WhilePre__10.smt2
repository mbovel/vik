; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/WhilePre.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_LIA)
(declare-fun start!21 () Bool)
(assert start!21)
(declare-fun n!7 () Int)
(assert (=> start!21 (and (> n!7 0) (not (= n!7 5)))))
(assert (=> start!21 true))
(set-info :status sat)
(check-sat)
(get-model)
