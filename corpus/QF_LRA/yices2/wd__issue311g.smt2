; From yices2: wd/issue311g.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
(set-logic QF_LRA)
(declare-const r Real)
(assert (= 1.0 (/ r 1 2 3)))
(set-info :status sat)
(check-sat)
(get-value (r))


