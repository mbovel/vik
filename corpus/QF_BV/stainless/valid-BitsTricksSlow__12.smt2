; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/BitsTricksSlow.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_BV)
(declare-fun start!51 () Bool)
(assert start!51)
(declare-fun i!1 () (_ BitVec 32))
(declare-fun n!28 () (_ BitVec 32))
(assert (=> start!51 (and (bvsle #b00000000000000000000000000000000 i!1) (bvslt i!1 #b00000000000000000000000000100000) (bvsle #b00000000000000000000000000000000 n!28) (bvslt n!28 #b00000000000000000000000000100000) (bvslt i!1 #b00000000000000000000000000000000))))
(assert (=> start!51 true))
(set-info :status unsat)
(check-sat)
