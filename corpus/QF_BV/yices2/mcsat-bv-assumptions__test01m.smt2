; From yices2: mcsat/bv/assumptions/test01m.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
(set-logic QF_BV)

(declare-const x (_ BitVec 8))
(declare-const y (_ BitVec 8))
(declare-const z (_ BitVec 8))

(assert (bvslt (bvadd x y z) (_ bv0 8)))

(set-info :status sat)
(check-sat-assuming-model (x y) ((_ bv0 8) (_ bv0 8)))

(get-model)
