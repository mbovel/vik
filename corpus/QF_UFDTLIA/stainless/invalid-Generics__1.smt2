; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/Generics.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!5 () Bool)
(assert start!5)
(declare-datatypes ((T!12 0))(
  ( (T!13 (val!1 Int)) )
))
(declare-datatypes ((List!3 0))(
  ( (Cons!3 (head!7 T!12) (tail!7 List!3)) (Nil!3) )
))
(declare-fun l!1 () List!3)
(get-info :version)
(assert (=> start!5 ((_ is Nil!3) l!1)))
(declare-fun e!11 () Bool)
(assert (=> start!5 e!11))
(declare-fun b!16 () Bool)
(declare-fun tp_is_empty!3 () Bool)
(declare-fun tp!8 () Bool)
(assert (=> b!16 (= e!11 (and tp_is_empty!3 tp!8))))
(assert (= (and start!5 ((_ is Cons!3) l!1)) b!16))
(set-info :status sat)
(check-sat-assuming ((not b!16) tp_is_empty!3))
(get-model)
