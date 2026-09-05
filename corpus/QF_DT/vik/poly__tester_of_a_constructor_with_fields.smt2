; Hand-written for VIK (this repository's license): the tester of a parametric datatype's constructor
; that has fields. It was resolved by applying the constructor to no arguments, which failed its arity
; check, so `((_ is cons) l)` threw where the nullary `((_ is nil) l)` did not (issue 31, 2026-09-05).
(set-logic ALL)
(declare-datatypes ((Lst 1)) ((par (T) ((nil) (cons (hd T) (tl (Lst T)))))))
(declare-const l (Lst Int))
(declare-const m (Lst Bool))
(assert ((_ is cons) l))
(assert ((_ is cons) m))
(assert (= (hd l) 3))
(set-info :status sat)
(check-sat)
(assert ((_ is nil) l))
(set-info :status unsat)
(check-sat)
