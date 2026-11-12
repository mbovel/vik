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

  test("the pruner shows z3 the script, not its solving or its options"):
    // cvc5's `:incremental` is an error to z3, and so is a `get-value` with no model: neither is about the text.
    assertEquals(
      withoutSolving(
        "(set-option :incremental true)\n(set-option :global-declarations true)\n(declare-const p Bool) ; p\n" +
          "(assert p)\n(check-sat)\n(get-value (p))\n(check-sat-assuming (p))\n"
      ),
      "(set-option :global-declarations true)\n(declare-const p Bool)\n(assert p)\n"
    )

  test("a file's family is its importer's prefix or its test, in its directory"):
    assertEquals(family("QF_UFDTLIA/dotty/alpha-list_zip_2-0757c679b587.smt2"), ("QF_UFDTLIA/dotty", "alpha-list_zip_2"))
    assertEquals(family("QF_BV/smt-lib/sage__app10__bench_42.smt2"), ("QF_BV/smt-lib", "sage"))

  test("the trivial files kept differ most: another status, then another shape"):
    def member(name: String, status: String, script: String) =
      val commands = sexpr.SExprParser.parse(script)
      Member(name, List(status), script.length, false, true, Alike.shape(commands))
    val decls = "(declare-const x Int)(declare-const y Int)"
    val big = member("big", "sat", decls + "(assert (> x 0))(assert (> y 0))(assert (< x 9))")
    val copy = member("copy", "sat", decls + "(assert (> y 0))(assert (> x 0))(assert (< x 8))")
    val unsat = member("unsat", "unsat", decls + "(assert (> x 0))(assert (> y 0))(assert (< x 0))")
    val other = member("other", "sat", "(declare-const p Bool)(assert p)")
    assertEquals(spread(Nil, List(copy, unsat, big), 2).map(_.file), List("big", "unsat"))
    assertEquals(spread(List(big), List(copy, other), 1).map(_.file), List("other"))

  test("a logic is read as its parts and written back, and holds the logics within it"):
    for name <- List("QF_AUFBVLIA", "QF_AX", "QF_ALIA", "QF_UFDTLIA", "QF_UFIDL", "QF_BVLRA", "QF_UF") do
      assertEquals(Logic.parse(name).map(_.name), Some(name))
    assertEquals(Logic.parse("ALL"), None)
    def within(a: String, b: String) = Logic.parse(a).get.within(Logic.parse(b).get)
    assert(within("QF_UFIDL", "QF_UFLIA") && within("QF_LIA", "AUFLIA") && within("QF_LRA", "QF_LIRA"))
    assert(!within("QF_UF", "QF_LIA") && !within("QF_LIA", "QF_IDL") && !within("AUFLIA", "QF_AUFLIA"))

  test("a script relabeled sets the new logic and says so in its header"):
    assertEquals(
      Logic.relabel("; From x\n(set-logic QF_UFDTLIA)\n(check-sat)\n", "QF_UF"),
      "; From x\n; logic: QF_UF, the smallest the script fits, for QF_UFDTLIA (prune logic)\n(set-logic QF_UF)\n(check-sat)\n"
    )
    assertEquals(
      Logic.relabel("; From x\n(check-sat)\n", "QF_UF"),
      "; From x\n; logic: QF_UF, the smallest the script fits, which set none (prune logic)\n(set-logic QF_UF)\n(check-sat)\n"
    )
    // A set-logic in a comment sets nothing.
    assertEquals(Logic.claimed(";(set-logic QF_UFNIA)\n(set-logic ALL) ; or (set-logic QF_LIA)\n"), List("ALL"))
