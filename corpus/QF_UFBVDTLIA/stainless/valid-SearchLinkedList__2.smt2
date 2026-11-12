; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/SearchLinkedList.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFBVDTLIA)
(declare-fun start!17 () Bool)
(assert start!17)
(declare-fun lt!11 () Bool)
(declare-datatypes ((List!4 0))(
  ( (Cons!4 (head!11 (_ BitVec 32)) (tail!11 List!4)) (Nil!4) )
))
(declare-fun list!45 () List!4)
(declare-fun i!1 () Int)
(declare-fun firstZero!0 (List!4) Int)
(assert (=> start!17 (= lt!11 (= (firstZero!0 list!45) i!1))))
(assert (=> start!17 false))
(assert (=> start!17 true))
(declare-fun bs!3 () Bool)
(assert (= bs!3 start!17))
(declare-fun m!31 () Bool)
(assert (=> bs!3 m!31))
(set-info :status unsat)
(check-sat-assuming ((not start!17)))
(set-info :status unsat)
(check-sat)
