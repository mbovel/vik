; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/MatchExh.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!6 () Bool)
(assert start!6)
(declare-datatypes ((List!5 0))(
  ( (Nil!3) (Cons!3 (h!62 Int) (t!72 List!5)) )
))
(declare-fun l!108 () List!5)
(get-info :version)
(assert (=> start!6 (and (not ((_ is Nil!3) l!108)) (or (not (= (h!62 l!108) 0)) (not ((_ is Nil!3) (t!72 l!108)))) (not ((_ is Cons!3) (t!72 l!108))))))
(assert (=> start!6 true))
(set-info :status sat)
(check-sat)
(get-model)
