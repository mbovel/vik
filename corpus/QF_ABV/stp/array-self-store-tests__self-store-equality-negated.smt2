; From stp: array-self-store-tests/self-store-equality-negated.smt2 (MIT, LICENSES/stp.txt)
; RUN: %solver -d %s | %OutputCheck %s
;
; Negated: the cell must differ from the value, which the model check
; reconstructs.
;
; CHECK: ^sat
(set-logic QF_ABV)
(declare-fun a () (Array (_ BitVec 8) (_ BitVec 8)))
(assert (not (= (store a #x03 #x2a) a)))
(set-info :status sat)
(check-sat)
(exit)
