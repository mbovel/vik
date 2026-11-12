; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/Peano.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_BV)
(declare-fun start!11 () Bool)
(assert start!11)
(declare-fun n!6 () (_ BitVec 32))
(assert (=> start!11 (and (bvsge n!6 #b00000000000000000000000000000000) (not (= n!6 #b00000000000000000000000000000000)) (bvslt (bvsub n!6 #b00000000000000000000000000000001) #b00000000000000000000000000000000))))
(assert (=> start!11 true))
(set-info :status unsat)
(check-sat)
