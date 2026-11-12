; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/MyTuple3.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFBVDT)
(declare-fun start!1 () Bool)
(assert start!1)
(declare-datatypes ((tuple2!0 0))(
  ( (tuple2!1 (_1!0 (_ BitVec 32)) (_2!0 (_ BitVec 32))) )
))
(declare-fun t!2 () tuple2!0)
(assert (=> start!1 (or (bvsle (_1!0 t!2) #b00000000000000000000000000000000) (bvsle (_2!0 t!2) #b00000000000000000000000000000001))))
(assert (=> start!1 true))
(set-info :status sat)
(check-sat)
(get-model)
