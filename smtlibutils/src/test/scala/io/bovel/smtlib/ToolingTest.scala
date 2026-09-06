package io.bovel.smtlib

/** The corpus tools' text handling: what the importer stamps, what the status
 *  stamper shows a solver.
 */
class ToolingTest extends munit.FunSuite:
  test("the importer stamps the live check-sats, not the commented-out ones"):
    // A status spliced into `;(check-sat)` stayed in the comment and made the command live (issue 56).
    val source = "(assert (> a 0))\n;(check-sat)\n(check-sat)\n(assert (< a 0))\n(check-sat)\n"
    assertEquals(
      Import.withStatuses(source, List("sat", "unsat")),
      "(assert (> a 0))\n;(check-sat)\n(set-info :status sat)\n(check-sat)\n(assert (< a 0))\n(set-info :status unsat)\n(check-sat)\n"
    )
    assertEquals(Import.withStatuses("(assert p) ; (check-sat) later\n(check-sat)\n", List("sat")),
      "(assert p) ; (check-sat) later\n(set-info :status sat)\n(check-sat)\n")

  test("the status stamper shows the solver a script without statuses"):
    // z3 and cvc5 error on a status they contradict, and the error dropped every answer (issue 57).
    assertEquals(
      withoutStatuses("(set-info :status sat)\n(check-sat)\n(set-info :status unknown)\n(check-sat)\n"),
      "(set-info :status unknown)\n(check-sat)\n(set-info :status unknown)\n(check-sat)\n"
    )
