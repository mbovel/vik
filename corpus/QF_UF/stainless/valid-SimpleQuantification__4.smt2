; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/SimpleQuantification.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UF)
(declare-fun start!9 () Bool)
(assert start!9)
(declare-fun b_free!9 () Bool)
(declare-fun b_next!9 () Bool)
(assert (=> start!9 (= b_free!9 (not b_next!9))))
(declare-fun tp!14 () Bool)
(declare-fun b_and!31 () Bool)
(assert (=> start!9 (= tp!14 b_and!31)))
(assert (=> start!9 false))
(assert (=> start!9 tp!14))
(assert (=> start!9 true))
(set-info :status unsat)
(check-sat-assuming (b_and!31 (not b_next!9)))
