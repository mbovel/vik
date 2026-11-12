; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/CharCompare.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_BV)
(declare-fun start!1 () Bool)
(assert start!1)
(declare-fun c1!1 () (_ BitVec 16))
(declare-fun c2!1 () (_ BitVec 16))
(assert (=> start!1 (bvuge c1!1 c2!1)))
(assert (=> start!1 true))
(set-info :status sat)
(check-sat)
(get-model)
