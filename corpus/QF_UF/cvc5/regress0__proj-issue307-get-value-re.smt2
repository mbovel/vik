; From cvc5: regress0/proj-issue307-get-value-re.smt2 (modified BSD, LICENSES/cvc5.txt)
; SCRUBBER: grep -v -E '\(error'
; EXPECT: sat
(reset)
(set-logic ALL)
(set-option :produce-models true)
(set-info :status sat)
(check-sat)
(get-value ((re.opt re.allchar)))
