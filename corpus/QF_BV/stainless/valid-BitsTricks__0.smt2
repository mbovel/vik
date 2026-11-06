; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/BitsTricks.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_BV)
(declare-fun start!1 () Bool)
(assert start!1)
(declare-fun n!4 () (_ BitVec 32))
(assert (=> start!1 (and (bvsge n!4 #b00000000000000000000000000000000) (bvslt n!4 #b00000000000000000000000000100000) (bvsgt n!4 #b00000000000000000000000000100000))))
(assert (=> start!1 true))
(set-info :status unsat)
(check-sat)
