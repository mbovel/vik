; From yices2: mcsat/bv/assumptions/test06m.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
(set-option :produce-unsat-model-interpolants true)

(set-logic QF_BV)

(declare-const x (_ BitVec 8))
(declare-const y (_ BitVec 8))
(declare-const z (_ BitVec 8))

(assert (= (bvadd x (_ bv1 8)) (_ bv1 8)))
(set-info :status sat)
(check-sat)

(push 1)

(assert (= (bvadd y (_ bv1 8)) (bvadd x (_ bv1 8))))
(set-info :status sat)
(check-sat)

(push 1)

(assert (= z (bvadd x y)))

(set-info :status unsat)
(check-sat-assuming-model (z) ((_ bv1 8)))
(get-unsat-model-interpolant)

(pop 1)
(pop 1)
