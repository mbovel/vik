; From z3test: z3.2.smt2 (MIT, LICENSES/z3test.txt)
; logic: QF_UFLIA, the smallest the script fits, which set none (prune logic)
(set-logic QF_UFLIA)

; Copyright (c) 2015 Microsoft Corporation
(set-option :auto-config true)
(set-option :produce-models true)

(declare-const a Int)
(declare-fun f (Int Bool) Int)
(assert (> a 10))
(assert (< (f a true) 100))
(set-info :status sat)
(check-sat)
