; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/Longs.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_BV)
(declare-fun start!11 () Bool)
(assert start!11)
(declare-fun j!1 () (_ BitVec 32))
(assert (=> start!11 (and (bvsle #b00000000000000000000000000000000 j!1) (bvslt j!1 #b00000000000000000000000000100000) (let ((rhs!4 ((_ sign_extend 32) j!1))) (and (bvsge rhs!4 #b0000000000000000000000000000000000000000000000000000000000000000) (bvsle rhs!4 #b0000000000000000000000000000000000000000000000000000000001000000) (bvslt ((_ sign_extend 32) j!1) #b0000000000000000000000000000000000000000000000000000000000000000))))))
(assert (=> start!11 true))
(set-info :status unsat)
(check-sat)
