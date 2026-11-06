; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/BitsTricksSlow.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_BV)
(declare-fun start!35 () Bool)
(assert start!35)
(declare-fun n!40 () (_ BitVec 32))
(assert (=> start!35 (and (bvsge n!40 #b00000000000000000000000000000000) (bvslt n!40 #b00000000000000000000000000100000) (let ((i!5 #b00000000000000000000000000011111)) (or (bvsgt #b00000000000000000000000000000000 i!5) (bvsge i!5 #b00000000000000000000000000100000) (bvsgt #b00000000000000000000000000000000 n!40))))))
(assert (=> start!35 true))
(set-info :status unsat)
(check-sat)
