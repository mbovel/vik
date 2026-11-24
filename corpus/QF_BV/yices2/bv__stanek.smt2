; From yices2: bv/stanek.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
(set-option :produce-models true)
(set-logic QF_BV)

(declare-fun q () (_ BitVec 16))
(declare-fun q1 () (_ BitVec 16))
(declare-fun q2 () (_ BitVec 16))

(define-fun bitxor ((x (_ BitVec 16))) (_ BitVec 16)
   (bvxor (bvshl x #x0008) x))

(assert (= q1 #x4200))
(assert (= q2 (bvxor (bvshl q1 #x0008) q1)))
(assert (= q (bitxor q1)))

; GOAL
(set-info :status sat)
(check-sat)
(get-value (q1 q2 q))
