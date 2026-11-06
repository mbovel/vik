; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/BitVectorBigIntConversions.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_BV)
(declare-fun start!13 () Bool)
(assert start!13)
(declare-fun x!11 () (_ BitVec 32))
(assert (=> start!13 (not (= (>= (ite (bvslt x!11 #b00000000000000000000000000000000) (- (bv2nat x!11) 4294967296) (bv2nat x!11)) 0) (bvsge x!11 #b00000000000000000000000000000000)))))
(assert (=> start!13 true))
(set-info :status unsat)
(check-sat)
