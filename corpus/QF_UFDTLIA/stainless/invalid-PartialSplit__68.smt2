; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/PartialSplit.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!138 () Bool)
(assert start!138)
(declare-datatypes ((P!2 0))(
  ( (Charlie!2) (Bob!2) (Alice!2) )
))
(declare-datatypes ((S!4 0))(
  ( (S!5 (who!4 P!2) (amount!4 Int)) )
))
(declare-datatypes ((List!4 0))(
  ( (Nil!2) (Cons!2 (h!69 S!4) (t!86 List!4)) )
))
(declare-fun size!3 (List!4) Int)
(assert (=> start!138 (not (= (size!3 Nil!2) (size!3 Nil!2)))))
(declare-fun bs!69 () Bool)
(assert (= bs!69 start!138))
(declare-fun m!1 () Bool)
(assert (=> bs!69 m!1))
(set-info :status unsat)
(check-sat-assuming ((not start!138)))
(set-info :status unsat)
(check-sat)
