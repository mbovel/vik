package net.bovel.vik

import net.bovel.smtlib.*
import net.bovel.smtlib.Formula.*
import net.bovel.smtlib.Term.*

/** Higher-order core (SMT-LIB 2.7): curried applications, extensionality of
 *  functions, and `Bool` as a sort — formulas as arguments, `ite` branches,
 *  datatype fields and lambda bodies.
 */
class HoCoreTest extends munit.FunSuite:
  def check(script: String): List[Boolean] = Session.answers(Parser.parse(script)).map(_.get)

  test("curried and partial applications"):
    val script = Parser.parse("""
      (declare-fun f (Int Int) Int)
      (declare-const g (-> Int Int))
      (assert (= g (f 1)))
      (assert (= ((f 1) 2) (f 1 2)))""")
    val asserts = script.commands.collect { case Command.Assert(a) => a }
    assertEquals(asserts(0), Eq(List(Const("g"), App(Const("f"), List(Numeral(1))))))
    assertEquals(
      asserts(1), // `((f 1) 2)` as written on the left, `(f 1 2)` on the right: the lowering equates them
      Eq(List(App(App(Const("f"), List(Numeral(1))), List(Numeral(2))), App(Const("f"), List(Numeral(1), Numeral(2)))))
    )
    assertEquals(
      check("""
        (declare-fun f (Int Int) Int)
        (declare-const g (-> Int Int))
        (assert (= g (f 1)))
        (push) (assert (not (= (g 2) (f 1 2)))) (check-sat) (pop)
        (push) (assert (not (= (g 2) (f 1 3)))) (check-sat) (pop)
        (declare-const k (-> Int (-> Int Int)))
        (assert (= k (lambda ((x Int) (y Int)) (- x y))))
        (push) (assert (not (= ((k 5) 3) 2))) (check-sat) (pop)
        (push) (assert (not (= (k 5 3) 2))) (check-sat) (pop)
        (push) (assert (= (k 5) (lambda ((y Int)) (- 5 y)))) (check-sat) (pop)"""),
      List(false, true, false, false, true)
    )

  test("extensionality: a witness per equality between functions"):
    assertEquals(
      check("""
        (declare-fun f (Int) Int)
        (declare-fun g (Int) Int)
        (push) (assert (distinct f (lambda ((x Int)) (f x)))) (check-sat) (pop)
        (push) (assert (distinct f g)) (check-sat) (pop)
        (push) (assert (distinct f g)) (assert (= (f 1) (g 1))) (check-sat) (pop)
        (push) (assert (not (= (lambda ((x Int)) (+ (f x) 1)) (lambda ((y Int)) (+ 1 (f y)))))) (check-sat) (pop)
        (declare-const k (-> Int Int Int))
        (push) (assert (distinct k (lambda ((x Int)) (lambda ((y Int)) (k x y))))) (check-sat) (pop)"""),
      List(false, true, true, false, false)
    )

  test("Boolean terms as arguments, and their two truth values"):
    assertEquals(
      check(
        """
        (declare-fun f (Bool) Int)
        (declare-const p Bool)
        (declare-const q Bool)
        (declare-const x Int)
        (push) (assert (not (= (f true) (f (not false))))) (check-sat) (pop)
        (push) (assert (= p q)) (assert (not (= (f p) (f q)))) (check-sat) (pop)
        (push) (assert (not p)) (assert (not q)) (assert (not (= (f p) (f q)))) (check-sat) (pop)
        (push) (assert (not (= (f p) (f q)))) (check-sat) (pop)
        (push) (assert (not (= (f (and p q)) (f (and q p))))) (check-sat) (pop)
        (push) (assert (= (f (> x 0)) 1)) (assert (= (f false) 2)) (assert (<= x 0)) (check-sat) (pop)
        (push) (assert (= (f (> x 0)) 1)) (assert (= (f false) 2)) (assert (> x 0)) (check-sat) (pop)"""
      ),
      List(false, false, false, true, false, false, true)
    )

  test("Boolean lambda bodies and datatype fields"):
    assertEquals(
      check("""
        (declare-const pos (-> Int Bool))
        (assert (= pos (lambda ((x Int)) (> x 0))))
        (push) (assert (pos 5)) (check-sat) (pop)
        (push) (assert (pos (- 5))) (check-sat) (pop)
        (push) (assert (not (pos 5))) (check-sat) (pop)
        (declare-datatype P ((mk (flag Bool) (v Int))))
        (declare-const r P)
        (push) (assert (= r (mk true 1))) (assert (not (flag r))) (check-sat) (pop)
        (push) (assert (= r (mk (pos 3) 1))) (assert (not (flag r))) (check-sat) (pop)
        (push) (assert (= r (mk (pos (- 3)) 1))) (assert (not (flag r))) (check-sat) (pop)"""),
      List(true, false, false, false, false, true)
    )
    val printed = Printer.print(Parser.parse("(declare-fun f (Bool) Int) (assert (= (f (and true (not false))) 1))"))
    assert(printed.contains("(f (and true (not false)))"), printed)
