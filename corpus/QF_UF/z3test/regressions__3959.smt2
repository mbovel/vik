; From z3test: 3959.smt2 (MIT, LICENSES/z3test.txt)
(declare-const a Bool)
(set-info :status sat)
(check-sat-assuming (a (or true)))
