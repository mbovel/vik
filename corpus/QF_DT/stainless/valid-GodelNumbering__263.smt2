; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/GodelNumbering.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_DT)
(declare-fun start!1679 () Bool)
(assert start!1679)
(declare-datatypes ((Nat!265 0))(
  ( (Zero!265) (Succ!265 (n!1708 Nat!265)) )
))
(declare-fun n1!418 () Nat!265)
(declare-fun n2!429 () Nat!265)
(get-info :version)
(assert (=> start!1679 (and (or (not ((_ is Zero!265) n1!418)) (not ((_ is Zero!265) n2!429))) (or (not ((_ is Zero!265) n1!418)) (not ((_ is Succ!265) n2!429))) (or (not ((_ is Succ!265) n1!418)) (not ((_ is Zero!265) n2!429))) (or (not ((_ is Succ!265) n1!418)) (not ((_ is Succ!265) n2!429))))))
(assert (=> start!1679 true))
(set-info :status unsat)
(check-sat)
