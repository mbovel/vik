; From cvc5: regress0/push-pop/simple_unsat_cores.smt2 (modified BSD, LICENSES/cvc5.txt)
; COMMAND-LINE: --incremental
; EXPECT: unsat
; EXPECT: unsat
(set-logic UF)
(push 1)
(assert false)
(set-info :status unsat)
(check-sat)
(pop 1)
(assert false)
(set-info :status unsat)
(check-sat)
