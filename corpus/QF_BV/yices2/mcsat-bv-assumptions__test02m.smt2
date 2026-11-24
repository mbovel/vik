; From yices2: mcsat/bv/assumptions/test02m.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
(set-option :produce-unsat-model-interpolants true)

(set-logic QF_BV)

(declare-const x (_ BitVec 8))
(declare-const y (_ BitVec 8))
(declare-const z (_ BitVec 8))

(assert (bvslt (bvadd x y z) (_ bv0 8)))

(set-info :status unsat)
(check-sat-assuming-model (x y z) ((_ bv0 8) (_ bv0 8) (_ bv0 8)))
(get-unsat-model-interpolant)

(assert (= z x))
(set-info :status unsat)
(check-sat-assuming-model (x y) ((_ bv0 8) (_ bv0 8)))
(get-unsat-model-interpolant)
