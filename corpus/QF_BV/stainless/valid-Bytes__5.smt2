; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/Bytes.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_BV)
(declare-fun start!11 () Bool)
(assert start!11)
(declare-fun b!8 () (_ BitVec 8))
(assert (=> start!11 (or (bvslt ((_ sign_extend 24) b!8) #b11111111111111111111111110000000) (bvsgt ((_ sign_extend 24) b!8) #b00000000000000000000000001111111))))
(assert (=> start!11 true))
(set-info :status unsat)
(check-sat)
