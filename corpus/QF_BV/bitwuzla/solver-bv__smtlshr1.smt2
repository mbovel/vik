; From bitwuzla: solver/bv/smtlshr1.smt2 (MIT, LICENSES/bitwuzla.txt)
(set-logic QF_BV)
(set-info :status sat)
(assert (= (bvlshr (_ bv0 1) (_ bv0 1)) (_ bv0 1)))
(assert (= (bvlshr (_ bv0 1) (_ bv1 1)) (_ bv0 1)))
(assert (= (bvlshr (_ bv1 1) (_ bv0 1)) (_ bv1 1)))
(assert (= (bvlshr (_ bv1 1) (_ bv1 1)) (_ bv0 1)))
(check-sat)

