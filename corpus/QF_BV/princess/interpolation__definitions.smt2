; From princess: interpolation/definitions.smt2 (BSD, LICENSES/princess.txt)
(set-logic ALL)
(set-option :produce-interpolants true)

(declare-fun x () (_ BitVec 32))
(declare-fun y () Int)

(define-fun p () Bool (> y (ubv_to_int x)))
(define-fun q () Bool (< y 0))

(assert p)
(assert q)

(set-info :status unsat)
(check-sat)
(get-interpolants)