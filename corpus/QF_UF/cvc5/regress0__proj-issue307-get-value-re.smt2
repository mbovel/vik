; From cvc5: regress0/proj-issue307-get-value-re.smt2 (modified BSD, LICENSES/cvc5.txt)
; SCRUBBER: grep -v -E '\(error'
; EXPECT: sat
; logic: QF_UF, the smallest the script fits, for ALL (prune logic)
(reset)
(set-logic QF_UF)
(set-option :produce-models true)
(set-info :status sat)
(check-sat)
(get-value ((re.opt re.allchar)))
