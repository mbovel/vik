package net.bovel.smtlib

/** A sort, as SMT-LIB writes it. `(Array I E)` is read as written and becomes a
 *  function sort in the solver's lowering. `Named` covers the uninterpreted
 *  sorts and the datatypes — which of the two a name is, the declarations say.
 *  `Fun` is the sort of a declared function symbol, which the parser keeps to
 *  type its applications: no script writes it, a function symbol is applied,
 *  never a value.
 */
enum Sort:
  case Bool
  case Int
  case Real
  case BitVec(width: scala.Int)
  case Array(index: Sort, element: Sort)
  case Named(name: String)
  case Fun(args: List[Sort], result: Sort)

  /** The sort as written. */
  def text: String = this match
    case Bool => "Bool"
    case Int => "Int"
    case Real => "Real"
    case BitVec(width) => s"(_ BitVec $width)"
    case Array(index, element) => s"(Array ${index.text} ${element.text})"
    case Named(name) => name
    case Fun(args, result) => s"(-> ${(args.map(_.text) :+ result.text).mkString(" ")})"

  override def toString: String = text

  /** The parameter sorts and the result of a function sort ([[Sort.fun]] keeps
   *  them flattened); `None` for a value sort.
   */
  def arrow: Option[(List[Sort], Sort)] = this match
    case Fun(args, result) => Some((args, result))
    case _ => None

  /** The sort of a term of this sort applied to `n` arguments, `None` when it
   *  is not a function sort of exactly that many.
   */
  def applied(n: scala.Int): Option[Sort] = this match
    case Fun(args, result) if n == args.length => Some(result)
    case _ => None

object Sort:
  /** The function sort of `args` to `result` (`result` itself when there are
   *  none), nested arrows in the result flattened: `(-> A (-> B C))` is
   *  `(-> A B C)`.
   */
  def fun(args: List[Sort], result: Sort): Sort =
    if args.isEmpty then result
    else
      result match
        case Fun(more, r) => Fun(args ++ more, r)
        case _ => Fun(args, result)
