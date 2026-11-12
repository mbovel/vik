; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/BitsTricksSlow.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_BV)
(declare-fun start!99 () Bool)
(assert start!99)
(declare-fun indexFromLeft!1 () (_ BitVec 32))
(assert (=> start!99 (and (bvsle #b00000000000000000000000000000000 indexFromLeft!1) (bvslt indexFromLeft!1 #b00000000000000000000000000100000) (not (= #b00000000000000000000000000000000 (bvand indexFromLeft!1 #b10000000000000000000000000000000))) (not (= #b00000000000000000000000000000000 (bvand (bvsub #b00000000000000000000000000011111 indexFromLeft!1) #b10000000000000000000000000000000))))))
(assert (=> start!99 true))
(set-info :status unsat)
(check-sat)
