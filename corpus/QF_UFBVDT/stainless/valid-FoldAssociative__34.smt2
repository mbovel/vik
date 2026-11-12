; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/FoldAssociative.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFBVDT)
(declare-fun start!157 () Bool)
(assert start!157)
(declare-fun count!2 () (_ BitVec 32))
(declare-datatypes ((List!20 0))(
  ( (Cons!20 (head!65 (_ BitVec 32)) (tail!65 List!20)) (Nil!20) )
))
(declare-fun list!13 () List!20)
(get-info :version)
(assert (=> start!157 (and (bvsge count!2 #b00000000000000000000000000000000) ((_ is Cons!20) list!13) (bvsgt count!2 #b00000000000000000000000000000000) (bvslt (bvsub count!2 #b00000000000000000000000000000001) #b00000000000000000000000000000000))))
(assert (=> start!157 true))
(set-info :status unsat)
(check-sat)
