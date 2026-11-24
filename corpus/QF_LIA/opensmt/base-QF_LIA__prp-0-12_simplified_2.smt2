; From opensmt: base/QF_LIA/prp-0-12_simplified_2.smt2 (MIT, LICENSES/opensmt.txt)
(set-logic QF_LIA)
(declare-fun b () Bool)
(set-info :status sat)
(assert
 (=
  (ite
   (= (ite b 1 0) 0)
   (ite b 1 0)
   (ite
    (= (ite b 1 0) 1)
    1
    0
   )
  )
  0
 )
)
(set-info :status sat)
(check-sat)
(exit)
