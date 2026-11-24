; From yices2: mcsat/lra/assumptions/test10m.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
(set-option :produce-unsat-model-interpolants true)
(set-logic QF_LIA)

(declare-const x Int)
(declare-const y Int)

(assert (= (* 2 y) x))

(set-info :status unsat)
(check-sat-assuming-model (x) (1))
(get-unsat-model-interpolant)

(set-info :status unsat)
(check-sat-assuming-model (x y) (2 2))
(get-unsat-model-interpolant)

(set-info :status unsat)
(check-sat-assuming-model (x y) (1 1))
(get-unsat-model-interpolant)

