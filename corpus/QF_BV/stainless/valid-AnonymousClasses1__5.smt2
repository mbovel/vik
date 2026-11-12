; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/AnonymousClasses1.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_BV)
(declare-fun start!29 () Bool)
(assert start!29)
(declare-fun x!29 () (_ BitVec 32))
(assert (=> start!29 (and (bvsge x!29 #b00000000000000000000000000000000) (bvslt x!29 #b00000000000000000000000001100100) (= (bvand x!29 #b10000000000000000000000000000000) #b00000000000000000000000000000000) (not (= (bvand x!29 #b10000000000000000000000000000000) (bvand (bvadd x!29 #b00000000000000000000000000000001) #b10000000000000000000000000000000))))))
(assert (=> start!29 true))
(set-info :status unsat)
(check-sat)
