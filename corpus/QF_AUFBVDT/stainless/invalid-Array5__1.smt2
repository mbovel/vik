; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/Array5.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_AUFBVDT)
(declare-fun start!5 () Bool)
(assert start!5)
(declare-datatypes ((array!6 0))(
  ( (array!7 (arr!2 (Array (_ BitVec 32) (_ BitVec 32))) (size!2 (_ BitVec 32))) )
))
(declare-fun a!1 () array!6)
(assert (=> start!5 (and (bvsgt (size!2 a!1) #b00000000000000000000000000000000) (bvsge #b00000000000000000000000000000000 (size!2 a!1)))))
(declare-fun array_inv!2 (array!6) Bool)
(assert (=> start!5 (array_inv!2 a!1)))
(declare-fun bs!3 () Bool)
(assert (= bs!3 start!5))
(declare-fun m!3 () Bool)
(assert (=> bs!3 m!3))
(set-info :status unsat)
(check-sat-assuming ((not start!5)))
(set-info :status unsat)
(check-sat)
