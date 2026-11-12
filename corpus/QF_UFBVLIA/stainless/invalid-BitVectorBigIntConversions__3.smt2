; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/BitVectorBigIntConversions.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFBVLIA)
(declare-fun start!7 () Bool)
(assert start!7)
(declare-fun i!1 () Int)
(assert (=> start!7 (not (= (ite (bvslt ((_ int2bv 32) i!1) #b00000000000000000000000000000000) (- (bv2nat ((_ int2bv 32) i!1)) 4294967296) (bv2nat ((_ int2bv 32) i!1))) i!1))))
(assert (=> start!7 true))
(set-info :status sat)
(check-sat)
(get-model)
