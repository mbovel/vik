; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/NNF.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!957 () Bool)
(assert start!957)
(declare-datatypes ((Formula!143 0))(
  ( (Not!143 (f!817 Formula!143)) (Implies!143 (lhs!1061 Formula!143) (rhs!1061 Formula!143)) (And!143 (lhs!1062 Formula!143) (rhs!1062 Formula!143)) (Or!143 (lhs!1063 Formula!143) (rhs!1063 Formula!143)) (Literal!143 (id!223 Int)) )
))
(declare-fun formula!24 () Formula!143)
(get-info :version)
(assert (=> start!957 (and (not ((_ is And!143) formula!24)) (not ((_ is Or!143) formula!24)) (not ((_ is Implies!143) formula!24)) (or (not ((_ is Not!143) formula!24)) (not ((_ is And!143) (f!817 formula!24)))) (or (not ((_ is Not!143) formula!24)) (not ((_ is Or!143) (f!817 formula!24)))) (or (not ((_ is Not!143) formula!24)) (not ((_ is Implies!143) (f!817 formula!24)))) (or (not ((_ is Not!143) formula!24)) (not ((_ is Not!143) (f!817 formula!24)))) (or (not ((_ is Not!143) formula!24)) (not ((_ is Literal!143) (f!817 formula!24)))) (not ((_ is Literal!143) formula!24)))))
(assert (=> start!957 true))
(set-info :status unsat)
(check-sat)
