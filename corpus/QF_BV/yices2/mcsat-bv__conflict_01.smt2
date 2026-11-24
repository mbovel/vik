; From yices2: mcsat/bv/conflict_01.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
(set-logic QF_BV)

(declare-fun x () (_ BitVec 5))
(declare-fun y () (_ BitVec 5))
(declare-fun z () (_ BitVec 5))

(assert (or
  (bvsgt x y)
  (bvsgt x z)
))

(assert (=
  (concat x z)
  (concat y x)
))

(set-info :status unsat)
(check-sat)

(exit)
