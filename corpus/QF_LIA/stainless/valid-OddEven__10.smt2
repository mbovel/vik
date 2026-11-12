; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/OddEven.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_LIA)
(declare-fun start!21 () Bool)
(assert start!21)
(declare-fun n!16 () Int)
(assert (=> start!21 false))
(assert (=> start!21 true))
(set-info :status unsat)
(check-sat)
