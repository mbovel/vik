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
      case atom => classify(atom)

  /** The lexical class of a token (SMT-LIB 2.6, 3.1). Anything that is none of
   *  the literals is a symbol, quoted or not: the lexer accepts more than the
   *  standard writes (`1a`, `#z`), and the layer above says what it means.
   */
  private def classify(atom: String): SExpr =
    if atom.startsWith("|") && atom.endsWith("|") && atom.length >= 2 then
      SExpr.SSymbol(atom.substring(1, atom.length - 1))
    else if atom.startsWith("\"") then
      SExpr.SString(atom.substring(1, atom.length - 1).replace("\"\"", "\""))
    else if atom.startsWith(":") then SExpr.SKeyword(atom.drop(1))
    else if atom.startsWith("#x") && atom.length > 2 && atom.drop(2).forall(isHexDigit) then
      SExpr.SBits(BigInt(atom.drop(2), 16), 4 * (atom.length - 2), hexadecimal = true)
    else if atom.startsWith("#b") && atom.length > 2 && atom.drop(2).forall(c => c == '0' || c == '1') then
      SExpr.SBits(BigInt(atom.drop(2), 2), atom.length - 2, hexadecimal = false)
    // A token that opens with a digit is a number, since a symbol may not: `BigDecimal` reads the digits
    // and their point in one pass, and says by its scale which of the two numbers it is.
    else if atom.head.isDigit then
      try
        val number = java.math.BigDecimal(atom)
        if number.scale > 0 then SExpr.SDecimal(BigInt(number.unscaledValue.nn), number.scale)
        else SExpr.SNumeral(BigInt(number.toBigIntegerExact.nn))
      catch case _: NumberFormatException => SExpr.SSymbol(atom)
    else SExpr.SSymbol(atom)

  private def isHexDigit(c: Char): Boolean = c.isDigit || (c.toLower >= 'a' && c.toLower <= 'f')

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
