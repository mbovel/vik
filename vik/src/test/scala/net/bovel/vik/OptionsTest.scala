package net.bovel.vik

import net.bovel.smtlib.{Parser, Printer}

/** `:rlimit` (the fuel), `:timeout`, and `:reason-unknown`. */
class OptionsTest extends munit.FunSuite:
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
      Player.run(script),
      List("unknown", "((:reason-unknown resourceout))", "sat", "((:reason-unknown incomplete))")
    )
    assertEquals(Player.answers(script), List(None, Some(true)))
    assertEquals(Parser.parse(Printer.print(script)), script)
