package net.bovel.smt2

import net.bovel.smt.Formula.*
import net.bovel.smt.Term
import net.bovel.smt.Term.*

/** Lambdas: `(lambda ((x S) ...) body)`, parsed to de Bruijn indices (equal up
 *  to renaming), printed back; beta-reduction; bound variables never equated
 *  with anything (see [[Term.instantiate]]).
 */
class LambdaTest extends munit.FunSuite:
  def check(script: String): List[Boolean] = Player.play(Parser.parse(script))

  test("parsed to de Bruijn indices, printed back"):
    val script = Parser.parse("""
      (declare-fun f (Int Int) Int)
      (declare-const g (-> Int Int))
      (declare-const h (-> Int (-> Int Int)))
      (assert (= g (lambda ((x Int)) (f x 1))))
      (assert (= h (lambda ((y Int)) (lambda ((z Int)) (f z y)))))
      (assert (= (g 0) (h 1 2)))""")
    val f = Const("f")
    val asserts = script.commands.collect { case Command.Assert(a) => a }
    assertEquals(asserts(0), Eq(Const("g"), Lambda(List("Int"), App(f, List(Var(0), Num(1))))))
    assertEquals(asserts(1), Eq(Const("h"), Lambda(List("Int"), Lambda(List("Int"), App(f, List(Var(0), Var(1)))))))
    assertEquals(asserts(2), IntEq(App(Const("g"), List(Num(0))), App(App(Const("h"), List(Num(1))), List(Num(2)))))
    assertEquals(script.commands(1), Command.DeclareFun("g", List("Int"), "Int")) // a function symbol
    assertEquals(script.commands(2), Command.DeclareFun("h", List("Int"), "(-> Int Int)"))
    assert(Printer.print(script).contains("(lambda ((%0 Int)) (lambda ((%1 Int)) (f %1 %0)))"))
    assertEquals(Parser.parse(Printer.print(script)), script)

  test("alpha-equivalence by congruence"):
    val prefix = """
      (declare-fun f (Int Int) Int)
      (declare-const c Int)
      (assert (not (= (lambda ((x Int)) (f x c)) (lambda ((y Int)) (f y c)))))"""
    assertEquals(check(prefix + "(check-sat)"), List(false))
    assertEquals(check(prefix.replace("(f y c)", "(f c y)") + "(check-sat)"), List(true))

  test("beta-reduction"):
    assertEquals(
      check("""
        (declare-const g (-> Int Int))
        (assert (= g (lambda ((x Int)) (+ x 1))))
        (push) (assert (not (= (g 0) 1))) (check-sat) (pop)
        (push) (assert (= (g 0) 1)) (check-sat) (pop)
        (push) (assert (= (g 41) (g 40))) (check-sat) (pop)
        (push) (assert (not (= (g (g 40)) 42))) (check-sat) (pop)"""),
      List(false, true, false, false)
    )

  test("nested lambdas, curried applications, let"):
    assertEquals(
      check("""
        (declare-fun f (Int) Int)
        (declare-const k (-> Int (-> Int Int)))
        (assert (= k (lambda ((x Int)) (lambda ((y Int)) (- x y)))))
        (push) (assert (not (= ((k 5) 3) 2))) (check-sat) (pop)
        (push) (assert (not (= (k 5 3) 2))) (check-sat) (pop)
        (push) (assert (= (k 5 3) (k 4 3))) (check-sat) (pop)
        (declare-const g (-> Int Int))
        (assert (= g (lambda ((x Int)) (let ((y (f x))) (lambda ((z Int)) (- y z))))))
        (push) (assert (not (= (g 3 1) (- (f 3) 1)))) (check-sat) (pop)"""),
      List(false, false, false, false)
    )
    intercept[Smt2Error](
      Parser.parse("(declare-const g (-> Int Int)) (assert (= g (lambda ((x Int)) (ite (< x 0) 0 x))))")
    )

  test("lambdas as arguments"):
    assertEquals(
      check("""
        (declare-const twice (-> (-> Int Int) Int Int))
        (assert (= twice (lambda ((g (-> Int Int)) (x Int)) (g (g x)))))
        (push) (assert (not (= (twice (lambda ((y Int)) (+ y 1)) 0) 2))) (check-sat) (pop)
        (push) (assert (= (twice (lambda ((y Int)) (+ y 1)) 0) 2)) (check-sat) (pop)"""),
      List(false, true)
    )

  test("datatypes under lambdas"):
    assertEquals(
      check("""
        (declare-datatype List ((nil) (cons (head Int) (tail List))))
        (declare-const single (-> Int List))
        (assert (= single (lambda ((x Int)) (cons x nil))))
        (push) (assert (not (= (head (single 5)) 5))) (check-sat) (pop)
        (push) (assert ((_ is nil) (single 5))) (check-sat) (pop)"""),
      List(false, false)
    )

  test("bound variables are never equated: no unsound unsat"):
    assertEquals(
      check("""
        (declare-fun f (Int) Int)
        (declare-const h (-> Int Int))
        (declare-const k (-> Int Int))
        (assert (= h (lambda ((x Int)) (f x))))
        (assert (= k (lambda ((x Int)) (f 1))))
        (assert (= (h 1) (k 7)))
        (assert (not (= h k)))
        (check-sat)"""),
      List(true)
    )
    assertEquals(
      check("""
        (declare-datatype Color ((red) (green) (blue)))
        (declare-fun c (Int) Color)
        (declare-const h (-> Int Color))
        (assert (= h (lambda ((x Int)) (c x))))
        (assert (not (= h (lambda ((x Int)) red))))
        (assert (not (= h (lambda ((x Int)) green))))
        (assert (not (= h (lambda ((x Int)) blue))))
        (assert (= (h 1) red))
        (assert (= (h 2) green))
        (check-sat)"""),
      List(true)
    )
