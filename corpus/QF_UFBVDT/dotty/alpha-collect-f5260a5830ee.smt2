; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `collect.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/collect.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: S
; untyped: T
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFBVDT)
(declare-sort scala.Function1_S_scala.Boolean 0)
(declare-sort MyList_T 0)
(declare-sort Any 0)
(declare-fun p () scala.Function1_S_scala.Boolean)
(declare-fun x () Any)
(declare-fun Function1.apply@scala.Function1_S_scala.Boolean (scala.Function1_S_scala.Boolean Any) Bool)
(declare-fun x9 () MyList_T)
(declare-fun Any.$isInstanceOf$@MyList_T_MyList$.MyCons_T (MyList_T) Bool)
(declare-fun x0 () Any)
(assert (Function1.apply@scala.Function1_S_scala.Boolean p x))
(assert (Any.$isInstanceOf$@MyList_T_MyList$.MyCons_T x9))
(assert true)
(assert (not (Function1.apply@scala.Function1_S_scala.Boolean p x0)))
(check-sat)
