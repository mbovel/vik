; From cvc5: regress0/preprocess/proj-issue749-ensureRew.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: sat
; logic: QF_LIA, the smallest the script fits, for QF_ABVFFLIA (prune logic)
(set-logic QF_LIA)
(declare-const x Bool)
(assert (= 0 (ite x 1 0)))
(set-info :status sat)
(check-sat)
