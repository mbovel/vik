; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/AmortizedQueue.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFBVDT)
(declare-fun start!48 () Bool)
(assert start!48)
(declare-datatypes ((List!9 0))(
  ( (Cons!8 (head!84 (_ BitVec 32)) (tail!37 List!9)) (Nil!8) )
))
(declare-fun list!4 () List!9)
(get-info :version)
(assert (=> start!48 (and (not (= list!4 Nil!8)) (not ((_ is Cons!8) list!4)))))
(assert (=> start!48 true))
(set-info :status unsat)
(check-sat)
