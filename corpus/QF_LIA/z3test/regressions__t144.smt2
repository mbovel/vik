; From z3test: t144.smt2 (MIT, LICENSES/z3test.txt)
; logic: QF_LIA, the smallest the script fits, which set none (prune logic)
(set-logic QF_LIA)

; Copyright (c) 2015 Microsoft Corporation



(declare-const x Int)
(define-fun b () Bool (> x 4))
(set-info :status sat)
(check-sat b)
(set-info :status sat)
(check-sat (not b))
(declare-const p Bool)
(define-fun b2 () Bool (not p))
(set-info :status sat)
(check-sat b2)
(set-info :status sat)
(check-sat (not b2))
(set-info :status sat)
(check-sat (not p))
