; From opensmt: base/QF_UF/iso_brn029_simplified_2.smt2 (MIT, LICENSES/opensmt.txt)
(set-logic QF_UF)
(declare-sort I 0)
(declare-fun op (I I) I)
(declare-fun e5 () I)
(declare-fun e4 () I)
(assert
 (and
  (= (op e4 e5) e4)
  (or
   (not (= (op e4 e5) e5))
   (or
    (= (op e4 e5) e5)
    (not (= (op e5 e4) e5))))))
(set-info :status sat)
(check-sat)
(exit)
