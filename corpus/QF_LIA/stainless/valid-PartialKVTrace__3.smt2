; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/PartialKVTrace.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_LIA)
(declare-fun start!8 () Bool)
(assert start!8)
(declare-fun fuel!1 () Int)
(assert (=> start!8 (and (> fuel!1 10) (< fuel!1 0))))
(assert (=> start!8 true))
(set-info :status unsat)
(check-sat)
