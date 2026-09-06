package io.bovel.smtlib

/** What the parser refuses, and what it reads as the script's own. */
class ParserTest extends munit.FunSuite:
  private def parse(text: String): Script = Parser.parse(text)

  test("a script's function named like an operator is its own symbol"):
    // In a logic without arithmetic, `+` is whatever the script declares (issue 41).
    val script = parse("(declare-sort U 0) (declare-fun + (U U) U) (declare-const a U) (assert (= (+ a a) a))")
    script.commands.last match
      case Command.Assert(Formula.Eq(List(Term.App(Term.Const("+"), List(Term.Const("a"), Term.Const("a"))), _))) => ()
      case other => fail(s"read as the operator: $other")
    val defined = parse(
      "(declare-sort U 0) (declare-fun g (U) U) (define-fun to_real ((x U)) U (g x)) (declare-const a U) (assert (= (to_real a) a))"
    )
    assert(Printer.print(defined).contains("(to_real a)"))

  test("the reserved words and the Core symbols cannot be declared"):
    for name <- List("and", "=", "ite", "let", "forall", "true") do
      intercept[Smt2Error](parse(s"(declare-fun $name (Bool Bool) Bool)"))
      intercept[Smt2Error](parse(s"(define-fun $name ((x Bool)) Bool x)"))

  test("the binder of a match stays clear of the names bound around it"):
    // A let-bound `match!2`, a macro parameter `match!1` (issue 42): the case body reads them, not the scrutinee.
    val script = parse("""
      (declare-datatype L ((nil) (cons (h Int) (t L))))
      (define-fun g ((match!1 L) (x L)) L (match x ((nil nil) ((cons a b) match!1))))
      (declare-const x L) (declare-const y L)
      (assert (= (let ((match!1 y) (match!2 y)) (match x ((nil nil) ((cons a b) match!2)))) x))""")
    val binders = "\\(match!(\\d+) x\\)".r.findAllMatchIn(Printer.print(script)).map(_.group(1).nn.toInt).toList
    // The macro's binder, then the assertion's: neither is a name bound around its match.
    assertEquals(binders.length, 2)
    assert(binders.head != 1 && binders(1) != 1 && binders(1) != 2, binders.toString)

  test("an instance's name is refused as a script symbol, in either order"):
    // `f@Int` is the instance of `f` at Int (issue 43).
    intercept[Smt2Error](
      parse("(declare-sort-parameter A) (declare-fun f@Int (Int) Int) (declare-fun f (A) A) (assert (= (f 1) 5))")
    )
    intercept[Smt2Error](
      parse("(declare-sort-parameter A) (declare-fun f (A) A) (assert (= (f 1) 5)) (declare-fun f@Int (Int) Int)")
    )

  test("an index that does not fit is refused"):
    // `(_ rotate_left 4294967296)` was a rotation by nothing (issue 44).
    intercept[Smt2Error](parse("(declare-const x (_ BitVec 3)) (assert (= ((_ rotate_left 4294967296) x) x))"))
    intercept[Smt2Error](parse("(declare-const x (_ BitVec 4294967299))"))
    intercept[Smt2Error](parse("(declare-const x (_ BitVec 3)) (assert (= ((_ extract 4294967296 0) x) x))"))
    intercept[Smt2Error](parse("(push 4294967297)"))
