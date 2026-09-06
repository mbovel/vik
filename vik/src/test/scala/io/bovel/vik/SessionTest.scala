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

  test("an unsat among the top-level assertions holds for every later check") {
    // A conflict found at decision level 0 (issue 37): plain, under an assumption, under a push, with
    // one more assertion, on Booleans and on the bounds of one integer.
    val s = Session()
    s.run("(declare-const u Bool) (declare-const f Bool) (declare-const q Bool)")
    s.run("(assert (or (not u) (not f))) (assert f) (assert u)")
    assertEquals(s.checkSat(), Some(false))
    assertEquals(s.checkSat(), Some(false))
    assertEquals(s.run("(check-sat-assuming (q))"), List("unsat"))
    s.push()
    s.assert("q")
    assertEquals(s.checkSat(), Some(false))
    s.pop()
    s.assert("(or u q)")
    assertEquals(s.checkSat(), Some(false))
    val t = Session()
    t.run("(declare-const x Int) (assert (<= x 5)) (assert (not (<= x 7)))")
    assertEquals(t.run("(check-sat) (check-sat) (check-sat-assuming ((<= x 100)))"), List("unsat", "unsat", "unsat"))
  }

  test("a budget spent before the lemmas answers unknown") {
    // The array lemmas take rounds of the lemma loop, which the budget stops (issue 38).
    val text = """
      (declare-sort E 0)
      (declare-const a (Array Int E)) (declare-const b (Array Int E)) (declare-const x E) (declare-const y E)
      (assert (= a (store b 0 x))) (assert (= (select a 0) y)) (assert (not (= x y)))
      (check-sat)"""
    assertEquals(Session(fuel = 0).run(text), List("unknown"))
    assertEquals(Session().run(text), List("unsat"))
  }

  test("an unknown symbol is an error of the text, not of the solver") {
    val s = Session()
    intercept[s2.Smt2Error](s.assert("(< x 1)"))
    s.run("(declare-const x Int)")
    s.assert("(< x 1)")
    assertEquals(s.checkSat(), Some(true))
  }

  test("pop through the API clears the lowering caches (issue 22)"):
    val s = Session()
    s.run("(set-logic QF_LRA)(declare-const x Real)")
    s.push()
    s.assert("(< x 0.5)")
    assertEquals(s.checkSat(), Some(true))
    s.pop()
    s.assert("(> x 0.5)")
    s.assert("(< x 0.25)")
    assertEquals(s.checkSat(), Some(false), "x > 1/2 and x < 1/4: the constant for 1/2 must keep its definition")
