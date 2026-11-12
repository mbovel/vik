; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/ListOperations.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFBVDTLIA)
(declare-fun start!180 () Bool)
(assert start!180)
(declare-fun acc!4 () Int)
(declare-datatypes ((List!33 0))(
  ( (Cons!32 (head!159 (_ BitVec 32)) (tail!111 List!33)) (Nil!32) )
))
(declare-fun l!153 () List!33)
(get-info :version)
(assert (=> start!180 (and (>= acc!4 0) (not ((_ is Nil!32) l!153)) (< (+ acc!4 1) 0))))
(assert (=> start!180 true))
(set-info :status unsat)
(check-sat)
