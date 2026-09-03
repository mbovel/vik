; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `scala-workshop-25.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/scala-workshop-25.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: (param)11
; untyped: (param)12
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFBVDT)
(declare-sort scala.collection.immutable.List_scala.Int 0)
(declare-sort scala.collection.immutable.List_scala.Any 0)
(declare-sort Any 0)
(declare-fun sk_main_12 () scala.collection.immutable.List_scala.Int)
(declare-fun SeqOps.size@scala.collection.immutable.List_scala.Int (scala.collection.immutable.List_scala.Int) (_ BitVec 32))
(declare-fun xs () scala.collection.immutable.List_scala.Int)
(declare-fun ys () scala.collection.immutable.List_scala.Int)
(declare-fun x0 () scala.collection.immutable.List_scala.Any)
(declare-fun SeqOps.size@scala.collection.immutable.List_scala.Any (scala.collection.immutable.List_scala.Any) (_ BitVec 32))
(declare-fun sk_main_14 () Any)
(declare-fun SeqOps.size@Any (Any) (_ BitVec 32))
(declare-fun sk_main_15 () Any)
(assert (= (SeqOps.size@scala.collection.immutable.List_scala.Int sk_main_12) (bvadd (SeqOps.size@scala.collection.immutable.List_scala.Int xs) (SeqOps.size@scala.collection.immutable.List_scala.Int ys))))
(assert (= (SeqOps.size@scala.collection.immutable.List_scala.Any x0) (bvadd (SeqOps.size@Any sk_main_14) (SeqOps.size@Any sk_main_15))))
(assert (not (= (SeqOps.size@scala.collection.immutable.List_scala.Int sk_main_12) (SeqOps.size@scala.collection.immutable.List_scala.Any x0))))
(check-sat)
