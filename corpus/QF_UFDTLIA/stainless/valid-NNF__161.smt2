; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/NNF.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!1021 () Bool)
(assert start!1021)
(declare-datatypes ((Formula!157 0))(
  ( (Not!157 (f!831 Formula!157)) (Implies!157 (lhs!1103 Formula!157) (rhs!1103 Formula!157)) (And!157 (lhs!1104 Formula!157) (rhs!1104 Formula!157)) (Or!157 (lhs!1105 Formula!157) (rhs!1105 Formula!157)) (Literal!157 (id!237 Int)) )
))
(declare-fun formula!2 () Formula!157)
(get-info :version)
(assert (=> start!1021 false))
(assert (=> start!1021 true))
(set-info :status unsat)
(check-sat)
