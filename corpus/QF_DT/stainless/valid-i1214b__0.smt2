; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/i1214b.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_DT)
(declare-fun start!1 () Bool)
(assert start!1)
(assert (=> start!1 false))
(declare-datatypes ((Ordinal!2 0))(
  ( (Nat!2) (Transfinite!2 (in!5 Ordinal!2)) )
))
(declare-fun lt!2 () Ordinal!2)
(declare-fun c!4 () Ordinal!2)
(assert (=> start!1 (= lt!2 c!4)))
(get-info :version)
(assert (=> start!1 ((_ is Transfinite!2) lt!2)))
(set-info :status unsat)
(check-sat)
