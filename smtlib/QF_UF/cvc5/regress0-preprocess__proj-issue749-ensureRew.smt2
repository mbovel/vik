; From cvc5: regress0/preprocess/proj-issue749-ensureRew.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: sat
(set-logic QF_ABVFFLIA)
(declare-const x Bool)
(assert (= 0 (ite x 1 0)))
(set-info :status sat)
(check-sat)
