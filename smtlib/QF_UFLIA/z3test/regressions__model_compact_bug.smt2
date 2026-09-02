; From z3test: model_compact_bug.smt2 (MIT, LICENSES/z3test.txt)
; EXPECT: sat
; EXPECT: ((define-fun p ((x!0 Int)) Bool (ite (= x!0 3) true (ite (= x!0 1) true (ite (= x!0 2) true true)))))

; Copyright (c) 2015 Microsoft Corporation
(set-option :model.compact true)
(declare-fun p (Int) Bool)
(assert (p 1))
(assert (p 2))
(assert (p 3))
(set-info :status sat)
(check-sat-using smt)
(get-model)
