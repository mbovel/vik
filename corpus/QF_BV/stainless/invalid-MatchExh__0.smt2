; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/MatchExh.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_BV)
(declare-fun start!2 () Bool)
(assert start!2)
(declare-fun i!24 () (_ BitVec 32))
(assert (=> start!2 (and (not (= i!24 #b00000000000000000000000000000001)) (not (= i!24 #b00000000000000000000000000000000)))))
(assert (=> start!2 true))
(set-info :status sat)
(check-sat)
(get-model)
