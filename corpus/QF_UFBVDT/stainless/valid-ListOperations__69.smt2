; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/ListOperations.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFBVDT)
(declare-fun start!388 () Bool)
(assert start!388)
(declare-datatypes ((IntPair!6 0))(
  ( (IP!6 (fst!28 (_ BitVec 32)) (snd!28 (_ BitVec 32))) )
))
(declare-datatypes ((IntPairList!6 0))(
  ( (IPCons!6 (head!202 IntPair!6) (tail!154 IntPairList!6)) (IPNil!6) )
))
(declare-fun l!126 () IntPairList!6)
(get-info :version)
(assert (=> start!388 false))
(assert (=> start!388 true))
(set-info :status unsat)
(check-sat)
