; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/Longs.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_BV)
(declare-fun start!23 () Bool)
(assert start!23)
(declare-fun y!3 () (_ BitVec 64))
(assert (=> start!23 (and (bvsge y!3 #b0000000000000000000000000000000000000000000000000000000000000000) (not (= y!3 #b0000000000000000000000000000000000000000000000000000000000000000)) (bvslt (bvsub y!3 #b0000000000000000000000000000000000000000000000000000000000000001) #b0000000000000000000000000000000000000000000000000000000000000000))))
(assert (=> start!23 true))
(set-info :status unsat)
(check-sat)
