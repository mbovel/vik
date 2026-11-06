; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/BigArray.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_AUFBVDT)
(declare-fun start!1 () Bool)
(assert start!1)
(declare-datatypes ((array!3 0))(
  ( (array!4 (arr!1 (Array (_ BitVec 32) (_ BitVec 32))) (size!1 (_ BitVec 32))) )
))
(declare-fun a!1 () array!3)
(assert (=> start!1 (and (bvsge (size!1 a!1) #b00000000000000000000000000001010) (bvsge #b00000000000000000000000000000111 (size!1 a!1)))))
(declare-fun array_inv!1 (array!3) Bool)
(assert (=> start!1 (array_inv!1 a!1)))
(declare-fun bs!1 () Bool)
(assert (= bs!1 start!1))
(declare-fun m!1 () Bool)
(assert (=> bs!1 m!1))
(set-info :status unsat)
(check-sat-assuming ((not start!1)))
(set-info :status unsat)
(check-sat)
