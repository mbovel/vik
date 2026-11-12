; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/ForallExistsTest.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_LIA)
(declare-fun start!53 () Bool)
(assert start!53)
(declare-fun lt!104 () Int)
(declare-fun lt!103 () Int)
(assert (=> start!53 (= lt!104 (+ lt!103 1))))
(declare-fun x!40 () Int)
(assert (=> start!53 (= lt!103 (+ x!40 x!40 x!40 x!40))))
(assert (=> start!53 (and (< lt!103 lt!104) (>= lt!103 (+ lt!104 1)))))
(assert (=> start!53 true))
(set-info :status unsat)
(check-sat)
