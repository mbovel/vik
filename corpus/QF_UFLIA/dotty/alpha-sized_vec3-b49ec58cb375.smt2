; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `sized_vec3.scala`
; original path: tests/*-custom-args/qualified-types/sized_vec3.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: Vec[String]
; untyped: Vec[Int]
; logic: QF_UFLIA, narrowed from QF_UFDTLIA, which claims more than the script uses (prune logic)
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFLIA)
(declare-sort Any 0)
(declare-fun sk_example3_8 () Any)
(declare-fun sized_vec3$package$.len_String (Any) Int)
(declare-fun mPlusN () Int)
(declare-fun m () Int)
(declare-fun n () Int)
(declare-fun x0 () Any)
(declare-fun sized_vec3$package$.len_Int (Any) Int)
(assert (= (sized_vec3$package$.len_String sk_example3_8) mPlusN))
(assert (= mPlusN (+ m n)))
(assert (= (sized_vec3$package$.len_String sk_example3_8) mPlusN))
(assert (= mPlusN (+ m n)))
(assert (= (sized_vec3$package$.len_String sk_example3_8) (sized_vec3$package$.len_Int x0)))
(assert (not (= (sized_vec3$package$.len_String sk_example3_8) (sized_vec3$package$.len_Int x0))))
(check-sat)
