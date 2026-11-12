; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/FoldAssociative.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_BV)
(declare-fun start!93 () Bool)
(assert start!93)
(declare-fun x!61 () (_ BitVec 32))
(declare-fun x!9 () (_ BitVec 32))
(declare-fun s!2 () (_ BitVec 32))
(assert (=> start!93 (and (bvsge x!61 #b00000000000000000000000000000000) (= (bvand x!9 #b10000000000000000000000000000000) (bvand s!2 #b10000000000000000000000000000000)) (not (= (bvand x!9 #b10000000000000000000000000000000) (bvand (bvadd x!9 s!2) #b10000000000000000000000000000000))))))
(assert (=> start!93 true))
(set-info :status sat)
(check-sat)
(get-model)
