; From cvc5: regress0/preprocess/circuit-prop.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: unsat
; EXPECT: sat
; EXPECT: unsat
; EXPECT: sat
; EXPECT: unsat
; EXPECT: unsat
; EXPECT: unsat
; EXPECT: unsat
; logic: QF_UF, the smallest the script fits, for ALL (prune logic)

;;;;; iteEvalThen(true)
(set-logic QF_UF)
(declare-fun a () Bool)
(declare-fun b () Bool)
(declare-fun c () Bool)
(assert a)
(assert b)
(assert (not (ite a b c)))
(set-info :status unsat)
(check-sat)

(reset)

;;;;; iteEvalThen(false)
(set-logic QF_UF)
(declare-fun a () Bool)
(declare-fun b () Bool)
(declare-fun c () Bool)
(declare-fun d () Bool)
(assert a)
(assert (not b))
(assert (or (ite a b c) d))
(set-info :status sat)
(check-sat)

(reset)

;;;;; iteEvalElse(true)
(set-logic QF_UF)
(declare-fun a () Bool)
(declare-fun b () Bool)
(declare-fun c () Bool)
(assert (not a))
(assert c)
(assert (not (ite a b c)))
(set-info :status unsat)
(check-sat)

(reset)

;;;;; iteEvalElse(false)
(set-logic QF_UF)
(declare-fun a () Bool)
(declare-fun b () Bool)
(declare-fun c () Bool)
(declare-fun d () Bool)
(assert (not a))
(assert (not c))
(assert (or (ite a b c) d))
(set-info :status sat)
(check-sat)

(reset)

(set-logic QF_UF)
(declare-fun a () Bool)
(declare-fun b () Bool)
(declare-fun c () Bool)
(declare-fun d () Bool)
(assert a)
(assert b)
(assert (=> a c))
(assert (=> b (not c)))
(set-info :status unsat)
(check-sat)

(reset)

(set-logic QF_UF)
(assert false)
(set-info :status unsat)
(check-sat)

(reset)

(set-logic QF_UF)
(declare-fun x () Bool)
(declare-fun z () Bool)
(assert (= x z))
(assert (not x))
(assert z)
(set-info :status unsat)
(check-sat)

(reset)

(set-logic QF_UF)
(declare-fun x3 () Bool)
(declare-fun x9 () Bool)
(assert (not x3))
(assert (or x3 (and x9 x3)))
(set-info :status unsat)
(check-sat)
