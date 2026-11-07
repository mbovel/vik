; From cvc5: regress0/push-pop/incremental-subst-bug.cvc.smt2 (modified BSD, LICENSES/cvc5.txt)
; COMMAND-LINE: --incremental
; EXPECT: sat
; EXPECT: unsat
; EXPECT: unsat
; EXPECT: sat
; EXPECT: sat
; EXPECT: sat
; EXPECT: sat
; logic: QF_UF, the smallest the script fits, for ALL (prune logic)
(set-logic QF_UF)
(declare-sort U 0)
(declare-fun x () U)
(declare-fun y () U)
(set-info :status sat)
(check-sat-assuming ( (not (= x y)) ))
(assert (= x y))
(set-info :status unsat)
(check-sat-assuming ( (not (= x y)) ))
(declare-fun z () U)
(push 1)
(set-info :status unsat)
(check-sat-assuming ( (not (= x y)) ))
(set-info :status sat)
(check-sat-assuming ( (not (= x z)) ))
(set-info :status sat)
(check-sat-assuming ( (not (= z x)) ))
(set-info :status sat)
(check-sat-assuming ( (= z x) ))
(pop 1)
(set-info :status sat)
(check-sat-assuming ( (= z x) ))
