package net.bovel.sexpr

import SExpr.{SBits, SDecimal, SKeyword, SList, SNumeral, SString, SSymbol}

class SExprTest extends munit.FunSuite:

  test("atoms and lists"):
    assertEquals(SExprParser.parse("x"), List(SSymbol("x")))
    assertEquals(SExprParser.parse("(f a 12)"), List(SList(List(SSymbol("f"), SSymbol("a"), SNumeral(12)))))
    assertEquals(
      SExprParser.parse("(f (g a) ())"),
      List(SList(List(SSymbol("f"), SList(List(SSymbol("g"), SSymbol("a"))), SList(Nil))))
    )

  test("comments, quoted symbols, strings and keywords"):
    val input = """
      ; a comment
      (set-info :status sat) ; trailing comment
      (declare-fun |a b| () U)
      (echo "say ""hi"" now")
    """
    assertEquals(
      SExprParser.parse(input),
      List(
        SList(List(SSymbol("set-info"), SKeyword("status"), SSymbol("sat"))),
        SList(List(SSymbol("declare-fun"), SSymbol("a b"), SList(Nil), SSymbol("U"))),
        SList(List(SSymbol("echo"), SString("say \"hi\" now")))
      )
    )

  test("the lexical classes of the literals"):
    assertEquals(
      SExprParser.parse("(12 1.05 #x1f #b0110)"),
      List(SList(List(SNumeral(12), SDecimal(105, 2), SBits(31, 8, true), SBits(6, 4, false))))
    )
    // A quoted symbol is the text between its bars, and the same symbol as that text unquoted; one whose
    // text is another token — a numeral, a keyword — is neither, and keeps its bars when written back.
    assertEquals(SExprParser.parse("|p1|"), List(SSymbol("p1")))
    assertEquals(SExprParser.parse("|286|"), List(SSymbol("286")))
    assertNotEquals(SExprParser.parse("|286|"), SExprParser.parse("286"))

  test("written back the way SMT-LIB reads it"):
    val input = "(set-info :source |a (b) c|) (assert (= |p1| 1.05)) (assert (= x #x0f)) (echo \"a \"\"b\"\"\")"
    val once = SExprParser.parse(input).mkString(" ")
    assertEquals(once, "(set-info :source |a (b) c|) (assert (= p1 1.05)) (assert (= x #x0f)) (echo \"a \"\"b\"\"\")")
    assertEquals(SExprParser.parse(once).mkString(" "), once)

  test("errors carry what was found"):
    intercept[SExprParser.SyntaxError](SExprParser.parse("(a"))
    intercept[SExprParser.SyntaxError](SExprParser.parse(")"))
