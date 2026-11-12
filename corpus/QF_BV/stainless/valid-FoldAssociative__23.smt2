; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/FoldAssociative.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_BV)
(declare-fun start!107 () Bool)
(assert start!107)
(declare-fun x!13 () (_ BitVec 32))
(declare-fun s!5 () (_ BitVec 32))
(assert (=> start!107 (and (= (bvand x!13 #b10000000000000000000000000000000) (bvand s!5 #b10000000000000000000000000000000)) (not (= (bvand x!13 #b10000000000000000000000000000000) (bvand (bvadd x!13 s!5) #b10000000000000000000000000000000))))))
(assert (=> start!107 true))
(set-info :status sat)
(check-sat)
(get-model)
