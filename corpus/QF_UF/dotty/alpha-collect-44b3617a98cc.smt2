; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `collect.scala`
; original path: tests/*-custom-args/qualified-types/collect.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: T
; logic: QF_UF, narrowed from QF_UFDTLIA, which claims more than the script uses (prune logic)
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UF)
(declare-sort scala.Function1_S_Boolean 0)
(declare-sort MyList_T 0)
(declare-sort Any 0)
(declare-fun x () Any)
(declare-fun x0 () Any)
(declare-fun p () scala.Function1_S_Boolean)
(declare-fun Function1.apply@scala.Function1_S_Boolean (scala.Function1_S_Boolean Any) Bool)
(declare-fun x9 () MyList_T)
(declare-fun Any.$isInstanceOf$@MyList_T_MyList.MyCons_T_ (MyList_T) Bool)
(assert (= x x0))
(assert (= x x0))
(assert (Function1.apply@scala.Function1_S_Boolean p x))
(assert (Any.$isInstanceOf$@MyList_T_MyList.MyCons_T_ x9))
(assert (= x x0))
(assert (not (Function1.apply@scala.Function1_S_Boolean p x0)))
(check-sat)
