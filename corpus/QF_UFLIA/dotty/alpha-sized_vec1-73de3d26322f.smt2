; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `sized_vec1.scala`
; original path: tests/*-custom-args/qualified-types/sized_vec1.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: Vec
; logic: QF_UFLIA, narrowed from QF_UFDTLIA, which claims more than the script uses (prune logic)
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFLIA)
(declare-sort Any 0)
(declare-fun sized_vec1$package$.vec (Int) Any)
(declare-fun v3 () Any)
(declare-fun sized_vec1$package$.size (Any) Int)
(declare-fun v4 () Any)
(declare-fun x0 () Any)
(assert (= (sized_vec1$package$.vec 3) v3))
(assert (= 3 (sized_vec1$package$.size (sized_vec1$package$.vec 3))))
(assert (= 3 (sized_vec1$package$.size v3)))
(assert (= (sized_vec1$package$.vec 4) v4))
(assert (= 4 (sized_vec1$package$.size (sized_vec1$package$.vec 4))))
(assert (= 4 (sized_vec1$package$.size v4)))
(assert (= (sized_vec1$package$.size x0) (+ (sized_vec1$package$.size v3) (sized_vec1$package$.size v4))))
(assert (not (= (sized_vec1$package$.size x0) 7)))
(check-sat)
