; From cvc5: regress0/push-pop/bug233.cvc.smt2 (modified BSD, LICENSES/cvc5.txt)
; COMMAND-LINE: --incremental
; EXPECT: unsat
; EXPECT: sat
(set-logic ALL)
(declare-fun a () Bool)
(declare-fun b () Bool)
(set-info :status unsat)
(check-sat-assuming ( (not (let ((_let_1 (and a b))) (or _let_1 (not _let_1)))) ))
(set-info :status sat)
(check-sat-assuming ( (not (or a b)) ))
