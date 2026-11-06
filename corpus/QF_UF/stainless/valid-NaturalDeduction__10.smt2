; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/NaturalDeduction.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UF)
(declare-fun start!21 () Bool)
(assert start!21)
(declare-fun b_free!1 () Bool)
(declare-fun b_next!1 () Bool)
(assert (=> start!21 (= b_free!1 (not b_next!1))))
(declare-fun tp!2 () Bool)
(declare-fun b_and!1 () Bool)
(assert (=> start!21 (= tp!2 b_and!1)))
(assert (=> start!21 false))
(assert (=> start!21 tp!2))
(set-info :status unsat)
(check-sat-assuming (b_and!1 (not b_next!1)))
