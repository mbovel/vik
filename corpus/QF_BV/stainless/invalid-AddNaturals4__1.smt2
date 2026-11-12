; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/AddNaturals4.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_BV)
(declare-fun start!3 () Bool)
(assert start!3)
(declare-fun x!10 () (_ BitVec 32))
(declare-fun y!3 () (_ BitVec 32))
(assert (=> start!3 (and (bvsge x!10 #b00000000000000000000000000000000) (bvsge y!3 #b00000000000000000000000000000000) (= (bvand x!10 #b10000000000000000000000000000000) (bvand y!3 #b10000000000000000000000000000000)) (not (= (bvand x!10 #b10000000000000000000000000000000) (bvand (bvadd x!10 y!3) #b10000000000000000000000000000000))))))
(assert (=> start!3 true))
(set-info :status sat)
(check-sat)
(get-model)
