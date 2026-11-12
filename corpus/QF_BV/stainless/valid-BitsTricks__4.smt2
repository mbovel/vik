; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/BitsTricks.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_BV)
(declare-fun start!9 () Bool)
(assert start!9)
(declare-fun x!16 () (_ BitVec 32))
(assert (=> start!9 (not (= (= (bvlshr x!16 #b00000000000000000000000000011111) #b00000000000000000000000000000001) (bvslt x!16 #b00000000000000000000000000000000)))))
(assert (=> start!9 true))
(set-info :status unsat)
(check-sat)
