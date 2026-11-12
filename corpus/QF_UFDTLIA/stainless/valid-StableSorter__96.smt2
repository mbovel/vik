; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/StableSorter.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!768 () Bool)
(assert start!768)
(assert (=> start!768 false))
(declare-fun e!3630 () Bool)
(assert (=> start!768 e!3630))
(assert (=> start!768 true))
(declare-fun b!5309 () Bool)
(declare-fun tp_is_empty!185 () Bool)
(declare-fun tp!761 () Bool)
(assert (=> b!5309 (= e!3630 (and tp_is_empty!185 tp!761))))
(declare-datatypes ((T!301 0))(
  ( (T!302 (val!92 Int)) )
))
(declare-datatypes ((tuple2!138 0))(
  ( (tuple2!139 (_1!69 T!301) (_2!69 Int)) )
))
(declare-datatypes ((List!116 0))(
  ( (Cons!114 (h!291 tuple2!138) (t!664 List!116)) (Nil!114) )
))
(declare-fun l!216 () List!116)
(get-info :version)
(assert (= (and start!768 ((_ is Cons!114) l!216)) b!5309))
(set-info :status unsat)
(check-sat-assuming ((not b!5309) tp_is_empty!185))
(set-info :status unsat)
(check-sat)
