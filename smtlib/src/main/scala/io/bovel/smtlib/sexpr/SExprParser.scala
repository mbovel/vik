package io.bovel.smtlib.sexpr

import scala.collection.mutable.ArrayBuffer

/** Reads S-expressions with a hand-written lexer: tokens are parentheses,
 *  comments (`;` to the end of the line, skipped), `|quoted symbols|`,
 *  `"strings"` (with `""` for a quote) and runs of other non-space characters.
 *  The class is stateful — it consumes its input once, with one token of
 *  lookahead — so a parser is used for one input and thrown away;
 *  [[SExprParser.parse]] is the way in.
 */
private class SExprParser(input: String):
  import SExprParser.*

  private var position = 0
  private var lookahead: String | Null = null

  // The end of the input is `null`, a token no string equals on any platform: a string of its own
  // told apart by identity was not one on Scala.js, where a string is the engine's primitive, and a
  // script holding the symbol `$EOF` was cut short there (issue 29, again).
  private var ended = false

  /** The next token, by hand: the lexer is a tenth of the time of a session of
   *  small queries as a regex. The tokens are the ones of [[tokenPattern]], the
   *  alternatives tried in its order at every position, and a character that
   *  starts none of them skipped, as `findAllIn` would.
   */
  private[sexpr] def next(): String | Null =
    val length = input.length
    var token: String | Null = null
    while token == null && position < length do
      val start = position
      val c = input.charAt(start)
      if c == ';' then // a comment, to the end of the line: skipped
        position = input.indexOf('\n', start)
        if position < 0 then position = length
      else if c == '|' then
        val close = input.indexOf('|', start + 1)
        if close < 0 then position += 1
        else
          position = close + 1
          token = input.substring(start, position)
      else if c == '"' then
        // Greedy, `""` a quote inside: without a closing quote, the string ends at the last `""`.
        var i = start + 1
        var end = -1
        var lastPair = -1
        while end < 0 && i < length do
          if input.charAt(i) != '"' then i += 1
          else if i + 1 < length && input.charAt(i + 1) == '"' then
            lastPair = i
            i += 2
          else end = i
        if end < 0 then end = lastPair
        if end < 0 then position += 1
        else
          position = end + 1
          token = input.substring(start, position)
      else if c == '(' then
        position += 1
        token = "("
      else if c == ')' then
        position += 1
        token = ")"
      else if inSymbol(c) then
        var i = start + 1
        while i < length && inSymbol(input.charAt(i)) do i += 1
        position = i
        token = input.substring(start, i)
      else if c == ' ' || c == '\t' || c == '\n' || c == '\r' then position += 1
      else // another control character: a token of its own, which `classify` refuses
        position += 1
        token = c.toString
    if token == null then ended = true
    token

  private def inSymbol(c: Char): Boolean =
    c > ' ' && c != '(' && c != ')' && c != '"' && c != ';' && c != '|' && c != '\u007f'

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

  // The whitespace is SMT-LIB's, space, tab, newline and return, spelled out: Java's `\s` has the
  // vertical tab where Scala Native's port of RE2 has not (issue 59). Any other control character is
  // a token of its own, which `classify` refuses, as z3 and cvc5 do. The lexer reads these tokens by
  // hand (see `next`); the pattern is their definition, and what the tests hold the lexer to.
  private[sexpr] val tokenPattern =
    """;[^\n]*|\|[^|]*\||"(?:[^"]|"")*"|[()]|[^\x00-\x20()";|\x7F]+|[\x00-\x08\x0B\x0C\x0E-\x1F\x7F]""".r

  /** The tokens of `input`, comments included, as the lexer reads them. */
  private[sexpr] def tokens(input: String): List[String] =
    val parser = SExprParser(input)
    Iterator.continually(parser.next()).takeWhile(_ != null).map(_.nn).toList

  private def show(token: String | Null) = if token == null then "end of input" else s"'$token'"

  /** Reads all the S-expressions of `input`. */
  def parse(input: String): List[SExpr] = SExprParser(input).parseAll()
