; Hand-written for VIK (this repository's license): a sort parameter bound to a bit-vector sort. A sort is
; written as symbols and numerals — `(_ BitVec 8)` — and the walk over one read the symbols alone, so
; instantiating a rank at a bit-vector raised a match error (2026-09-04).
(set-logic QF_UFBV)
(declare-sort-parameter X)
(declare-fun box (X) X)
(declare-const b (_ BitVec 8))
(declare-const c (_ BitVec 4))
(assert (= (box b) #x0f))
(assert (= (box c) #x7))
(set-info :status sat)
(check-sat)
(assert (distinct (box b) #x0f))
(set-info :status unsat)
(check-sat)
