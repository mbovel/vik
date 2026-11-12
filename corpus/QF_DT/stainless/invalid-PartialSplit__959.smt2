; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/PartialSplit.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_DT)
(declare-fun start!2100 () Bool)
(assert start!2100)
(declare-datatypes ((P!67 0))(
  ( (Charlie!67) (Bob!67) (Alice!67) )
))
(declare-datatypes ((tuple2!18 0))(
  ( (tuple2!19 (_1!9 P!67) (_2!9 P!67)) )
))
(declare-fun x$1!0 () tuple2!18)
(assert (=> start!2100 (= (_1!9 x$1!0) (_2!9 x$1!0))))
(assert (=> start!2100 true))
(set-info :status sat)
(check-sat)
(get-model)
