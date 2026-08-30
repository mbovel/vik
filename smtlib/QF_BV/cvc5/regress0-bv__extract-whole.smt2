; From cvc5: regress0/bv/holes/extract-whole.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: unsat
(set-info :smt-lib-version 2.6)
(set-logic QF_BV)
(set-info :status unsat)

(declare-const x (_ BitVec 10))
(assert (not (= x ((_ extract 9 0) x))))
(set-info :status unsat)
(check-sat)
(exit)
