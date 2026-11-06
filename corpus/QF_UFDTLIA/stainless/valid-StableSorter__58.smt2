; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/StableSorter.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!488 () Bool)
(assert start!488)
(assert (=> start!488 false))
(declare-fun e!2438 () Bool)
(assert (=> start!488 e!2438))
(declare-fun b!3611 () Bool)
(declare-fun tp_is_empty!109 () Bool)
(declare-fun tp!471 () Bool)
(assert (=> b!3611 (= e!2438 (and tp_is_empty!109 tp!471))))
(declare-datatypes ((T!214 0))(
  ( (T!215 (val!54 Int)) )
))
(declare-datatypes ((List!63 0))(
  ( (Cons!61 (h!185 T!214) (t!434 List!63)) (Nil!61) )
))
(declare-fun l!0 () List!63)
(get-info :version)
(assert (= (and start!488 ((_ is Cons!61) l!0)) b!3611))
(set-info :status unsat)
(check-sat-assuming ((not b!3611) tp_is_empty!109))
(set-info :status unsat)
(check-sat)
