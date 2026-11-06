; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/AddNaturals4.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_BV)
(declare-fun start!9 () Bool)
(assert start!9)
(declare-fun x!12 () (_ BitVec 32))
(declare-fun y!5 () (_ BitVec 32))
(assert (=> start!9 (and (bvsge x!12 #b00000000000000000000000000000000) (bvsge y!5 #b00000000000000000000000000000000) (bvslt x!12 #b00000000000011110100001001000000) (bvslt y!5 #b00000000000011110100001001000000) (or (not (= (bvand x!12 #b10000000000000000000000000000000) (bvand y!5 #b10000000000000000000000000000000))) (= (bvand x!12 #b10000000000000000000000000000000) (bvand (bvadd x!12 y!5) #b10000000000000000000000000000000))) (bvslt (bvadd x!12 y!5) #b00000000000000000000000000000000))))
(assert (=> start!9 true))
(set-info :status unsat)
(check-sat)
