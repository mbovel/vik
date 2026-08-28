package net.bovel.sexpr

import scala.collection.mutable.ArrayBuffer

/** An S-expression: an atom (a symbol, a numeral, a keyword, a
 *  `|quoted symbol|` or a `"string"`, kept as written) or a parenthesized list.
 */
enum SExpr:
  case Atom(text: String)
  case SList(items: List[SExpr])

  override def toString: String = this match
    case Atom(text) => text
    case SList(items) => items.mkString("(", " ", ")")

case class SyntaxError(message: String) extends Exception(message)

/** Reads S-expressions with a regex lexer: tokens are parentheses, comments
 *  (`;` to the end of the line, skipped), `|quoted symbols|`, `"strings"` (with
 *  `""` for a quote) and runs of other non-space characters.
 */
class SExprParser(input: String):
  import SExprParser.*

  private val tokens = tokenRegex.findAllIn(input).filterNot(_.startsWith(";"))
  private val lookahead = ArrayBuffer.empty[String]

  private def next(): String = if tokens.hasNext then tokens.next() else EOF

  def peek(): String =
    if lookahead.isEmpty then lookahead += next()
    lookahead(0)

  def eat(): String = if lookahead.isEmpty then next() else lookahead.remove(0)

  def eat(expected: String): String =
    if peek() == expected then eat()
    else throw SyntaxError(s"expected ${show(expected)} but got ${show(peek())}")

  def atEnd: Boolean = peek() == EOF

  /** Reads one S-expression. */
  def parse(): SExpr =
    val token = eat()
    token match
      case "(" =>
        val items = ArrayBuffer.empty[SExpr]
        while peek() != ")" do
          if atEnd then throw SyntaxError("unexpected end of input: missing ')'")
          items += parse()
        eat(")")
        SExpr.SList(items.toList)
      case ")" => throw SyntaxError("unexpected ')'")
      case EOF => throw SyntaxError("unexpected end of input")
      case atom => SExpr.Atom(atom)

  /** Reads all the S-expressions of the input. */
  def parseAll(): List[SExpr] =
    val all = ArrayBuffer.empty[SExpr]
    while !atEnd do all += parse()
    all.toList

object SExprParser:
  private val tokenRegex = """;[^\n]*|\|[^|]*\||"(?:[^"]|"")*"|[()]|[^\s()";|]+""".r
  private val EOF = "$EOF"
  private def show(token: String) = if token == EOF then "end of input" else s"'$token'"

  def parse(input: String): SExpr = SExprParser(input).parse()
  def parseAll(input: String): List[SExpr] = SExprParser(input).parseAll()
