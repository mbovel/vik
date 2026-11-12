; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/BitsTricks.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_BV)
(declare-fun start!3 () Bool)
(assert start!3)
(declare-fun x!32 () (_ BitVec 32))
(declare-fun y!1 () (_ BitVec 32))
(assert (=> start!3 (not (= (bvxor (bvxor x!32 y!1) x!32) y!1))))
(assert (=> start!3 true))
(set-info :status unsat)
(check-sat)
