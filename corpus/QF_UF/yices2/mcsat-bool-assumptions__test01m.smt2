; From yices2: mcsat/bool/assumptions/test01m.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
(set-logic QF_UF)

(declare-const x Bool)
(declare-const y Bool)
(declare-const z Bool)

(assert (or x y z))

(set-info :status sat)
(check-sat-assuming-model (x y) (false false))

(get-model)
