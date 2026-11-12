; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/BitsTricksSlow.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_BV)
(declare-fun start!89 () Bool)
(assert start!89)
(declare-fun indexFromLeft!1 () (_ BitVec 32))
(assert (=> start!89 (and (bvsle #b00000000000000000000000000000000 indexFromLeft!1) (bvslt indexFromLeft!1 #b00000000000000000000000000100000) (let ((n!23 (bvsub #b00000000000000000000000000011111 indexFromLeft!1))) (or (bvslt n!23 #b00000000000000000000000000000000) (bvsge n!23 #b00000000000000000000000000100000))))))
(assert (=> start!89 true))
(set-info :status unsat)
(check-sat)
