; From yices2: sublogic_uf_kept.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
(set-logic QF_UFLIA)
; Uses an uninterpreted function: the egraph must be kept
(declare-fun f (Int) Int)
(declare-fun x () Int)
(assert (> (f x) 2))
(assert (< (f x) 4))
(set-info :status sat)
(check-sat)
