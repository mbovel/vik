; From yices2: bv/issue146.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
(set-logic QF_BV)
(declare-fun r1 () (_ BitVec 10))
(declare-fun i () (_ BitVec 10))
(declare-fun j () (_ BitVec 10))
(declare-fun k () (_ BitVec 10))

(assert (= (bvadd i j (bvmul (_ bv1023 10) k)) (_ bv0 10)))
;; (assert (= (bvadd i j) k))

;; (assert (= (bvadd k (bvmul (_ bv1023 10) j)) r1))
(assert (= (bvsub k j) r1))

(assert (= (bvadd (_ bv1 10) j (bvmul (_ bv1023 10) k)) (_ bv0 10)))
;; (assert (= (bvadd (_ bv1 10) j) k))

(assert (not (= (bvmul (_ bv4 10) r1) (bvmul (_ bv4 10) i))))

(set-info :status unsat)
(check-sat)

