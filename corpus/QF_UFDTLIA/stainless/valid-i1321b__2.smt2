; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/i1321b.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!5 () Bool)
(assert start!5)
(assert (=> start!5 false))
(declare-datatypes ((MyInt!2 0))(
  ( (MyIntExt!1 (__x!3 Int)) (PosInt!2 (v!35 Int)) )
))
(declare-fun lt!2 () MyInt!2)
(declare-fun thiss!5 () MyInt!2)
(assert (=> start!5 (= lt!2 thiss!5)))
(get-info :version)
(assert (=> start!5 ((_ is MyIntExt!1) lt!2)))
(set-info :status unsat)
(check-sat)
