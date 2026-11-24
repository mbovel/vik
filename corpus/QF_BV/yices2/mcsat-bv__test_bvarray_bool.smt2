; From yices2: mcsat/bv/test_bvarray_bool.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
(set-logic QF_BV)
(set-info :status sat)

(declare-fun x () Bool)
(declare-fun y () Bool)

(assert (= #b11 
  (concat 
    (ite x #b0 #b1) 
    (ite y #b1 #b0)
  )
))

(set-info :status sat)
(check-sat)

(exit)

