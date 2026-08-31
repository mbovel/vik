package net.bovel.sexpr

import SExpr.{SAtom, SList}

class SExprTest extends munit.FunSuite:

  test("atoms and lists"):
    assertEquals(SExprParser.parse("x"), List(SAtom("x")))
    assertEquals(SExprParser.parse("(f a 12)"), List(SList(List(SAtom("f"), SAtom("a"), SAtom("12")))))
    assertEquals(
      SExprParser.parse("(f (g a) ())"),
      List(SList(List(SAtom("f"), SList(List(SAtom("g"), SAtom("a"))), SList(Nil))))
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
        SList(List(SAtom("set-info"), SAtom(":status"), SAtom("sat"))),
        SList(List(SAtom("declare-fun"), SAtom("|a b|"), SList(Nil), SAtom("U"))),
        SList(List(SAtom("echo"), SAtom("\"say \"\"hi\"\" now\"")))
      )
    )

  test("errors carry what was found"):
    intercept[SExprParser.SyntaxError](SExprParser.parse("(a"))
    intercept[SExprParser.SyntaxError](SExprParser.parse(")"))
