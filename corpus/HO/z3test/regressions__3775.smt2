; From z3test: 3775.smt2 (MIT, LICENSES/z3test.txt)
(assert (distinct (lambda ((a Int)) 0)))
(set-info :status sat)
(check-sat-using horn)