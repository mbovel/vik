; From cvc5: regress0/proofs/assume-false-final-step-check.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: unsat
;; Companion to assume-false-final-step.smt2, which checks that the dummy step
;; is printed. This test has plain unsat expected output so that the cpc tester
;; applies to it, which checks the proof with ethos and hence covers that the
;; proof is accepted under --require-proof-of-false.
(set-logic ALL)
(assert false)
(set-info :status unsat)
(check-sat)
