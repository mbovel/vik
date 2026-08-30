; From cvc5: regress0/parser/declarefun-emptyset-uf.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: sat
(set-logic QF_UF)
(declare-sort T 0)
(declare-fun union () T)
(declare-fun set.empty () T)
(assert (= union set.empty))
(set-info :status sat)
(check-sat)
