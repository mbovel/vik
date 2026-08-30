; From cvc5: regress0/push-pop/bug821.smt2 (modified BSD, LICENSES/cvc5.txt)
; COMMAND-LINE: --incremental
; EXPECT: sat
(set-logic UF)
(push 1)
(declare-fun _substvar_4_ () Bool)
(assert _substvar_4_)
(assert _substvar_4_)
(set-info :status sat)
(check-sat)
