; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/ParBalance.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!473 () Bool)
(assert start!473)
(declare-fun counter!22 () Int)
(declare-fun failed!1 () Bool)
(declare-datatypes ((List!49 0))(
  ( (Cons!49 (head!222 Int) (tail!205 List!49)) (Nil!49) )
))
(declare-fun list!94 () List!49)
(get-info :version)
(assert (=> start!473 (and (or (>= counter!22 0) failed!1) ((_ is Cons!49) list!94) (let ((c!817 (ite (= (head!222 list!94) 1) (+ counter!22 1) (ite (= (head!222 list!94) 2) (- counter!22 1) counter!22)))) (and (< c!817 0) (not failed!1) (>= c!817 0))))))
(assert (=> start!473 true))
(set-info :status unsat)
(check-sat)
