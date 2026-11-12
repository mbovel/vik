; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/IsInstanceOfInt.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFBVDTLIA)
(declare-fun start!1 () Bool)
(assert start!1)
(declare-datatypes ((Object!1 0))(
  ( (SignedBitvector32!1 (value!18 (_ BitVec 32))) (Open!1 (value!19 Int)) )
))
(declare-fun lt!4 () Object!1)
(declare-fun empty!4 () Object!1)
(assert (=> start!1 (= lt!4 empty!4)))
(assert (=> start!1 true))
(assert (=> start!1 false))
(declare-fun lt!5 () Object!1)
(assert (=> start!1 (= lt!5 empty!4)))
(get-info :version)
(assert (=> start!1 (and ((_ is SignedBitvector32!1) lt!4) (not ((_ is SignedBitvector32!1) lt!4)))))
(set-info :status unsat)
(check-sat)
