; From cvc5: regress0/issue12386-get-assertions.smt2 (modified BSD, LICENSES/cvc5.txt)
; SCRUBBER: sed -E 's/\(\(x \(- [1-9][0-9]*\)\)\)/((x NONZERO))/; s/\(\(x [1-9][0-9]*\)\)/((x NONZERO))/'
; EXPECT: sat
; EXPECT: (
; EXPECT: (distinct x 0)
; EXPECT: true
; EXPECT: )
; EXPECT: ((x NONZERO))
(set-option :produce-models true)
(set-logic QF_LIA)
(declare-const x Int)
(assert (distinct x 0))
(set-info :status sat)
(check-sat-assuming (true))
(get-assertions)
(get-value (x))
