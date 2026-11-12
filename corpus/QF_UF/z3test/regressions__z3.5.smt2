; From z3test: z3.5.smt2 (MIT, LICENSES/z3test.txt)
; logic: QF_UF, the smallest the script fits, which set none (prune logic)
(set-logic QF_UF)

; Copyright (c) 2015 Microsoft Corporation
(set-option :auto-config true)
(set-option :produce-models true)

(declare-const a Bool)
(declare-const b Bool)
(define-fun demorgan () Bool
    (= (and a b) (not (or (not a) (not b)))))
(assert (not demorgan))
(set-info :status unsat)
(check-sat)
