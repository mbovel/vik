package io.bovel.vik

import io.bovel.smtlib.Term.Const
import io.bovel.smtlib as s2

class SessionTest extends munit.FunSuite:
  test("commands arrive piece by piece, each in the scope of the earlier ones") {
    val s = Session()
    s.run("(declare-const x Int) (declare-const y Int) (define-fun big ((z Int)) Bool (> z 100))")
    s.assert("(< x y)")
    assertEquals(s.checkSat(), Some(true))
    s.push()
    s.assert("(big x)") // the macro of the first piece
    s.assert(s2.Formula.Cmp(s2.CmpOp.Lt, List(Const("y"), Const("x")))) // a tree, in the same scope
    assertEquals(s.checkSat(), Some(false))
    s.pop()
    assertEquals(s.checkSat(), Some(true))
  }

  test("a lowered convenience keeps its definitions across the pieces") {
    val s = Session()
    s.run("(set-option :produce-models true) (declare-const x Int) (declare-const y Int)")
    s.assert("(= y (ite (< x 0) 0 x))") // the ite becomes a defined constant, declared before the assertion
    s.assert("(< x (- 3))")
    assertEquals(s.checkSat(), Some(true))
    assertEquals(s.model().map(_.value(Term.Const("y"))), Some(Term.Num(0)))
    assertEquals(s.run("(get-value (y))"), List("((y 0))"))
  }

  test("the responses of each piece, and the state of the whole") {
    val s = Session()
    assertEquals(s.run("(declare-const p Bool) (assert (! p :named a)) (check-sat)"), List("sat"))
    assertEquals(s.run("(assert (! (not p) :named b)) (check-sat) (get-unsat-core)"), List("unsat", "(a b)"))
    assertEquals(s.unsatCore, List("a", "b"))
    assertEquals(s.run("(reset) (declare-const p Int) (assert (> p 0)) (check-sat)"), List("sat"))
  }

  test("an unknown symbol is an error of the text, not of the solver") {
    val s = Session()
    intercept[s2.Smt2Error](s.assert("(< x 1)"))
    s.run("(declare-const x Int)")
    s.assert("(< x 1)")
    assertEquals(s.checkSat(), Some(true))
  }
