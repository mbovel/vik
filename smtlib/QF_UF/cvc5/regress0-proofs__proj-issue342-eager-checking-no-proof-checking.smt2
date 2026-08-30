; From cvc5: regress0/proofs/proj-issue342-eager-checking-no-proof-checking.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: unsat
(set-logic ALL)
(set-option :produce-unsat-cores true)
(set-option :proof-check eager)
(assert false)
(set-info :status unsat)
(check-sat)