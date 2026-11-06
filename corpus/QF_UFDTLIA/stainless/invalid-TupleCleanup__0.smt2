; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/TupleCleanup.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!2 () Bool)
(assert start!2)
(declare-datatypes ((List!4 0))(
  ( (Nil!2) (Cons!2 (h!66 Int) (t!77 List!4)) )
))
(declare-fun xs!29 () List!4)
(get-info :version)
(assert (=> start!2 (not ((_ is Nil!2) xs!29))))
(assert (=> start!2 true))
(set-info :status sat)
(check-sat)
(get-model)
