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

  // The search decides only what a model needs, and the model has to hold anyway.
  private def assertModelHolds(script: String, expected: Boolean = true): Session =
    val s = Session()
    s.run("(set-option :produce-models true)")
    s.run(script)
    assertEquals(s.checkSat(), Some(expected))
    if expected then
      val m = s.model().get
      s.assertions.foreach(f => assert(m.holds(f), f))
    s

  test("different results of a function need different integer arguments") {
    assertModelHolds("""(declare-fun f (Int) Int) (declare-const a Int) (declare-const b Int)
      (assert (not (= (f a) (f b))))""")
    // The values the relaxation picks are equal until the equality is decided.
    assertModelHolds("""(declare-fun f (Int) Int) (declare-const a Int) (declare-const b Int)
      (assert (and (<= 0 a) (<= a 1) (<= 0 b) (<= b 1) (not (= (f a) (f b)))))""")
    assertModelHolds(
      """(declare-fun f (Int) Int) (declare-const a Int) (declare-const b Int)
      (assert (and (<= 0 a) (<= a 0) (<= 0 b) (<= b 0) (not (= (f a) (f b)))))""",
      expected = false
    )
  }

  // Twelve applications: a group that large has its pairs left to the model (`Encoder.pairsTheModelBreaks`).
  private val manyApplications =
    "(declare-fun f (Int) Int)(declare-const a Int)(declare-const b Int)" +
      (1 to 10).map(i => s"(declare-const x$i Int)(assert (= (f x$i) $i))").mkString +
      "(assert (= (f a) 20))(assert (= (f b) 30))"

  // Without `:produce-models`: an answer of a search that left atoms open rests on the model all the same.
  test("congruence reaches the arithmetic when no model is asked for") {
    val answers = Session().run(s2.Parser.parse("""(declare-sort U 0)(declare-fun f (U) Int)
      (declare-const s U)(declare-const t U)(assert (= s t))(assert (= (f s) 1))(check-sat)
      (assert (not (= (f t) 1)))(check-sat)"""))
    assertEquals(answers, List("sat", "unsat"))
    val large = (1 to 12).map(i => s"(declare-const x$i Int)(assert (= (f x$i) $i))").mkString
    val more = Session().run(s2.Parser.parse(s"""(declare-fun f (Int) Int)(declare-fun g (Int) Int)$large
      (declare-const a Int)(declare-const b Int)(assert (= (g a) 1))(assert (= (g b) 2))
      (assert (= (f a) (f b)))(assert (= a (f x1)))(assert (= b (f x1)))(check-sat)"""))
    assertEquals(more, List("unsat"))
  }

  test("a large function group still meets the arithmetic") {
    assertModelHolds(manyApplications + "(assert (<= a b))(assert (<= b a))", expected = false)
    assertModelHolds(manyApplications + "(assert (= x1 x2))", expected = false)
    assertModelHolds(manyApplications + "(assert (<= 0 a))(assert (<= a 1))(assert (<= 0 b))(assert (<= b 1))")
  }

  test("what is behind a false guard takes no part in the model") {
    assertModelHolds("""(declare-const g Bool) (declare-const x Int) (declare-const y Int)
      (assert (=> g (and (< x y) (< y x)))) (assert (or g (< x 0))) (assert (not g))""")
    assertModelHolds("""(declare-const g Bool) (declare-const h Bool) (declare-const x Int)
      (assert (or g h)) (assert (=> g (= x 1))) (assert (=> h (= x 2))) (assert (= g (> x 5)))""")
  }

  test("a Boolean term under a function has a value in the model") {
    assertModelHolds("""(declare-fun f (Bool) Int) (declare-const p Bool) (declare-const q Bool)
      (assert (not (= (f p) (f q)))) (assert (or p q))""")
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

  test("a declaration leaves with its level") {
    // Popped, the name is free for another sort (issue 40); with `:global-declarations` it stays.
    val s = Session()
    s.run("(declare-const a Int) (push 1) (declare-const x Int) (assert (> x 0))")
    assertEquals(s.checkSat(), Some(true))
    s.run("(pop 1) (declare-const x Bool) (declare-fun f (Bool) Int)")
    s.run("(assert (and (not x) (distinct (f x) (f false))))")
    assertEquals(s.checkSat(), Some(false))
    s.run(
      "(reset-assertions) (push 1) (define-fun p () Bool true) (assert p) (pop 1) (declare-const p Bool) (assert (not p))"
    )
    assertEquals(s.checkSat(), Some(true))
    intercept[s2.Smt2Error](s.run("(push 1) (declare-const q Int) (pop 1) (assert (= q 1))"))
    val g = Session()
    g.run("(set-option :global-declarations true) (push 1) (declare-const q Int) (pop 1) (assert (= q 1))")
    assertEquals(g.checkSat(), Some(true))
  }

  test("a datatype leaves with its level") {
    val s = Session()
    s.run("(push 1) (declare-datatype D ((a) (b))) (declare-const d D) (assert (= d a))")
    assertEquals(s.checkSat(), Some(true))
    s.run("(pop 1) (declare-datatype D ((c))) (declare-const e D) (assert (= e c))")
    assertEquals(s.checkSat(), Some(true))
    s.run("(reset-assertions) (push 1) (declare-datatype E ((m) (n))) (pop 1) (declare-const m Int) (assert (= m 5))")
    assertEquals(s.checkSat(), Some(true))
  }

  test("exit ends the script") {
    // What follows is not read, not even parsed (issue 52).
    val s = Session()
    assertEquals(
      s.run("(declare-const p Bool) (assert p) (check-sat) (exit) (assert (not p)) (check-sat) (not a command)"),
      List("sat")
    )
    assertEquals(s.run("(check-sat)"), Nil)
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
