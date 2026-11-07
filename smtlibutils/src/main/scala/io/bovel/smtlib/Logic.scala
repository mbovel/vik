package io.bovel.smtlib

/** An SMT-LIB logic as its parts: `QF_AUFBVLIA` is quantifier-free, with
 *  arrays, uninterpreted functions, bit-vectors and linear integers; the
 *  arithmetic is the name's last part (`IDL`, `LIA`, `LRA`, `LIRA`, …).
 */
private[smtlib] final case class Logic(
    quantifierFree: Boolean,
    arrays: Boolean,
    functions: Boolean,
    bitVectors: Boolean,
    datatypes: Boolean,
    arithmetic: String
):
  def name: String =
    val others = functions || bitVectors || datatypes || arithmetic.nonEmpty
    (if quantifierFree then "QF_" else "") + (if arrays then (if others then "A" else "AX") else "") +
      (if functions then "UF" else "") + (if bitVectors then "BV" else "") + (if datatypes then "DT" else "") + arithmetic

  /** How many theories it has: integers and reals count apart. */
  def size: Int =
    List(arrays, functions, bitVectors, datatypes).count(identity) + Logic.arithmetics.getOrElse(arithmetic, 0)

  /** The logics with one part less: a theory dropped, or the integers or the
   *  reals of a mixed arithmetic.
   */
  def smaller: List[Logic] =
    val split = arithmetic match
      case "LIRA" => List("LIA", "LRA")
      case "NIRA" => List("NIA", "NRA")
      case _ => Nil
    List(
      Option.when(arrays)(copy(arrays = false)),
      Option.when(functions)(copy(functions = false)),
      Option.when(bitVectors)(copy(bitVectors = false)),
      Option.when(datatypes)(copy(datatypes = false)),
      Option.when(arithmetic.nonEmpty)(copy(arithmetic = ""))
    ).flatten ++ split.map(a => copy(arithmetic = a))

object Logic:
  private val arithmetics = Map("IDL" -> 1, "RDL" -> 1, "LIA" -> 1, "LRA" -> 1, "NIA" -> 1, "NRA" -> 1, "LIRA" -> 2, "NIRA" -> 2)
  private val pattern = "(QF_)?(AX|A)?(UF)?(BV)?(DT)?(IDL|RDL|LIRA|LIA|LRA|NIRA|NIA|NRA)?".r

  /** The quantifier-free logics of SMT-LIB that the corpus's theories make,
   *  and the corpus's own `QF_UFBVDT`: z3 takes a name it does not know for
   *  `ALL`, so only these are tried.
   */
  val standard: Set[String] = Set(
    "QF_ABV", "QF_ALIA", "QF_ANIA", "QF_AUFBV", "QF_AUFBVLIA", "QF_AUFLIA", "QF_AUFNIA", "QF_AX", "QF_BV", "QF_BVLRA",
    "QF_DT", "QF_IDL", "QF_LIA", "QF_LIRA", "QF_LRA", "QF_NIA", "QF_NIRA", "QF_NRA", "QF_RDL", "QF_UF", "QF_UFBV",
    "QF_UFBVDT", "QF_UFBVLIA", "QF_UFDT", "QF_UFDTLIA", "QF_UFDTLIRA", "QF_UFIDL", "QF_UFLIA", "QF_UFLRA", "QF_UFNIA",
    "QF_UFNRA"
  )

  def parse(name: String): Option[Logic] = name match
    case pattern(qf, a, uf, bv, dt, arith) if name.nonEmpty && name != "QF_" =>
      Some(Logic(qf != null, a != null, uf != null, bv != null, dt != null, if arith == null then "" else arith))
    case _ => None
