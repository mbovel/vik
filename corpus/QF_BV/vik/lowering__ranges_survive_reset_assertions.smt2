; Hand-written for VIK (this repository's license): a bit-vector constant keeps its range across
; `reset-assertions`. The range is an assertion of the lowering, and `reset-assertions` drops the
; assertions: the constants were unbounded integers afterwards, and a query unsat on its own answered
; sat (2026-09-04).
; The declarations are global, so that they stay over `reset-assertions`, which forgets the others (issue 246).
(set-option :global-declarations true)
(set-logic QF_BV)
(declare-const x (_ BitVec 8))
(declare-const y (_ BitVec 8))
(set-info :status sat)
(check-sat)
(reset-assertions)
(assert (bvuge x #x80))
(assert (bvult (bvashr x #x01) #x80))
(set-info :status unsat)
(check-sat)
(reset-assertions)
(assert (bvult (bvadd x y) x))
(assert (= y #x00))
(set-info :status unsat)
(check-sat)
(reset-assertions)
(assert (bvugt (bvmul x #x02) #xfe))
(set-info :status unsat)
(check-sat)
