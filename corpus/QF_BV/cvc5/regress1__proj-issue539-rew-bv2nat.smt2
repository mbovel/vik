; From cvc5: regress1/proj-issue539-rew-bv2nat.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: unsat
(set-logic ALL)
(declare-const __ (_ BitVec 1))
(assert (< (ubv_to_int ((_ zero_extend 37) __)) (- (ubv_to_int ((_ zero_extend 37) __)))))
(set-info :status unsat)
(check-sat)
