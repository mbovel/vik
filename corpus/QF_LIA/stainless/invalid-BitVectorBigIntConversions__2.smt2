; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/BitVectorBigIntConversions.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_LIA)
(declare-fun start!5 () Bool)
(assert start!5)
(declare-fun i!1 () Int)
(assert (=> start!5 (and (>= i!1 (- 2147483648)) (> i!1 2147483647))))
(assert (=> start!5 true))
(set-info :status sat)
(check-sat)
(get-model)
