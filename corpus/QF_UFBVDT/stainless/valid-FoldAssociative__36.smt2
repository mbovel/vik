; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/FoldAssociative.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFBVDT)
(declare-fun start!163 () Bool)
(assert start!163)
(declare-fun count!2 () (_ BitVec 32))
(declare-datatypes ((List!22 0))(
  ( (Cons!22 (head!67 (_ BitVec 32)) (tail!67 List!22)) (Nil!22) )
))
(declare-fun list!13 () List!22)
(get-info :version)
(assert (=> start!163 (and (bvsge count!2 #b00000000000000000000000000000000) ((_ is Cons!22) list!13) (bvsgt count!2 #b00000000000000000000000000000000) (not (= (bvand count!2 #b10000000000000000000000000000000) #b00000000000000000000000000000000)) (not (= (bvand count!2 #b10000000000000000000000000000000) (bvand (bvsub count!2 #b00000000000000000000000000000001) #b10000000000000000000000000000000))))))
(assert (=> start!163 true))
(set-info :status unsat)
(check-sat)
