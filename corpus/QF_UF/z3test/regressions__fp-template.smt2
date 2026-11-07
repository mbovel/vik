; From z3test: fp-template.smt2 (MIT, LICENSES/z3test.txt)
; logic: QF_UF, the smallest the script fits, which set none (prune logic)
(set-logic QF_UF)

; Copyright (c) 2015 Microsoft Corporation
(set-info :status sat)
(set-info :source "Handcrafted by C.M. Wintersteiger")


(set-info :status sat)
(check-sat)
(set-info :status sat)
(check-sat-using smt)
(exit)
