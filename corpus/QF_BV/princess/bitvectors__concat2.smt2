; From princess: bitvectors/concat2.smt2 (BSD, LICENSES/princess.txt)
(declare-fun a () (_ BitVec 16))
(declare-fun b () (_ BitVec 32))
(declare-fun c () (_ BitVec 48))

(assert (= (concat a b c) (concat c b a)))

(set-info :status sat)
(check-sat)