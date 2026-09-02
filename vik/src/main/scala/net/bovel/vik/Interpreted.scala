package net.bovel.vik

/** The interpreted symbols: the bit-vector operations the [[Lowering]] cannot
 *  make linear — a product, a division, a shift or a bitwise operation of two
 *  variables — become applications of `op!w` (`bvmul!8` for the product of two
 *  8-bit vectors, `bvudiv!32`, ...), which the search treats as uninterpreted:
 *  no bit-blasting, the result only bounded to its width. Their meaning is
 *  here, and the solver holds each candidate model to it: an application whose
 *  value disagrees with its symbol's meaning on the values of its arguments
 *  yields a value lemma — these arguments give that result — and the search
 *  goes on with it (see `Solver.solve`). Values are the unsigned readings of
 *  the vectors, as the lowering represents them.
 */
object Interpreted:
  /** The meaning of `name`, when it is an interpreted symbol: a function on the
   *  values of the arguments.
   */
  def apply(name: String): Option[List[BigInt] => BigInt] = name.split('!') match
    case Array(op, w) if w.nonEmpty && w.forall(_.isDigit) => meaning(op, w.toInt).map(f => args => f(args(0), args(1)))
    case _ => None

  /** The SMT-LIB semantics on `w`-bit vectors: division by zero is all ones, a
   *  remainder by zero the dividend, a shift by the width or more all zeros (or
   *  all sign bits), the signed operations through the signed readings.
   */
  private def meaning(op: String, w: Int): Option[(BigInt, BigInt) => BigInt] =
    val m = BigInt(2).pow(w)
    def signed(v: BigInt): BigInt = if v >= m / 2 then v - m else v
    def unsigned(v: BigInt): BigInt = v.mod(m)
    def udiv(a: BigInt, b: BigInt): BigInt = if b.signum == 0 then m - 1 else a / b
    def urem(a: BigInt, b: BigInt): BigInt = if b.signum == 0 then a else a % b
    val f: PartialFunction[String, (BigInt, BigInt) => BigInt] =
      case "bvmul" => (a, b) => unsigned(a * b)
      case "bvudiv" => udiv
      case "bvurem" => urem
      case "bvsdiv" => (a, b) =>
          val q = udiv(signed(a).abs, signed(b).abs)
          if (signed(a).signum < 0) != (signed(b).signum < 0) then unsigned(-q) else q
      case "bvsrem" => (a, b) => // the sign of the dividend
          val r = urem(signed(a).abs, signed(b).abs)
          if signed(a).signum < 0 then unsigned(-r) else r
      case "bvsmod" => (a, b) => // the sign of the divisor
          val r = urem(signed(a).abs, signed(b).abs)
          if r.signum == 0 then r
          else if signed(a).signum >= 0 && signed(b).signum >= 0 then r
          else if signed(a).signum < 0 && signed(b).signum >= 0 then unsigned(signed(b) - r)
          else if signed(a).signum >= 0 then unsigned(signed(b) + r)
          else unsigned(-r)
      case "bvand" => (a, b) => a & b
      case "bvor" => (a, b) => a | b
      case "bvxor" => (a, b) => a ^ b
      case "bvnand" => (a, b) => m - 1 - (a & b)
      case "bvnor" => (a, b) => m - 1 - (a | b)
      case "bvxnor" => (a, b) => m - 1 - (a ^ b)
      case "bvshl" => (a, b) => if b >= w then BigInt(0) else unsigned(a << b.toInt)
      case "bvlshr" => (a, b) => if b >= w then BigInt(0) else a >> b.toInt
      case "bvashr" => (a, b) => unsigned(signed(a) >> (if b >= w then w else b.toInt))
      case "bvcomp" => (a, b) => if a == b then BigInt(1) else BigInt(0)
    f.lift(op)
