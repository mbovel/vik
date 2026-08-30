; From z3test: strategy-par-or-1.smt2 (MIT, LICENSES/z3test.txt)
;; Copyright (c) 2016 Microsoft Corporation 

(set-info :source |Written by D. B. Staple for GitHub issue #623.|)
(set-info :status sat)

(declare-fun n () Int)
(assert (not (< n 1)))
(set-info :status sat)
(check-sat-using (par-or qflia smt))
