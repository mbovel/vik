package net.bovel.vik

import net.bovel.smtlib.{Parser, Printer}

/** `:named` assertions and `get-unsat-core`. */
class CoresTest extends munit.FunSuite:
  private def run(text: String): List[String] = Session.run(Parser.parse(text))

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

  test("a named assertion dies with its level"):
    val responses = run("""
      (set-logic QF_UF)
      (declare-const p Bool)
      (push 1)
      (assert (! p :named a))
      (check-sat)
      (pop 1)
      (assert (not p))
      (check-sat)""")
    assertEquals(responses, List("sat", "sat"))

  test("a named assertion sees the constants the simplification defined away"):
    val responses = run("""
      (set-logic QF_UF)
      (declare-sort U 0)
      (declare-const x U)
      (declare-const y U)
      (declare-const z U)
      (assert (and (= x y) (distinct y z)))
      (assert (! (= x z) :named xz))
      (check-sat)
      (get-unsat-core)""")
    assertEquals(responses, List("unsat", "(xz)"))
