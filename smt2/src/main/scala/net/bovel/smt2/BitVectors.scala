package net.bovel.smt2

/** The vocabulary of the arithmetic fragment of bit-vectors: the sort
 *  `(_ BitVec n)` and the operation names the parser reads into [[Term.Bv]].
 *  The solver lowers the terms to integers before solving (the solver's
 *  `Lowering`).
 */
object BitVectors:
  private val Sort = """\(_ BitVec (\d+)\)""".r

  /** The width of a bit-vector sort, -1 for another sort. */
  def width(sort: String): Int = sort match
    case Sort(n) => n.nn.toInt
    case _ => -1

  def sort(width: Int): String = s"(_ BitVec $width)"

  object Arithmetic:
    val names: Set[String] =
      Set("bvadd", "bvsub", "bvneg", "bvmul", "bvnot", "bvshl", "bvlshr", "bvudiv", "bvurem", "bvand")
    def unapply(name: String): Option[String] = Option.when(names(name))(name)

  object Comparison:
    val names: Set[String] = Set("bvult", "bvule", "bvugt", "bvuge", "bvslt", "bvsle", "bvsgt", "bvsge")
    def unapply(name: String): Option[String] = Option.when(names(name))(name)
