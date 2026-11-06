; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/Bytes.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_BV)
(declare-fun start!9 () Bool)
(assert start!9)
(declare-fun b!8 () (_ BitVec 8))
(assert (=> start!9 (and (bvsge ((_ sign_extend 24) b!8) #b11111111111111111111111110000000) (bvsle ((_ sign_extend 24) b!8) #b00000000000000000000000001111111) (bvsge ((_ sign_extend 24) b!8) #b00000000000000000000000010000000))))
(assert (=> start!9 true))
(set-info :status unsat)
(check-sat)
