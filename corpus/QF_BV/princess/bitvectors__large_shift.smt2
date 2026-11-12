; From princess: bitvectors/large_shift.smt2 (BSD, LICENSES/princess.txt)
; logic: QF_BV, the smallest the script fits, which set none (prune logic)
(set-logic QF_BV)

(declare-const x (_ BitVec 128))

(assert (not (= (_ bv0 128) (bvlshr x (_ bv999999999999 128)))))
(set-info :status unsat)
(check-sat)
