; From cvc5: regress0/proofs/unused-def1.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: unsat
;; define-const is not supported in Alethe
; DISABLE-TESTER: alethe
; logic: QF_UF, the smallest the script fits, for UF (prune logic)
(set-logic QF_UF)
(declare-const x Bool)
(define-const p Bool x)
(assert false)
(set-info :status unsat)
(check-sat)
