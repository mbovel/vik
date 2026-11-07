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

  private def ints = arithmetic.contains("I")
  private def reals = arithmetic.contains("R")

  /** How many theories it has, integers and reals apart. */
  def theories: Int = List(arrays, functions, bitVectors, datatypes, ints, reals).count(identity)

  /** How strong its arithmetic is: none, difference logic, linear, nonlinear. */
  def strength: Int = arithmetic.headOption.fold(0)(c => if c == 'N' then 3 else if arithmetic.endsWith("DL") then 1 else 2)

  /** Whether everything it allows, `other` allows. */
  def within(other: Logic): Boolean =
    (!arrays || other.arrays) && (!functions || other.functions) && (!bitVectors || other.bitVectors) &&
      (!datatypes || other.datatypes) && (!ints || other.ints) && (!reals || other.reals) && strength <= other.strength &&
      (quantifierFree || !other.quantifierFree)

object Logic:
  private val pattern = "(QF_)?(AX|A)?(UF)?(BV)?(DT)?(IDL|RDL|LIRA|LIA|LRA|NIRA|NIA|NRA)?".r
  private val setLogic = "\\(set-logic ([A-Za-z_]+)\\)".r

  /** The quantifier-free logics of SMT-LIB that the corpus's theories make,
   *  and the corpus's own `QF_UFBVDT`; on a tie, the earlier wins. z3 takes a
   *  name it does not know for `ALL`, so only these are tried.
   */
  val standard: List[String] = List(
    "QF_UF", "QF_IDL", "QF_RDL", "QF_LIA", "QF_LRA", "QF_NIA", "QF_NRA", "QF_LIRA", "QF_NIRA", "QF_BV", "QF_DT", "QF_AX",
    "QF_UFIDL", "QF_UFLIA", "QF_UFLRA", "QF_UFNIA", "QF_UFNRA", "QF_UFBV", "QF_UFDT", "QF_ABV", "QF_ALIA", "QF_ANIA",
    "QF_BVLRA", "QF_UFDTLIA", "QF_UFBVDT", "QF_UFBVLIA", "QF_AUFBV", "QF_AUFLIA", "QF_AUFNIA", "QF_UFDTLIRA",
    "QF_AUFBVLIA"
  )

  def parse(name: String): Option[Logic] = name match
    case pattern(qf, a, uf, bv, dt, arith) if name.nonEmpty && name != "QF_" =>
      Some(Logic(qf != null, a != null, uf != null, bv != null, dt != null, if arith == null then "" else arith))
    case _ => None

  /** The logics the script sets, without repeats; a comment sets none. */
  def claimed(text: String): List[String] =
    text.linesIterator.flatMap(l => setLogic.findAllMatchIn(code(l)).map(_.group(1).nn)).toList.distinct

  // A line without its comment.
  private def code(line: String) = line.takeWhile(_ != ';')

  /** One script per feature a logic may lack, each using that feature alone. */
  private def features(l: Logic): List[String] =
    val nonlinear = "(declare-fun x () T)(assert (= (* x x) (* x x)))"
    val linear = "(declare-fun x () T)(declare-fun y () T)(assert (<= (+ x y) (+ y x)))"
    List(
      Option.when(!l.arrays)("(declare-fun a () (Array Bool Bool))(assert (select a true))"),
      Option.when(!l.functions)("(declare-fun f (Bool) Bool)(assert (f true))"),
      Option.when(!l.bitVectors)("(declare-fun x () (_ BitVec 4))(assert (= x x))"),
      Option.when(!l.datatypes)("(declare-datatypes ((D 0)) (((c) (d))))(declare-fun x () D)(assert (= x c))"),
      Option.when(!l.ints)("(declare-fun x () Int)(assert (= x x))"),
      Option.when(!l.reals)("(declare-fun x () Real)(assert (= x x))"),
      Option.when(l.ints && l.strength < 3)(nonlinear.replace("T", "Int")),
      Option.when(l.reals && l.strength < 3)(nonlinear.replace("T", "Real")),
      Option.when(l.ints && l.strength == 1)(linear.replace("T", "Int")),
      Option.when(l.reals && l.strength == 1)(linear.replace("T", "Real")),
      Option.when(l.quantifierFree)("(assert (forall ((b Bool)) b))")
    ).flatten

  /** The standard logics z3 holds a script to: it refuses each feature they
   *  lack. A name z3 does not know it takes for `ALL`, and some logics it knows
   *  only in part; a logic it lets one feature through is no judge.
   */
  lazy val enforced: List[Logic] =
    standard.flatMap(parse).filter(l => features(l).forall(f => z3Error(s"(set-logic ${l.name})\n$f\n").nonEmpty))

  /** The smallest standard logic the script fits: `QF_UF` if it does, else
   *  the fewest theories, then one within the logic the script sets, then the
   *  weakest arithmetic, then the earlier in [[standard]]. Only the logics z3
   *  holds a script to ([[enforced]]) are tried, and, when the script sets a
   *  standard logic, only those within it: that logic is the answer when none
   *  of them fits. Difference logic (`QF_IDL`, `QF_RDL`, `QF_UFIDL`) is tried
   *  only for a script that sets one: z3 holds a script to less than SMT-LIB's
   *  definition (it lets `(<= x 5)` through, which is none of the standard's
   *  atoms), and VIK solves it as linear arithmetic. A logic fits when z3 reads the script under it (z3 holds a
   *  script to its logic's signature: `Int` is unknown under `QF_UFDT`, and so
   *  are a numeral, a quantifier, a product of two unknowns under `QF_LIA` and
   *  a sum under `QF_IDL`), VIK's parser still reads it, and it has `UF` or
   *  arrays when the script declares a sort, which z3 lets through. None for a
   *  script that sets two logics, that z3 refuses as it is, or that sets no
   *  standard logic and fits none.
   */
  def smallest(text: String): Option[String] =
    claimed(text) match
      case names if names.length > 1 || z3Error(text).nonEmpty => None
      case names =>
        val claim = names.headOption.filter(standard.contains).flatMap(parse)
        val sorts = text.contains("(declare-sort ")
        def fits(l: Logic) =
          val t = relabel(text, l.name)
          (l.functions || l.arrays || !sorts) && z3Error(t).isEmpty && vikError(t).isEmpty
        val difference = claim.exists(_.strength == 1)
        val candidates = enforced.filter(l => claim.forall(l.within) && (l.strength != 1 || difference)).zipWithIndex
          .sortBy((l, i) => (l.theories, !claim.exists(l.within), l.strength, i)).map(_._1)
        // Booleans and uninterpreted symbols alone fit any logic: they are `QF_UF`'s.
        candidates.find(_.name == "QF_UF").filter(fits).orElse(candidates.find(fits)).orElse(claim).map(_.name)

  /** The script under another logic: its `set-logic` changed, or one added
   *  after its header, and a header line saying so.
   */
  def relabel(text: String, to: String): String =
    val claim = claimed(text).headOption
    val lines = text.linesIterator.toList
    val header = lines.takeWhile(_.startsWith(";"))
    val note = s"; logic: $to, the smallest the script fits${claim.fold(", which set none")(c => s", for $c")} (prune logic)"
    val body = lines.drop(header.length).map(l => setLogic.replaceAllIn(code(l), s"(set-logic $to)") + l.drop(code(l).length))
    (header ++ (note :: (if claim.isEmpty then s"(set-logic $to)" :: body else body))).mkString("", "\n", "\n")
