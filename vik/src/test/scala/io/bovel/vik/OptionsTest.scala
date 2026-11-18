package io.bovel.vik

import io.bovel.smtlib.{Parser, Printer}

/** `:rlimit` (the fuel), `:timeout`, `:check-models`, and `:reason-unknown`. */
class OptionsTest extends munit.FunSuite:
  test("check-models: each sat's model makes the assertions true, as written"):
    val script = Parser.parse("""
      (set-option :check-models true)
      (declare-const x Int)
      (declare-const p Bool)
      (declare-const b (_ BitVec 8))
      (define-fun big ((v (_ BitVec 8))) Bool (bvugt v #x80))
      (assert (< 3 x 10))
      (assert (big (bvadd b #x01)))
      (assert (=> p (= x 7)))
      (check-sat)
      (push 1)
      (assert (let ((y (+ x 1))) (= y 5)))
      (check-sat)
      (pop 1)
      (check-sat-assuming (p))""")
    assertEquals(Session.run(script), List("sat", "sat", "sat"))
    val s = Session(checkModels = true)
    s.run("(declare-const x Int)")
    s.assert("(< x 0)")
    assertEquals(s.checkSat(), Some(true))

  test("rlimit and reason-unknown"):
    val script = Parser.parse("""
      (set-option :rlimit 1)
      (declare-const x Int)
      (declare-const y Int)
      (declare-fun f (Int) Int)
      (assert (or (= (f x) 1) (= (f y) 2)))
      (assert (< x y))
      (check-sat)
      (get-info :reason-unknown)
      (set-option :rlimit 1000000)
      (check-sat)
      (get-info :reason-unknown)""")
    assertEquals(
      Session.run(script),
      List("unknown", "((:reason-unknown resourceout))", "sat", "((:reason-unknown incomplete))")
    )
    assertEquals(Session.answers(script), List(None, Some(true)))
    assertEquals(Parser.parse(Printer.print(script)), script)
