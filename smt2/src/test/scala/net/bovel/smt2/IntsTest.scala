package net.bovel.smt2

import net.bovel.smt.{Formula, Term}
import net.bovel.smt.Formula.*
import net.bovel.smt.Term.*

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
    assertEquals(asserts(0), Le(Linear(List((2, x), (1, y)), -3), Num(5)))
    assertEquals(asserts(1), Le(Num(1), x)) // 0 < x is 0 + 1 ≤ x
    assertEquals(asserts(2), IntEq(Linear(List((1, x), (-1, y)), 0), Num(-1)))
    assertEquals(asserts(3), IntDistinct(List(x, y, Num(7))))
    assertEquals(asserts(5), Le(Linear(List((1, x), (1, y)), 1), Num(10)))
    // The `ite` is lowered to a constant defined by two integer equalities before its assertion.
    assert(s.commands.exists { case Command.DeclareFun("ite!1", Nil, "Int") => true; case _ => false })
    assertEquals(asserts(6), Implies(Pred(Const("p")), IntEq(Const("ite!1"), x)))

  test("solving and printing round trip"):
    val s = Parser.parse(script)
    assertEquals(Player.play(s), List(true))
    assertEquals(Parser.parse(Printer.print(s)), s)
    assertEquals(Player.play(Parser.parse(script.replace("(check-sat)", "(assert (< x 1)) (check-sat)"))), List(false))

  test("rejected inputs"):
    intercept[Smt2Error](Parser.parse("(declare-fun f (Int) Int)"))
    intercept[Smt2Error](Parser.parse("(declare-const x Int) (assert (<= (* x x) 1))"))
