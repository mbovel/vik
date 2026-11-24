; From yices2: mcsat/bool/assumptions/test03.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
(set-logic QF_UF)

(declare-const x Bool)
(declare-const y Bool)
(declare-const z Bool)

(assert (or x y z))
(assert (or x y (not z)))
(assert (or x (not y) z))
(assert (or x (not y) (not z)))

(push 1)
(set-info :status unsat)
(check-sat-assuming ((not x)))
(pop 1)
(set-info :status sat)
(check-sat-assuming (x))
