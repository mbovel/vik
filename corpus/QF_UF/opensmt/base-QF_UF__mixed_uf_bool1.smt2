; From opensmt: base/QF_UF/mixed_uf_bool1.smt2 (MIT, LICENSES/opensmt.txt)
(set-logic QF_UF)
(declare-fun c () Bool)
(declare-fun d () Bool)
(declare-sort U 0)
(declare-fun f (Bool) U)

(assert
  (not (=
   (f (not (and (or c d) (or c (not d)) (or (not c) d) (or (not c) (not d)))))
   (f true))))

(set-info :status unsat)
(check-sat)
