package net.bovel.vik

import net.bovel.smt2.{Parser, Printer}

/** `:named` assertions and `get-unsat-core`. */
class CoresTest extends munit.FunSuite:
  private def run(text: String): List[String] = Player.run(Parser.parse(text))

  test("the core names the guilty assertions"):
    val responses = run("""
      (set-logic QF_UF)
      (declare-sort U 0)
      (declare-const a U)
      (declare-const b U)
      (declare-const c U)
      (assert (! (= a b) :named ab))
      (assert (! (= b c) :named bc))
      (assert (! (distinct a c) :named ac))
      (assert (= a a))
      (check-sat)
      (get-unsat-core)""")
    assertEquals(responses.head, "unsat")
    val core = responses(1).stripPrefix("(").stripSuffix(")").split(" ").filter(_.nonEmpty).toSet
    assertEquals(core, Set("ab", "bc", "ac"))

  test("a satisfiable check clears the core; an innocent name stays out"):
    val responses = run("""
      (set-logic QF_LIA)
      (declare-const x Int)
      (declare-const y Int)
      (assert (! (<= x 0) :named low))
      (assert (! (<= 5 y) :named other))
      (check-sat)
      (assert (! (<= 1 x) :named high))
      (check-sat)
      (get-unsat-core)""")
    assertEquals(responses, List("sat", "unsat", "(low high)"))

  test("the printer round-trips a named assertion"):
    val text = "(set-logic QF_UF)\n(declare-const p Bool)\n(assert (! p :named goal))\n(check-sat)\n"
    val script = Parser.parse(text)
    assertEquals(Parser.parse(Printer.print(script)), script)
