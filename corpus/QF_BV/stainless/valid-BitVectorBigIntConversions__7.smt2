; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/BitVectorBigIntConversions.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_BV)
(declare-fun start!15 () Bool)
(assert start!15)
(declare-fun x!10 () (_ BitVec 32))
(assert (=> start!15 (or (< (ite (bvslt x!10 #b00000000000000000000000000000000) (- (bv2nat x!10) 4294967296) (bv2nat x!10)) (- 2147483648)) (> (ite (bvslt x!10 #b00000000000000000000000000000000) (- (bv2nat x!10) 4294967296) (bv2nat x!10)) 2147483647))))
(assert (=> start!15 true))
(set-info :status unsat)
(check-sat)
