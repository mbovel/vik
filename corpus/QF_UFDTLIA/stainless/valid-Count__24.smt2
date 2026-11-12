; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/Count.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!172 () Bool)
(assert start!172)
(declare-datatypes ((List!28 0))(
  ( (Nil!26) (Cons!26 (h!131 Int) (t!186 List!28)) )
))
(declare-fun l!115 () List!28)
(get-info :version)
(assert (=> start!172 (and (not ((_ is Nil!26) l!115)) (= l!115 Nil!26))))
(assert (=> start!172 true))
(set-info :status unsat)
(check-sat)
