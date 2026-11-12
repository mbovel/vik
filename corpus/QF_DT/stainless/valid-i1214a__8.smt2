; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/i1214a.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_DT)
(declare-fun start!25 () Bool)
(assert start!25)
(declare-datatypes ((Ordinal!9 0))(
  ( (Nat!9) (Transfinite!9 (in!17 Ordinal!9)) )
))
(declare-fun thiss!2 () Ordinal!9)
(get-info :version)
(assert (=> start!25 false))
(assert (=> start!25 true))
(set-info :status unsat)
(check-sat)
