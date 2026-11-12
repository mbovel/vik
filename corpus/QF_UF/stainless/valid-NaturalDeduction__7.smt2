; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/NaturalDeduction.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UF)
(declare-fun start!15 () Bool)
(assert start!15)
(declare-fun p!58 () Bool)
(declare-fun q!20 () Bool)
(declare-fun r!3 () Bool)
(assert (=> start!15 (and (or (not p!58) q!20) (or (not q!20) r!3) p!58 (not r!3))))
(assert (=> start!15 true))
(set-info :status unsat)
(check-sat)
