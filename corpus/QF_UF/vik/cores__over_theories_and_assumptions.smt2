; Hand-written for VIK (this repository's license): unsat cores over arrays, datatypes and bit-vectors,
; with a `check-sat-assuming` whose assumption is a Boolean the named assertions guard, and a level pushed
; and popped. Judged by z3 through bench `CoreCheck`.
; EXPECT: sat
; EXPECT: (error "unsat core is not available")
; EXPECT: unsat
; EXPECT: (rw ij)
; EXPECT: unsat
; EXPECT: (lc ln)
; EXPECT: unsat
; EXPECT: (lc hd4)
(set-logic ALL)
(set-option :produce-unsat-cores true)
(declare-const a (Array Int Int)) (declare-const i Int) (declare-const j Int)
(declare-datatype Lst ((nil) (cons (hd Int) (tl Lst))))
(declare-const l Lst) (declare-const x (_ BitVec 4)) (declare-const pij Bool)
(assert (! (= (select (store a i 1) j) 2) :named rw)) (assert (! (=> pij (= i j)) :named ij)) (assert (! (= l (cons 3 nil)) :named lc)) (assert (! (= (bvadd x #x1) #x0) :named bv))
(set-info :status sat)
(check-sat)
(get-unsat-core)
(set-info :status unsat)
(check-sat-assuming (pij))
(get-unsat-core)
(push 1)
(assert (! ((_ is nil) l) :named ln))
(set-info :status unsat)
(check-sat)
(get-unsat-core)
(pop 1)
(assert (! (= x #xf) :named xf)) (assert (! (= (hd l) 4) :named hd4))
(set-info :status unsat)
(check-sat)
(get-unsat-core)
