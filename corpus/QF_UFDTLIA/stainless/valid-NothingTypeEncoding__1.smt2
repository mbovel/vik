; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/NothingTypeEncoding.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!3 () Bool)
(assert start!3)
(assert (=> start!3 false))
(declare-fun e!2 () Bool)
(assert (=> start!3 e!2))
(declare-fun b!2 () Bool)
(declare-fun tp_is_empty!1 () Bool)
(declare-fun tp!2 () Bool)
(assert (=> b!2 (= e!2 (and tp_is_empty!1 tp!2))))
(declare-datatypes ((T!24 0))(
  ( (T!25 (val!0 Int)) )
))
(declare-datatypes ((List!2 0))(
  ( (Nil!2) (Cons!2 (head!5 T!24) (tail!5 List!2)) )
))
(declare-fun self!1 () List!2)
(get-info :version)
(assert (= (and start!3 ((_ is Cons!2) self!1)) b!2))
(set-info :status unsat)
(check-sat-assuming ((not b!2) tp_is_empty!1))
(set-info :status unsat)
(check-sat)
