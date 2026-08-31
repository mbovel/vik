package net.bovel.sexpr

/** An S-expression: an atom or a parenthesized list. */
enum SExpr:
  /** An atom, kept as written: a symbol, a numeral, a keyword, a
   *  `|quoted symbol|` or a `"string"`.
   */
  case SAtom(text: String)

  /** A parenthesized list of S-expressions. */
  case SList(items: List[SExpr])

  override def toString: String = this match
    case SAtom(text) => text
    case SList(items) => items.mkString("(", " ", ")")
