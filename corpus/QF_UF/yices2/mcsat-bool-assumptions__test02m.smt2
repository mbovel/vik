; From yices2: mcsat/bool/assumptions/test02m.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
(set-option :produce-unsat-model-interpolants true)
(set-logic QF_UF)

(declare-const x Bool)
(declare-const y Bool)
(declare-const z Bool)

(assert (or x y z))

(set-info :status unsat)
(check-sat-assuming-model (x y z) (false false false))

(get-unsat-model-interpolant)