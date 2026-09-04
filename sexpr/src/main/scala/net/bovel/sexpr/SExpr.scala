package net.bovel.sexpr

/** An S-expression: an atom or a parenthesized list. */
enum SExpr:
  /** An atom: a symbol, a numeral, a keyword or a `"string"`, as written —
   *  except a `|quoted symbol|`, which holds the text between the bars, since
   *  that text is the symbol (SMT-LIB 2.6, 3.1: `|p1|` and `p1` are one
   *  symbol). [[toString]] puts the bars back where the text needs them.
   */
  case SAtom(text: String)

  /** A parenthesized list of S-expressions. */
  case SList(items: List[SExpr])

  override def toString: String = this match
    case SAtom(text) => if text.startsWith("|") || !SExpr.barred(text) then text else s"|$text|"
    case SList(items) => items.mkString("(", " ", ")")

object SExpr:
  /** Whether a symbol needs its bars to lex back as one atom: anything a simple
   *  symbol cannot hold, an empty name included. A string keeps its own quotes
   *  and needs none.
   */
  def barred(text: String): Boolean =
    !text.startsWith("\"") &&
      (text.isEmpty || text.exists(c => c.isWhitespace || c == '(' || c == ')' || c == ';' || c == '|' || c == '"'))

  /** Whether the text is a symbol SMT-LIB writes without bars: the letters, the
   *  digits and `~!@$%^&*_-+=<>.?/`, not starting with a digit — so `|p1|` is
   *  the symbol `p1`, while `|286|` and `|:k|` keep their bars, being neither
   *  the numeral nor the keyword.
   */
  def simple(text: String): Boolean =
    text.nonEmpty && !text.head.isDigit &&
      text.forall(c => c.isLetterOrDigit || "~!@$%^&*_-+=<>.?/".contains(c))
