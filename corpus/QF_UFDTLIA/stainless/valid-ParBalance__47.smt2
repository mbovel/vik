; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/ParBalance.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!447 () Bool)
(assert start!447)
(declare-datatypes ((tuple2!34 0))(
  ( (tuple2!35 (_1!17 Int) (_2!17 Int)) )
))
(declare-fun p!4 () tuple2!34)
(declare-datatypes ((List!44 0))(
  ( (Cons!44 (head!217 Int) (tail!200 List!44)) (Nil!44) )
))
(declare-fun list!110 () List!44)
(get-info :version)
(assert (=> start!447 (and (>= (_1!17 p!4) 0) (>= (_2!17 p!4) 0) (not ((_ is Cons!44) list!110)) (< (- (_1!17 p!4) (_2!17 p!4)) 0) (<= (_2!17 p!4) 0))))
(assert (=> start!447 true))
(set-info :status unsat)
(check-sat)
