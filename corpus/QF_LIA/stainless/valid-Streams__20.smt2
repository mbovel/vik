; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/Streams.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_LIA)
(declare-fun start!45 () Bool)
(assert start!45)
(declare-fun n!17 () Int)
(assert (=> start!45 (and (>= n!17 0) (> n!17 0) (> (- n!17 1) 0) (not (= (- (- n!17 1) 1) (- n!17 2))))))
(assert (=> start!45 true))
(set-info :status unsat)
(check-sat)
