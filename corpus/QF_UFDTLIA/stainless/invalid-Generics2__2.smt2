; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/Generics2.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!9 () Bool)
(assert start!9)
(declare-datatypes ((T!14 0))(
  ( (T!15 (val!1 Int)) )
))
(declare-datatypes ((List!4 0))(
  ( (Cons!4 (head!8 T!14) (tail!8 List!4)) (Nil!4) )
))
(declare-fun l!3 () List!4)
(get-info :version)
(assert (=> start!9 false))
(declare-fun e!16 () Bool)
(assert (=> start!9 e!16))
(declare-fun b!26 () Bool)
(declare-fun tp_is_empty!3 () Bool)
(declare-fun tp!8 () Bool)
(assert (=> b!26 (= e!16 (and tp_is_empty!3 tp!8))))
(assert (= (and start!9 ((_ is Cons!4) l!3)) b!26))
(set-info :status unsat)
(check-sat-assuming ((not b!26) tp_is_empty!3))
(set-info :status unsat)
(check-sat)
