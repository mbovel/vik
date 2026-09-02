; From z3test: z3.29.smt2 (MIT, LICENSES/z3test.txt)

; Copyright (c) 2015 Microsoft Corporation
(set-option :auto-config true)
(set-option :produce-models true)
(set-option :global-decls false) ;; tests are executed using SMTLIB2_COMPLIANT=false

(declare-const x Int)
(declare-const y Int)
(declare-const z Int)
(push)
(assert (= (+ x y) 10))
(assert (= (+ x (* 2 y)) 20))
(set-info :status sat)
(check-sat)
(pop) ; remove the two assertions
(push) 
(assert (= (+ (* 3 x) y) 10))
(assert (= (+ (* 2 x) (* 2 y)) 21))
(set-info :status unsat)
(check-sat)
(declare-const p Bool)
(pop)
(assert p) ; error, since declaration of p was removed from the stack
