; From cvc5: regress0/proofs/issue277-circuit-propagator.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: sat
; EXPECT: sat
; EXPECT: sat
; EXPECT: sat
; EXPECT: sat
; EXPECT: sat
(set-logic QF_UF)
(set-option :produce-proofs true)
(set-option :incremental true)
(declare-fun p () Bool)
(declare-fun q () Bool)

(push)
(assert (not (= p (not q))))
(assert p)
(set-info :status sat)
(check-sat)
(pop)

(push)
(assert (not (= (not q) p)))
(assert p)
(set-info :status sat)
(check-sat)
(pop)

(push)
(assert (not (= (not p) (not (not q)))))
(assert p)
(set-info :status sat)
(check-sat)
(pop)

(push)
(assert (not (= (not (not q)) (not p))))
(assert p)
(set-info :status sat)
(check-sat)
(pop)

(push)
(assert (not (= (not (not p)) (not (not (not q))))))
(assert p)
(set-info :status sat)
(check-sat)
(pop)

(push)
(assert (not (= (not (not (not q))) (not (not p)))))
(assert p)
(set-info :status sat)
(check-sat)
(pop)
