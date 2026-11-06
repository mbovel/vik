; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/PartialKVTrace.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_LIA)
(declare-fun start!10 () Bool)
(assert start!10)
(declare-fun fuel!1 () Int)
(assert (=> start!10 (> fuel!1 10)))
(assert (=> start!10 true))
(set-info :status sat)
(check-sat)
(get-model)
