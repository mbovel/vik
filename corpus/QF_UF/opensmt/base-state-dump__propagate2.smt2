; From opensmt: base/state-dump/propagate2.smt2 (MIT, LICENSES/opensmt.txt)
(set-logic QF_UF)
(declare-fun x1 () Bool)
(declare-fun x2 () Bool)
(declare-fun x3 () Bool)
(declare-fun x4 () Bool)
(declare-fun x5 () Bool)
(declare-fun x6 () Bool)

(assert (and
  (or (not x1) x2)
  (or (not x1) x3)
  (or (not x1) x4)
  (or x1 x2)
  (or x1 x3)
  (or x1 x4)
  (or (not x5) x6)
  (or x5 x6)))

(set-info :status sat)
(check-sat)

