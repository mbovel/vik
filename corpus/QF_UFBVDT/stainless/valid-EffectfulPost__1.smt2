; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/EffectfulPost.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFBVDT)
(declare-fun start!3 () Bool)
(assert start!3)
(declare-datatypes ((Foo!4 0))(
  ( (Foo!5 (bar!8 (_ BitVec 32))) )
))
(declare-fun foo!10 () Foo!4)
(assert (=> start!3 (and (= (bvand (bar!8 foo!10) #b10000000000000000000000000000000) #b00000000000000000000000000000000) (not (= (bvand (bar!8 foo!10) #b10000000000000000000000000000000) (bvand (bvadd (bar!8 foo!10) #b00000000000000000000000000000001) #b10000000000000000000000000000000))))))
(assert (=> start!3 true))
(set-info :status sat)
(check-sat)
(get-model)
