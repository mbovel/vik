; From bitwuzla: solver/bv/regsmod1.smt2 (MIT, LICENSES/bitwuzla.txt)
(set-info :status unsat)
(set-logic QF_BV)
(assert (not (= (_ bv0 11) (bvsmod (_ bv0 11) ((_ sign_extend 8) (_ bv4 3))))))
(check-sat)

