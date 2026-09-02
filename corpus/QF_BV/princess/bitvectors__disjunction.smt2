; From princess: bitvectors/disjunction.smt2 (BSD, LICENSES/princess.txt)
(set-logic ALL)

(declare-fun x () (_ BitVec 1))

(assert (not (and (>= (ubv_to_int x) 0) (<= (ubv_to_int x) 1))))

(set-info :status unsat)
(check-sat)