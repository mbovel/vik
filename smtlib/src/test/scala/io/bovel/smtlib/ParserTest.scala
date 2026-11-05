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

  test("an ill-sorted script is refused"):
    // Each of these got a definite answer before the parser checked sorts; z3 refuses them all.
    val declarations = """
      (declare-sort U 0) (declare-sort V 0) (declare-const u U) (declare-const v V)
      (declare-const b Bool) (declare-const i Int) (declare-const r Real)
      (declare-const x (_ BitVec 8)) (declare-const y (_ BitVec 4))
      (declare-const a (Array Int U))
      (declare-fun f (U) U) (declare-fun g (Int) Int)
      (define-fun m ((p U)) U p)
      (declare-datatype D ((d (field U)))) (declare-datatype E ((e (other Int)))) (declare-const t D)
      """
    for ill <- List(
        "(= u v)", // the shape of corpus file alpha-beta_reduce-932bafde0fe6
        "(= (f u) v)",
        "(= (ite b u v) u)",
        "(distinct u v)",
        "(= u i)",
        "(= i b)",
        "(= x y)",
        "(= x 1)",
        "(= (f v) u)", // arguments
        "(= (g u) 1)",
        "(= (g r) 1)", // a Real is no Int, though z3 truncates it
        "(= (m v) u)",
        "(= (d v) t)",
        "(= (field (e 1)) u)",
        "((_ is d) (e 1))",
        "(= (select a u) u)",
        "(= (select a 1) v)",
        "(= (select (store a 1 v) 1) u)",
        "(= (+ i u) 1)", // theory operations
        "(= (+ i b) 1)",
        "(<= b b)",
        "(<= x x)",
        "(is_int u)",
        "(= (bvadd x y) x)",
        "(= (bvadd x i) x)",
        "(bvult x y)",
        "(= ((_ extract 8 0) x) x)",
        "(= ((_ extract 0 1) x) x)", // threw an ArithmeticException in the lowering
        "(= ((_ repeat 0) x) x)",
        "(= ((_ int2bv 8) r) x)",
        "(= (let ((w u)) (f (g w))) u)",
        "(= (as i Bool) b)"
      )
    do intercept[Smt2Error](parse(s"$declarations (assert $ill)"))
    for ill <- List("(define-fun c () Int 1.5)", "(define-fun c () U 1)", "(define-fun c ((p V)) U p)") do
      intercept[Smt2Error](parse(s"$declarations $ill"))
    intercept[Smt2Error](parse(s"$declarations (check-sat-assuming ((= u v)))"))

  test("an Int fits where a Real is expected, and the well-sorted scripts stay"):
    val declarations = """
      (declare-const b Bool) (declare-const i Int) (declare-const r Real) (declare-const x (_ BitVec 8))
      (declare-fun h (Real) Real) (define-fun n ((p Real)) Real p) (define-fun e () Real 0)
      (declare-datatype D ((d (field Real)))) (declare-const a (Array Real Real))
      """
    for fine <- List(
        "(= i r)",
        "(distinct i r)",
        "(= (ite b 1 r) 1.5)",
        "(= (+ i r) 1)",
        "(< i r 2)",
        "(= (h 1) (n i))",
        "(= (field (d 1)) 1.0)",
        "(= (select (store a 1 2) 1) 2.0)",
        "(= (to_real i) (to_real r))",
        "(= (to_int i) (to_int r))",
        "(is_int i)",
        "(= (/ i 2) 1.5)",
        "(= (bvadd x x x) ((_ extract 7 0) (concat x x)))",
        "(= (bvcomp x x) #b1)",
        "(= ((_ repeat 2) x) ((_ zero_extend 8) x))",
        "(= (ubv_to_int x) i)",
        "(= ((_ int2bv 8) i) x)"
      )
    do parse(s"$declarations (assert $fine)")

  test("an instance of a declared sort is one sort, however it is written"):
    parse("""
      (declare-sort-parameter X) (declare-sort Box 1) (define-sort IntBox () Int)
      (declare-fun unbox ((Box X)) X) (declare-const b (Box IntBox)) (declare-const c (Box Int))
      (assert (= (unbox b) (unbox c) 1))
      """)
    intercept[Smt2Error](
      parse("(declare-sort Box 1) (declare-const b (Box Int)) (declare-const c (Box Bool)) (assert (= b c))")
    )
  test("an indexed identifier that is no bit-vector literal is an error, not a crash"):
    for script <- List(
        "(assert (= (_ bvfoo 8) #x00))",
        "(assert (= #x00 (_ bv 8)))",
        "(assert (= #x00 (_ bv-1 8)))"
      )
    do intercept[Smt2Error](parse(script))
    parse("(assert (= (_ bv255 8) #xFF))")
