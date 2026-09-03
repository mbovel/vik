; Hand-written for VIK (this repository's license): two bit-vector fields and signed comparisons on their
; selectors — from the Scala 3 qualified-types checker (`range-54cec765e157`).
(set-logic QF_UFBVDT)
(declare-datatypes ((CheckedRange 0)) (((new_CheckedRange (CheckedRange.from@CheckedRange (_ BitVec 32)) (CheckedRange.until@CheckedRange (_ BitVec 32))))))
(declare-fun r () CheckedRange)
(declare-fun x0 () (_ BitVec 32))
(assert (= (new_CheckedRange (_ bv0 32) (_ bv10 32)) r))
(assert (bvslt (CheckedRange.from@CheckedRange r) (CheckedRange.until@CheckedRange r)))
(assert (and (bvsle (CheckedRange.from@CheckedRange r) x0) (bvslt x0 (CheckedRange.until@CheckedRange r))))
(assert (not (bvsle (_ bv0 32) x0)))
(set-info :status unsat)
(check-sat)
