; From opensmt: base/generic/define-fun2.smt2 (MIT, LICENSES/opensmt.txt)
(set-logic QF_LRA)
(declare-fun a () Real)
(declare-fun b () Real)

(define-fun foo () Real
    (+ a b 1))

(assert (> foo foo))

(set-info :status unsat)
(check-sat)

