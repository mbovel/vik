; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/FieldInheritance.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!1 () Bool)
(assert start!1)
(assert (=> start!1 false))
(declare-fun e!2 () Bool)
(assert (=> start!1 e!2))
(declare-fun b!6 () Bool)
(declare-fun tp_is_empty!1 () Bool)
(assert (=> b!6 (= e!2 tp_is_empty!1)))
(declare-fun b!7 () Bool)
(assert (=> b!7 (= e!2 tp_is_empty!1)))
(declare-fun b!8 () Bool)
(assert (=> b!8 (= e!2 tp_is_empty!1)))
(declare-datatypes ((A!11 0))(
  ( (A!12 (val!0 Int)) )
))
(declare-datatypes ((Foo!2 0))(
  ( (FooExt!1 (__x!3 Int) (weird!16 A!11) (thisIsIt!11 Int)) (Bar!2 (thisIsIt!12 Int) (weird!17 A!11)) (Baz!2 (weird!18 A!11)) )
))
(declare-fun f!1 () Foo!2)
(get-info :version)
(assert (= (and start!1 ((_ is FooExt!1) f!1)) b!6))
(assert (= (and start!1 ((_ is Bar!2) f!1)) b!7))
(assert (= (and start!1 ((_ is Baz!2) f!1)) b!8))
(set-info :status unsat)
(check-sat-assuming (tp_is_empty!1))
