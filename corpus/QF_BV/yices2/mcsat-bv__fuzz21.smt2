; From yices2: mcsat/bv/fuzz21.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
(set-logic QF_BV)
(declare-fun x () (_ BitVec 32))
(declare-fun y () (_ BitVec 8))
(declare-fun z () (_ BitVec 8))
(declare-fun w () (_ BitVec 32))

(assert 
  (let 
    ((?v_0 ((_ extract 7 0) (bvashr x ((_ zero_extend 24) (_ bv8 8)))))) 
    (and
      (= x (bvor (bvshl (bvor (bvshl w (_ bv8 32)) ((_ zero_extend 24) z)) (_ bv8 32)) ((_ zero_extend 24) y))) 
      (bvule (_ bv48 8) ?v_0) 
      (bvule (_ bv48 8) y)
    )
  )
)

(set-info :status sat)
(check-sat)
(exit)
