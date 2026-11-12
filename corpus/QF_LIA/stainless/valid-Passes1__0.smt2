; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/Passes1.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_LIA)
(declare-fun start!1 () Bool)
(assert start!1)
(declare-fun n!1 () Int)
(assert (=> start!1 (and (< n!1 0) (let ((res!2 (- n!1))) (or (< res!2 0) (not (= res!2 (ite (= n!1 (- 42)) 42 (ite (= n!1 (- 1)) 1 (ite (= n!1 0) 0 (ite (= n!1 1) 1 (ite (= n!1 42) 42 res!2))))))))))))
(assert (=> start!1 true))
(set-info :status unsat)
(check-sat)
