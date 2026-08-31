; From z3test: t16.smt2 (MIT, LICENSES/z3test.txt)

; Copyright (c) 2015 Microsoft Corporation

(set-option :produce-models true)
(set-info :status sat)
(check-sat)
(eval (bv2int (bvadd #x0a #xf0)))