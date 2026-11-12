; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/Bytes.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_BV)
(declare-fun start!13 () Bool)
(assert start!13)
(declare-fun i!1 () (_ BitVec 32))
(assert (=> start!13 (bvsge ((_ sign_extend 24) ((_ extract 7 0) i!1)) #b00000000000000000000000010000000)))
(assert (=> start!13 true))
(set-info :status unsat)
(check-sat)
