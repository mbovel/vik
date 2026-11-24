; From bitwuzla: preprocess/normalize10.smt2 (MIT, LICENSES/bitwuzla.txt)
(set-info :status unsat)
(declare-const x (_ BitVec 17))
(check-sat-assuming ((bvsaddo (_ bv0 17) (bvadd (bvshl x x) (_ bv1 17) (bvshl x x)))))
