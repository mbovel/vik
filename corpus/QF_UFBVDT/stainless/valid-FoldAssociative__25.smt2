; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/FoldAssociative.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFBVDT)
(declare-fun start!121 () Bool)
(assert start!121)
(declare-fun lt!65 () Bool)
(declare-datatypes ((List!15 0))(
  ( (Cons!15 (head!60 (_ BitVec 32)) (tail!60 List!15)) (Nil!15) )
))
(declare-fun l1!22 () List!15)
(declare-fun l2!22 () List!15)
(declare-fun lemma_reassociative_presplit!0 (List!15 List!15) Bool)
(assert (=> start!121 (= lt!65 (lemma_reassociative_presplit!0 l1!22 l2!22))))
(assert (=> start!121 false))
(assert (=> start!121 true))
(declare-fun bs!55 () Bool)
(assert (= bs!55 start!121))
(declare-fun m!293 () Bool)
(assert (=> bs!55 m!293))
(set-info :status unsat)
(check-sat-assuming ((not start!121)))
(set-info :status unsat)
(check-sat)
