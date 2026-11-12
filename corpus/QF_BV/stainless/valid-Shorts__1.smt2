; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/Shorts.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_BV)
(declare-fun start!3 () Bool)
(assert start!3)
(declare-fun lt!11 () (_ BitVec 32))
(declare-fun c!1 () (_ BitVec 16))
(assert (=> start!3 (= lt!11 ((_ sign_extend 16) c!1))))
(declare-fun lt!10 () (_ BitVec 32))
(declare-fun b!9 () (_ BitVec 16))
(assert (=> start!3 (= lt!10 ((_ sign_extend 16) b!9))))
(assert (=> start!3 (and (or (not (= (bvand lt!10 #b10000000000000000000000000000000) (bvand lt!11 #b10000000000000000000000000000000))) (= (bvand lt!10 #b10000000000000000000000000000000) (bvand (bvadd lt!10 lt!11) #b10000000000000000000000000000000))) (let ((res!2 (bvadd lt!10 lt!11))) (or (bvsgt #b11111111111111110000000000000000 res!2) (bvsgt res!2 #b00000000000000001111111111111110))))))
(assert (=> start!3 true))
(set-info :status unsat)
(check-sat)
