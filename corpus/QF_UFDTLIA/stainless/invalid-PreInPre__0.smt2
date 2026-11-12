; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/PreInPre.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!2 () Bool)
(assert start!2)
(declare-datatypes ((List!4 0))(
  ( (Cons!2 (h!65 Int) (t!79 List!4)) (Nil!2) )
))
(declare-fun t!73 () List!4)
(assert (=> start!2 (= t!73 Nil!2)))
(assert (=> start!2 true))
(set-info :status sat)
(check-sat)
(get-model)
