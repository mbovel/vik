package net.bovel.sexpr

import SExpr.{Atom, SList}

class SExprTest extends munit.FunSuite:

  test("atoms and lists"):
    assertEquals(SExprParser.parse("x"), Atom("x"))
    assertEquals(SExprParser.parse("(f a 12)"), SList(List(Atom("f"), Atom("a"), Atom("12"))))
    assertEquals(
      SExprParser.parse("(f (g a) ())"),
      SList(List(Atom("f"), SList(List(Atom("g"), Atom("a"))), SList(Nil)))
    )

  test("comments, quoted symbols, strings and keywords"):
    val input = """
      ; a comment
      (set-info :status sat) ; trailing comment
      (declare-fun |a b| () U)
      (echo "say ""hi"" now")
    """
    assertEquals(
      SExprParser.parseAll(input),
      List(
        SList(List(Atom("set-info"), Atom(":status"), Atom("sat"))),
        SList(List(Atom("declare-fun"), Atom("|a b|"), SList(Nil), Atom("U"))),
        SList(List(Atom("echo"), Atom("\"say \"\"hi\"\" now\"")))
      )
    )

  test("printing round-trips"):
    val text = "(assert (= (f a) (g b c)))"
    assertEquals(SExprParser.parse(text).toString, text)

  test("errors"):
    intercept[SyntaxError](SExprParser.parse("(f a"))
    intercept[SyntaxError](SExprParser.parse(")"))
    intercept[SyntaxError](SExprParser.parse(""))
    intercept[SyntaxError](SExprParser.parseAll("(a) )"))
