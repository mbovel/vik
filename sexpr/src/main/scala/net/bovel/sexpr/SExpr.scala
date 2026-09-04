package net.bovel.sexpr

/** An S-expression: a list, or one of the lexical classes SMT-LIB 2.6 defines
 *  in 3.1 — a symbol, a keyword, and the literals. Each holds what it means,
 *  not how it was written: a `|quoted symbol|` holds the text between its bars,
 *  since that text is the symbol (`|p1|` and `p1` are one), and a string holds
 *  its characters, not its quotes. [[toString]] writes each back the way
 *  SMT-LIB reads it.
 */
enum SExpr:
  /** A symbol, simple (`declare-fun`, `x!3`) or quoted (`|a b|`). */
  case SSymbol(name: String)

  /** A keyword, `:status`, by its name: `status`. */
  case SKeyword(name: String)

  /** A numeral, `0` or a digit string with no leading zero. */
  case SNumeral(value: BigInt)

  /** A decimal, `1.05`, by its digits: `unscaled × 10^(-scale)`. */
  case SDecimal(unscaled: BigInt, scale: Int)

  /** A hexadecimal (`#x1Fa`) or binary (`#b0110`) literal, by its value and the
   *  number of bits it was written with.
   */
  case SBits(value: BigInt, width: Int, hexadecimal: Boolean)

  /** A string literal, by its characters (`""` inside it is one quote). */
  case SString(value: String)

  /** A parenthesized list of S-expressions. */
  case SList(items: List[SExpr])

  override def toString: String = this match
    case SSymbol(name) => if SExpr.simple(name) then name else s"|$name|"
    case SKeyword(name) => s":$name"
    case SNumeral(value) => value.toString
    case SDecimal(unscaled, scale) =>
      val digits = unscaled.abs.toString.reverse.padTo(scale + 1, '0').reverse
      val sign = if unscaled.signum < 0 then "-" else ""
      s"$sign${digits.dropRight(scale)}.${digits.takeRight(scale)}"
    case SBits(value, width, hexadecimal) =>
      if hexadecimal then s"#x${value.toString(16).reverse.padTo(width / 4, '0').reverse}"
      else s"#b${value.toString(2).reverse.padTo(width, '0').reverse}"
    case SString(value) => "\"" + value.replace("\"", "\"\"") + "\""
    case SList(items) => items.mkString("(", " ", ")")

object SExpr:
  /** Whether the name is a symbol SMT-LIB writes without bars: the letters, the
   *  digits and `~!@$%^&*_-+=<>.?/`, not starting with a digit — so `p1` needs
   *  none, while `a b`, `286` and `:k` do, being otherwise another token.
   */
  def simple(name: String): Boolean =
    name.nonEmpty && !name.head.isDigit &&
      name.forall(c => c.isLetterOrDigit || "~!@$%^&*_-+=<>.?/".contains(c))
