package net.bovel.vik

import net.bovel.smt2.*
import Formula.*

class Smt2Test extends munit.FunSuite:

  def run(script: String): List[Boolean] = Player.play(Parser.parse(script))

  test("declarations, assertions and check-sat"):
    val s = Parser.parse("""
      (set-logic QF_UF)
      (set-info :status unsat)
      (declare-sort U 0)
      (declare-fun a () U)
      (declare-const b U)
      (declare-fun f (U) U)
      (assert (= a b))
      (assert (not (= (f a) (f b))))
      (check-sat)
      (exit)
    """)
    assertEquals(s.expectedStatus, Some(false))
    assertEquals(s.commands.collect { case Command.Assert(f) => f }.length, 2)
    assertEquals(Player.play(s), List(false))

  test("Boolean sorts: constants, predicates and equality as iff"):
    assertEquals(
      run("""
      (declare-sort U 0)
      (declare-fun p (U) Bool)
      (declare-const q Bool)
      (declare-const x U)
      (declare-const y U)
      (assert (= x y))
      (assert (p x))
      (assert (= q (p y)))
      (assert (not q))
      (check-sat)
    """),
      List(false)
    )

  test("let, define-fun, distinct, implies, xor, ite"):
    assertEquals(
      run("""
      (declare-sort U 0)
      (declare-fun f (U) U)
      (declare-const a U)
      (declare-const b U)
      (declare-const c U)
      (define-fun same ((x U) (y U)) Bool (= x y))
      (assert (let ((fa (f a)) (fb (f b))) (=> (same a b) (same fa fb))))
      (assert (distinct a b c))
      (assert (xor (= (f a) (f b)) (= (ite (= a b) a b) b)))
      (check-sat)
      (push 1)
      (assert (= a b))
      (check-sat)
      (pop 1)
      (check-sat)
    """),
      List(true, false, true)
    )

  test("term-level ite is lowered to a fresh constant"):
    val s = Parser.parse("""
      (declare-sort U 0)
      (declare-const a U)
      (declare-const b U)
      (declare-const c U)
      (assert (= c (ite (= a b) a b)))
      (assert (not (= c b)))
      (check-sat)
    """)
    assertEquals(s.commands.collect { case Command.Assert(_) => 1 }.sum, 4) // two definitions, then the two assertions
    assertEquals(
      s.commands.collect { case Command.DeclareFun(n, Nil, "U") if n.startsWith("ite!") => n },
      List("ite!1")
    )
    assertEquals(Player.play(s), List(false))

  test("printing round-trips"):
    val text = """(set-logic QF_UF)
(declare-sort U 0)
(declare-fun f (U) U)
(declare-fun p (U) Bool)
(declare-fun a () U)
(declare-fun b () U)
(assert (=> (= a b) (= (f a) (f b))))
(assert (or (not (p a)) (p (f a)) (= a (f b))))
(assert (= (p a) (p b)))
(check-sat)
"""
    val script = Parser.parse(text)
    assertEquals(Printer.print(script), text)
    assertEquals(Player.play(Parser.parse(Printer.print(script))), Player.play(script))

  test("errors"):
    intercept[Smt2Error](Parser.parse("(assert (= a unknown!))"))
    intercept[Smt2Error](Parser.parse("(declare-const p Bool) (assert (= p unknown))"))
