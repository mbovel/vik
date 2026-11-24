; From stp: overflow-tests/bvuaddo_equiv.smt2 (MIT, LICENSES/stp.txt)
; RUN: %solver %s | %OutputCheck %s
(set-logic QF_BV)
(declare-fun x () (_ BitVec 8))
(declare-fun y () (_ BitVec 8))
; bvuaddo(x,y) iff the unsigned sum wraps below x.
(assert (not (= (bvuaddo x y) (bvult (bvadd x y) x))))
; CHECK-NEXT: ^unsat
(set-info :status unsat)
(check-sat)
(exit)
