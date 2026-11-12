; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/Acc.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFBVDT)
(declare-fun start!13 () Bool)
(assert start!13)
(declare-datatypes ((Acc!12 0))(
  ( (Acc!13 (checking!9 (_ BitVec 32)) (savings!9 (_ BitVec 32))) )
))
(declare-fun a1!3 () Acc!12)
(assert (=> start!13 (and (= (bvand (checking!9 a1!3) #b10000000000000000000000000000000) (bvand (savings!9 a1!3) #b10000000000000000000000000000000)) (not (= (bvand (checking!9 a1!3) #b10000000000000000000000000000000) (bvand (bvadd (checking!9 a1!3) (savings!9 a1!3)) #b10000000000000000000000000000000))))))
(assert (=> start!13 true))
(set-info :status sat)
(check-sat)
(get-model)
