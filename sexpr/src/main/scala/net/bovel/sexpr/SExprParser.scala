package net.bovel.sexpr

import scala.collection.mutable.ArrayBuffer

/** Reads S-expressions with a regex lexer: tokens are parentheses, comments
 *  (`;` to the end of the line, skipped), `|quoted symbols|`, `"strings"` (with
 *  `""` for a quote) and runs of other non-space characters. The class is
 *  stateful — it consumes its input once, with one token of lookahead — so a
 *  parser is used for one input and thrown away; [[SExprParser.parse]] is the
 *  way in.
 */
private class SExprParser(input: String):
  import SExprParser.*

  private val tokens = tokenRegex.findAllIn(input).filterNot(_.startsWith(";"))
  private var lookahead: String | Null = null

  private def next(): String = if tokens.hasNext then tokens.next() else EOF

  private def peek(): String =
    if lookahead == null then lookahead = next()
    lookahead.nn

  private def eat(): String =
    val token = peek()
    lookahead = null
    token

  private def eat(expected: String): String =
    if peek() == expected then eat()
    else throw SyntaxError(s"expected ${show(expected)} but got ${show(peek())}")

  /** Reads one S-expression. */
  def parseOne(): SExpr =
    val token = eat()
    token match
      case "(" =>
        val items = ArrayBuffer.empty[SExpr]
        while peek() != ")" do
          if peek() == EOF then throw SyntaxError("unexpected end of input: missing ')'")
          items += parseOne()
        eat(")")
        SExpr.SList(items.toList)
      case ")" => throw SyntaxError("unexpected ')'")
      case EOF => throw SyntaxError("unexpected end of input")
      // A quoted symbol whose text is a symbol on its own is that symbol: `|p1|` and `p1` are one
      // (SMT-LIB 2.6, 3.1). `|286|` keeps its bars — the numeral 286 is another token.
      case atom
          if atom.length >= 2 && atom.startsWith("|") && atom.endsWith("|")
            && SExpr.simple(atom.substring(1, atom.length - 1)) =>
        SExpr.SAtom(atom.substring(1, atom.length - 1))
      case atom => SExpr.SAtom(atom)

  /** Reads all the S-expressions of the input. */
  def parseAll(): List[SExpr] =
    val all = ArrayBuffer.empty[SExpr]
    while peek() != EOF do all += parseOne()
    all.toList

object SExprParser:
  /** What [[parse]] throws on malformed input. */
  case class SyntaxError(message: String) extends Exception(message)

  private val tokenRegex = """;[^\n]*|\|[^|]*\||"(?:[^"]|"")*"|[()]|[^\s()";|]+""".r
  private val EOF = "$EOF"
  private def show(token: String) = if token == EOF then "end of input" else s"'$token'"

  /** Reads all the S-expressions of `input`. */
  def parse(input: String): List[SExpr] = SExprParser(input).parseAll()
