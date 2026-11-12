; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/NothingTypeEncoding.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_BV)
(declare-fun start!7 () Bool)
(assert start!7)
(declare-fun arg!3 () (_ BitVec 32))
(assert (=> start!7 (and (bvsgt arg!3 #b00000000000000000000000000000000) (not (= arg!3 #b00000000000000000000000000000000)) (bvslt arg!3 #b00000000000000000000000000000000))))
(assert (=> start!7 true))
(set-info :status unsat)
(check-sat)
