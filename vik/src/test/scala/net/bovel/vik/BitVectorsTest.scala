package net.bovel.vik

import scala.util.Random

import net.bovel.smtlib.Parser

/** The lowering of bit-vector arithmetic to integers and the interpreted
 *  symbols of the rest: hand cases, and random 4-bit expressions whose value
 *  under an assignment is computed directly and compared with what the solver
 *  accepts.
 */
class BitVectorsTest extends munit.FunSuite:
  def sat(script: String): Boolean = Session.answers(Parser.parse(script)) == List(Some(true))

  val decls = "(set-logic QF_BV) (declare-const x (_ BitVec 8)) (declare-const y (_ BitVec 8))"

  test("get-assertions shows the lowered stack"):
    val out = Session.run(Parser.parse(s"$decls (assert (bvult x y)) (get-assertions)"))
    assert(out.last.contains("(<= x 255)"), out.last) // the range of a declared bit-vector
    assert(out.last.contains("(<= (+ x 1) y)"), out.last) // bvult, lowered

  test("overflow, comparisons, extraction"):
    assert(sat(s"$decls (assert (= (bvadd x #xFF) #x00)) (assert (= x #x01)) (check-sat)"))
    assert(!sat(s"$decls (assert (= x #xFF)) (assert (bvult x (bvadd x #x01))) (check-sat)"))
    assert(sat(s"$decls (assert (= x #xFE)) (assert (bvult x (bvadd x #x01))) (check-sat)"))
    assert(
      sat(s"$decls (assert (bvslt x #x00)) (assert (bvugt x #x7F)) (check-sat)")
    ) // negative signed = high unsigned
    assert(!sat(s"$decls (assert (bvslt x #x00)) (assert (bvult x #x80)) (check-sat)"))
    assert(sat(
      s"$decls (assert (= ((_ extract 7 4) x) #x3)) (assert (= ((_ extract 3 0) x) #xA)) (assert (= x #x3A)) (check-sat)"
    ))
    assert(!sat(s"$decls (assert (= ((_ extract 7 4) x) #x3)) (assert (= x #x2A)) (check-sat)"))
    assert(
      sat(s"$decls (assert (= (concat ((_ extract 7 4) x) ((_ extract 3 0) x)) x)) (assert (= x #xC7)) (check-sat)")
    )
    assert(sat(s"$decls (assert (= (bvmul x #x03) #x01)) (check-sat)")) // 3 × 171 = 513 = 1 mod 256
    assert(!sat(s"$decls (assert (= (bvmul x #x02) #x01)) (check-sat)"))
    assert(sat(s"$decls (assert (= ((_ sign_extend 8) x) (_ bv65535 16))) (assert (= x #xFF)) (check-sat)"))
    assert(
      sat(s"$decls (assert (= (bvudiv x #x03) #x05)) (assert (= (bvurem x #x03) #x02)) (assert (= x #x11)) (check-sat)")
    )
    assert(
      sat(s"$decls (assert (= (bvand x #x0F) #x07)) (assert (= (bvlshr x #x04) #x0A)) (assert (= x #xA7)) (check-sat)")
    )
    assert(sat(s"$decls (assert (= (bvnot x) (bvneg (bvadd x #x01)))) (check-sat)")) // an identity: sat for every x
    assert(!sat(s"$decls (assert (distinct (bvnot x) (bvneg (bvadd x #x01)))) (check-sat)"))

  test("operations of two variables: interpreted symbols, checked against the candidate models"):
    val small = "(set-logic QF_BV) (declare-const x (_ BitVec 4)) (declare-const y (_ BitVec 4))"
    assert(sat(s"$small (assert (= (bvmul x y) #x1)) (check-sat)")) // 1 × 1, 3 × 11, ...
    assert(!sat(s"$small (assert (= x #x2)) (assert (= (bvmul x y) #x7)) (check-sat)")) // 2y is even
    assert(sat(s"$small (assert (= x #x2)) (assert (= (bvmul x y) #x6)) (assert (= y #x3)) (check-sat)"))
    assert(!sat(s"$small (assert (= x #x2)) (assert (= (bvmul x y) #x6)) (assert (= y #x4)) (check-sat)"))
    assert(sat(s"$small (assert (= (bvor x y) #xf)) (assert (= (bvand x y) #x0)) (assert (= x #x5)) (check-sat)"))
    assert(!sat(
      s"$small (assert (= (bvor x y) #xf)) (assert (= (bvand x y) #x0)) (assert (= x #x5)) (assert (= y #x8)) (check-sat)"
    ))
    assert(
      !sat(s"$small (assert (= (bvudiv x y) #x3)) (assert (= y #x0)) (assert (= x #x3)) (check-sat)")
    ) // x ÷ 0 = 15
    assert(sat(s"$small (assert (= (bvudiv x y) #xf)) (assert (= y #x0)) (check-sat)"))
    assert(sat(s"$small (assert (= (bvshl x y) #x8)) (assert (= y #x3)) (assert (= x #x1)) (check-sat)"))
    assert(!sat(s"$small (assert (= (bvshl x y) #x8)) (assert (= y #x4)) (check-sat)")) // a shift by the width: 0
    // The values are the symbol's meaning, in the model too.
    val out = Session.run(Parser.parse(
      s"$small (assert (= x #x7)) (assert (= y #x3)) (assert (bvult (bvmul x y) #xf)) (check-sat) (get-value ((bvmul x y) (bvand x y)))"
    ))
    assertEquals(out, List("sat", "(((bvmul x y) 5) ((bvand x y) 3))"))

  /** A random 4-bit expression over `x` and `y` as SMT-LIB text, with its value
   *  under `x = vx`, `y = vy`.
   */
  def expression(rnd: Random, depth: Int, vx: Int, vy: Int): (String, Int) =
    def lit(v: Int) = f"#x$v%x"
    if depth == 0 then
      rnd.nextInt(3) match
        case 0 => ("x", vx)
        case 1 => ("y", vy)
        case _ =>
          val v = rnd.nextInt(16)
          (lit(v), v)
    else
      val (a, va) = expression(rnd, depth - 1, vx, vy)
      val (b, vb) = expression(rnd, depth - 1, vx, vy)
      val c = rnd.nextInt(16)
      def signed(v: Int) = if v >= 8 then v - 16 else v
      rnd.nextInt(21) match
        case 11 => (s"(bvmul $a $b)", (va * vb) & 15)
        case 12 => (s"(bvand $a $b)", va & vb)
        case 13 => (s"(bvor $a $b)", va | vb)
        case 14 => (s"(bvxor $a $b)", va ^ vb)
        case 15 => (s"(bvudiv $a $b)", if vb == 0 then 15 else va / vb)
        case 16 => (s"(bvurem $a $b)", if vb == 0 then va else va % vb)
        case 17 => (s"(bvshl $a $b)", if vb >= 4 then 0 else (va << vb) & 15)
        case 18 => (s"(bvlshr $a $b)", if vb >= 4 then 0 else va >>> vb)
        case 19 => (s"(bvashr $a $b)", (signed(va) >> (vb min 4)) & 15)
        case 20 =>
          rnd.nextInt(3) match
            case 0 => (
                s"(bvsdiv $a $b)",
                (if signed(vb) == 0 then (if signed(va) >= 0 then -1 else 1) else signed(va) / signed(vb)) & 15
              )
            case 1 => (s"(bvsrem $a $b)", (if signed(vb) == 0 then signed(va) else signed(va) % signed(vb)) & 15)
            case _ =>
              (s"(bvsmod $a $b)", (if signed(vb) == 0 then signed(va) else Math.floorMod(signed(va), signed(vb))) & 15)
        case 0 => (s"(bvadd $a $b)", (va + vb) & 15)
        case 1 => (s"(bvsub $a $b)", (va - vb) & 15)
        case 2 => (s"(bvneg $a)", (-va) & 15)
        case 3 => (s"(bvnot $a)", (~va) & 15)
        case 4 => (s"(bvmul $a ${lit(c)})", (va * c) & 15)
        case 5 => (s"(bvshl $a ${lit(c % 5)})", (va << (c % 5)) & 15)
        case 6 => (s"(bvlshr $a ${lit(c % 5)})", va >>> (c % 5))
        case 7 => (s"(bvudiv $a ${lit(c)})", if c == 0 then 15 else va / c)
        case 8 => (s"(bvurem $a ${lit(c)})", if c == 0 then va else va % c)
        case 9 =>
          val i = rnd.nextInt(4)
          val j = rnd.nextInt(i + 1)
          (s"((_ zero_extend ${3 - i + j}) ((_ extract $i $j) $a))", (va >>> j) & ((1 << (i - j + 1)) - 1))
        case _ =>
          val mask = List(0, 1, 3, 7, 15, 14, 12, 8)(rnd.nextInt(8))
          (s"(bvand $a ${lit(mask)})", va & mask)
  end expression

  test("random 4-bit expressions against direct evaluation"):
    val rnd = Random(5)
    for round <- 0 until 150 do
      val vx = rnd.nextInt(16)
      val vy = rnd.nextInt(16)
      val (e, v) = expression(rnd, 2, vx, vy)
      val wrong = (v + 1 + rnd.nextInt(15)) & 15
      def script(value: Int) =
        f"(declare-const x (_ BitVec 4)) (declare-const y (_ BitVec 4)) (assert (= x #x$vx%x)) (assert (= y #x$vy%x)) (assert (= $e #x$value%x)) (check-sat)"
      assert(sat(script(v)), s"round $round: $e should be ${v} under x = $vx, y = $vy")
      assert(!sat(script(wrong)), s"round $round: $e is not $wrong under x = $vx, y = $vy")

  test("signed comparisons against direct evaluation"):
    val rnd = Random(6)
    for round <- 0 until 60 do
      val vx = rnd.nextInt(16)
      val vy = rnd.nextInt(16)
      def signed(v: Int) = if v >= 8 then v - 16 else v
      val ops = List(
        ("bvslt", signed(vx) < signed(vy)),
        ("bvsle", signed(vx) <= signed(vy)),
        ("bvsgt", signed(vx) > signed(vy)),
        ("bvsge", signed(vx) >= signed(vy)),
        ("bvult", vx < vy),
        ("bvuge", vx >= vy)
      )
      val (op, expected) = ops(rnd.nextInt(ops.length))
      val s =
        f"(declare-const x (_ BitVec 4)) (declare-const y (_ BitVec 4)) (assert (= x #x$vx%x)) (assert (= y #x$vy%x)) (assert ($op x y)) (check-sat)"
      assertEquals(sat(s), expected, s"round $round: $op $vx $vy")
