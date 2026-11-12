; From cvc5: regress0/bool/issue1978.smt2 (modified BSD, LICENSES/cvc5.txt)
; COMMAND-LINE: --incremental
; EXPECT: sat
; EXPECT: unsat
; logic: QF_UF, the smallest the script fits, for ALL (prune logic)
(set-logic QF_UF)
(declare-fun _substvar_29_ () Bool)
(declare-fun _substvar_30_ () Bool)
(declare-fun _substvar_36_ () Bool)
(assert (= false (and _substvar_29_ _substvar_30_)))
(set-info :status sat)
(check-sat)
(assert false)
(push 1)
(assert _substvar_36_)
(set-info :status unsat)
(check-sat)
