; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/Passes1.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_LIA)
(declare-fun start!3 () Bool)
(assert start!3)
(declare-fun n!1 () Int)
(assert (=> start!3 (and (>= n!1 0) (not (= n!1 (ite (= n!1 (- 42)) 42 (ite (= n!1 (- 1)) 1 (ite (= n!1 0) 0 (ite (= n!1 1) 1 (ite (= n!1 42) 42 n!1))))))))))
(assert (=> start!3 true))
(set-info :status unsat)
(check-sat)
