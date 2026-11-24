; From yices2: sublogic_ufbv_pure_bv.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
; logic: QF_BV, the smallest the script fits, for QF_UFBV (prune logic)
(set-logic QF_BV)
; No uninterpreted function: bit-vector solver without the egraph
(declare-fun x () (_ BitVec 8))
(assert (= (bvadd x #x01) #x00))
(set-info :status sat)
(check-sat)
(get-value (x))
