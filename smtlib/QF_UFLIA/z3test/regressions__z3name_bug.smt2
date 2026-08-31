; From z3test: z3name_bug.smt2 (MIT, LICENSES/z3test.txt)
; EXPECT: sat
; EXPECT: ((define-fun ite!1 () Int 10) (define-fun p ((x!0 Int)) Bool (ite (= x!0 0) true (ite (= x!0 10) true true))))

; Copyright (c) 2015 Microsoft Corporation

(set-option :model.compact true)
(declare-fun p (Int) Bool)
;(declare-fun f (Int Real) Bool)

(assert (p (ite (p 1) 10 20)))
(assert (p 1))
(set-info :status sat)
(check-sat-using (then nnf smt))
(get-model)

