; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/MergeSort.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_LIA)
(declare-fun start!888 () Bool)
(assert start!888)
(declare-fun i!35 () Int)
(assert (=> start!888 (and (< i!35 0) (< (- i!35) 0))))
(assert (=> start!888 true))
(set-info :status unsat)
(check-sat)
