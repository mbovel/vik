; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/PartialSplit.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_DT)
(declare-fun start!2102 () Bool)
(assert start!2102)
(declare-datatypes ((P!68 0))(
  ( (Charlie!68) (Bob!68) (Alice!68) )
))
(declare-datatypes ((tuple2!20 0))(
  ( (tuple2!21 (_1!10 P!68) (_2!10 P!68)) )
))
(declare-fun x$1!0 () tuple2!20)
(assert (=> start!2102 (not (= (_1!10 x$1!0) (_2!10 x$1!0)))))
(assert (=> start!2102 true))
(set-info :status sat)
(check-sat)
(get-model)
