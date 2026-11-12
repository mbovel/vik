; From cvc5: regress0/parser/bang-lex.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: unsat
; logic: QF_LIA, the smallest the script fits, for ALL (prune logic)
(set-logic QF_LIA)
(declare-fun !x () Int)
(assert (> !x 0))
(assert (< !x 0))
(set-info :status unsat)
(check-sat)
