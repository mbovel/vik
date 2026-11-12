; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/NaturalDeduction.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UF)
(declare-fun start!7 () Bool)
(assert start!7)
(declare-fun p!53 () Bool)
(declare-fun q!17 () Bool)
(declare-fun r!2 () Bool)
(assert (=> start!7 (and (or p!53 q!17) (or (not p!53) r!2) (or (not q!17) r!2) (not r!2))))
(assert (=> start!7 true))
(set-info :status unsat)
(check-sat)
