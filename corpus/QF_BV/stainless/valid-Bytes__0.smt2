; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/Bytes.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_BV)
(declare-fun start!1 () Bool)
(assert start!1)
(declare-fun lt!5 () (_ BitVec 32))
(declare-fun c!1 () (_ BitVec 8))
(assert (=> start!1 (= lt!5 ((_ sign_extend 24) c!1))))
(declare-fun lt!4 () (_ BitVec 32))
(declare-fun b!9 () (_ BitVec 8))
(assert (=> start!1 (= lt!4 ((_ sign_extend 24) b!9))))
(assert (=> start!1 (and (= (bvand lt!4 #b10000000000000000000000000000000) (bvand lt!5 #b10000000000000000000000000000000)) (not (= (bvand lt!4 #b10000000000000000000000000000000) (bvand (bvadd lt!4 lt!5) #b10000000000000000000000000000000))))))
(assert (=> start!1 true))
(set-info :status unsat)
(check-sat)
