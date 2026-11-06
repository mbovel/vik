; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/i1321b.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!7 () Bool)
(assert start!7)
(assert (=> start!7 false))
(declare-datatypes ((MyInt!3 0))(
  ( (MyIntExt!2 (__x!4 Int)) (PosInt!3 (v!36 Int)) )
))
(declare-fun lt!5 () MyInt!3)
(declare-fun xyz!0 () MyInt!3)
(assert (=> start!7 (= lt!5 xyz!0)))
(get-info :version)
(assert (=> start!7 ((_ is PosInt!3) lt!5)))
(set-info :status unsat)
(check-sat)
