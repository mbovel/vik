; From yices2: wd/issue220.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
(set-logic QF_BV)
(declare-const x (_ BitVec 5))
(assert
 (=
  (bvadd
   (bvmul (concat (_ bv64 5) x) (concat (_ bv64 5) x))
   (concat (_ bv64 5) x)
   (bvmul (concat (_ bv64 5) x) (concat (_ bv64 5) x)))
  
  (bvadd
   (concat (_ bv64 5) x)
   (bvmul (concat (_ bv64 5) x) (concat (_ bv64 5) x)))

  (concat (_ bv64 5) x)))

(set-info :status sat)
(check-sat)

