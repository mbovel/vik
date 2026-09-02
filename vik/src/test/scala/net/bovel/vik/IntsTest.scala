package net.bovel.vik

import net.bovel.smtlib.*
import net.bovel.smtlib.Formula.*
import net.bovel.smtlib.Term.*

/** QF_LIA scripts: the `Int` sort, numerals, `+`, `-`, `*`, comparisons,
 *  equality and `distinct` on integers, `ite` on integer terms.
 */
class IntsTest extends munit.FunSuite:
  val script = """
    (set-logic QF_LIA)
    (declare-fun x () Int)
    (declare-const y Int)
    (declare-const p Bool)
    (assert (<= (+ (* 2 x) y (- 3)) 5))
    (assert (> x 0))
    (assert (= (- x y) (- 1)))
    (assert (distinct x y 7))
    (assert (=> p (>= (* x 3) (+ y 2))))
    (assert (let ((s (+ x y))) (< s 10)))
    (assert (= (ite p x y) 2))
    (check-sat)
  """

  test("parsing"):
    val s = Parser.parse(script)
    val asserts = s.commands.collect { case Command.Assert(f) => f }
    val x = Const("x")
    val y = Const("y")
    assertEquals(
      asserts(0),
      Cmp(
        CmpOp.Le,
        List(
          ArithApp(
            ArithOp.Add,
            List(ArithApp(ArithOp.Mul, List(Numeral(2), x)), y, ArithApp(ArithOp.Neg, List(Numeral(3))))
          ),
          Numeral(5)
        )
      )
    )
    assertEquals(asserts(1), Cmp(CmpOp.Gt, List(x, Numeral(0))))
    assertEquals(asserts(2), Eq(List(ArithApp(ArithOp.Sub, List(x, y)), ArithApp(ArithOp.Neg, List(Numeral(1))))))
    assertEquals(asserts(3), Distinct(List(x, y, Numeral(7))))
    assertEquals(
      asserts(5),
      Formula.Let(List(("s", ArithApp(ArithOp.Add, List(x, y)))), Cmp(CmpOp.Lt, List(Const("s"), Numeral(10))))
    )
    assertEquals(asserts(6), Eq(List(Term.Ite(Pred(Const("p")), x, y), Numeral(2))))

  test("solving and printing round trip"):
    val s = Parser.parse(script)
    assertEquals(Session.run(s), List("sat"))
    assertEquals(Parser.parse(Printer.print(s)), s)
    assertEquals(
      Session.run(Parser.parse(script.replace("(check-sat)", "(assert (< x 1)) (check-sat)"))),
      List("unsat")
    )

  test("rejected inputs"):
    intercept[Smt2Error](Lowering(Parser.parse("(declare-const x Int) (assert (<= (* x x) 1))")))
