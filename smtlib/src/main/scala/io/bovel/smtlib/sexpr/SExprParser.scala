package io.bovel.smtlib.sexpr

import scala.collection.mutable.ArrayBuffer

/** Reads S-expressions with a regex lexer: tokens are parentheses, comments
 *  (`;` to the end of the line, at a line feed or a carriage return as SMT-LIB
 *  2.6 section 3.1 says, skipped), `|quoted symbols|`, `"strings"` (with `""`
 *  for a quote) and runs of other non-space characters. The class is stateful —
 *  it consumes its input once, with one token of lookahead — so a parser is
 *  used for one input and thrown away; [[SExprParser.parse]] is the way in.
 */
private class SExprParser(input: String):
  import SExprParser.*

  private val tokens = tokenRegex.findAllIn(input).filterNot(_.startsWith(";"))
  private var lookahead: String | Null = null

  // The end of the input is `null`, a token no string equals on any platform: a string of its own
  // told apart by identity was not one on Scala.js, where a string is the engine's primitive, and a
  // script holding the symbol `$EOF` was cut short there (issue 29, again).
  private var ended = false
  private def next(): String | Null =
    if tokens.hasNext then tokens.next()
    else
      ended = true
      null

  private def peek(): String | Null =
    if lookahead == null && !ended then lookahead = next()
    lookahead

  private def eat(): String | Null =
    val token = peek()
    lookahead = null
    token

  private def eat(expected: String): String =
    if peek() == expected then eat().nn
    else throw SyntaxError(s"expected ${show(expected)} but got ${show(peek())}")

  /** Reads one S-expression. */
  def parseOne(): SExpr =
    val token = eat()
    token match
      case "(" =>
        val items = ArrayBuffer.empty[SExpr]
        while peek() != ")" do
          if peek() == null then throw SyntaxError("unexpected end of input: missing ')'")
          items += parseOne()
        eat(")")
        SExpr.SList(items.toList)
      case ")" => throw SyntaxError("unexpected ')'")
      case null => throw SyntaxError("unexpected end of input")
      case atom => classify(atom)

  /** The lexical class of a token (SMT-LIB 2.6, 3.1). Anything that is none of
   *  the literals is a symbol, quoted or not: the lexer accepts more than the
   *  standard writes (`1a`, `#z`), and the layer above says what it means.
   */
  private def classify(atom: String): SExpr =
    if atom.length == 1 && (atom.head < ' ' || atom.head == '\u007f') then
      throw SyntaxError(s"unexpected character (code ${atom.head.toInt})")
    else if atom.startsWith("|") && atom.endsWith("|") && atom.length >= 2 then
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
    while peek() != null do all += parseOne()
    all.toList

object SExprParser:
  /** What [[parse]] throws on malformed input. */
  case class SyntaxError(message: String) extends Exception(message)

  // The whitespace is SMT-LIB's, space, tab, newline and return, spelled out: `\s` is the regex
  // engine's, and Java's has the vertical tab where Scala Native's port of RE2 has not (issue 59). Any
  // other control character is a token of its own, which `classify` refuses, as z3 and cvc5 do.
  private val tokenRegex =
    """;[^\n\r]*|\|[^|]*\||"(?:[^"]|"")*"|[()]|[^\x00-\x20()";|\x7F]+|[\x00-\x08\x0B\x0C\x0E-\x1F\x7F]""".r
  private def show(token: String | Null) = if token == null then "end of input" else s"'$token'"

  /** Reads all the S-expressions of `input`. */
  def parse(input: String): List[SExpr] = SExprParser(input).parseAll()
