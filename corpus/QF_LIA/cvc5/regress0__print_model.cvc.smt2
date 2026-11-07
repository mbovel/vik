; From cvc5: regress0/print_model.cvc.smt2 (modified BSD, LICENSES/cvc5.txt)
; COMMAND-LINE: --produce-models
; EXPECT: sat
; EXPECT: ((define-fun s1 () Int 2) (define-fun s2 () Int 1))
; logic: QF_LIA, the smallest the script fits, for ALL (prune logic)
(set-logic QF_LIA)
(set-option :incremental true)
(set-option :produce-models true)
(declare-fun s1 () Int)
(declare-fun s2 () Int)
(assert (= s1 2))
(push 1)

(assert (and (> s2 0) (< s2 s1)))

(set-info :status sat)
(check-sat)
(get-model)

(pop 1)
