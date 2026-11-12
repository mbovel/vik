; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/BitsTricksSlow.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_BV)
(declare-fun start!39 () Bool)
(assert start!39)
(declare-fun n!40 () (_ BitVec 32))
(assert (=> start!39 (and (bvsge n!40 #b00000000000000000000000000000000) (bvslt n!40 #b00000000000000000000000000100000) (let ((rhs!7 (bvsub #b00000000000000000000000000100000 n!40))) (or (bvslt rhs!7 #b00000000000000000000000000000000) (bvsgt rhs!7 #b00000000000000000000000000100000))))))
(assert (=> start!39 true))
(set-info :status unsat)
(check-sat)
