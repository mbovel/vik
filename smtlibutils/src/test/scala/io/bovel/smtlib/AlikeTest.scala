package io.bovel.smtlib

import io.bovel.smtlib.Alike.Level
import io.bovel.smtlib.sexpr.SExprParser

/** The canonical forms of [[Alike]]: what each level joins, and what none may. */
class AlikeTest extends munit.FunSuite:
  private def form(script: String, level: Level) = Alike.canonical(SExprParser.parse(script), level)

  /** The first level at which the two scripts have one form, if any. */
  private def joined(a: String, b: String): Option[Level] = Level.values.find(l => form(a, l) == form(b, l))

  private val xy = "(declare-const x Int)(declare-const y Int)"

  test("a symbol twice is not two symbols, at any level"):
    assertEquals(joined(xy + "(assert (= x x))", xy + "(assert (= x y))"), None)

  test("comments, layout and set-info are text"):
    assertEquals(
      joined(
        "; one\n(set-info :source |a|)\n(declare-const x Int)\n(assert (> x 0))",
        "(set-info :source |b|) (declare-const x Int) ; two\n(assert   (> x 0))"
      ),
      Some(Level.Text)
    )

  test("a renamed copy is alike by names"):
    val a = xy + "(assert (> (+ x y) x))"
    val b = "(declare-const p Int)(declare-const q Int)(assert (> (+ p q) p))"
    assertEquals(joined(a, b), Some(Level.Names))
    assertEquals(form(a, Level.Names), "(declare-const v0 Int)\n(declare-const v1 Int)\n(assert (> (+ v0 v1) v0))")

  test("bound variables are named by their binder, not by the symbols outside"):
    val global = "(declare-const y Int)"
    val a = global + "(assert (exists ((x Int)) (> x y)))"
    assertEquals(joined(a, global + "(assert (exists ((z Int)) (> z y)))"), Some(Level.Names))
    assertEquals(joined(a, global + "(assert (exists ((y Int)) (> y y)))"), None) // the bound y hides the global one

  test("a reordered copy is alike by order"):
    val a = xy + "(assert (> x 0))(assert (< (+ x y) 5))"
    val b = "(declare-const y Int)(declare-const x Int)(assert (< (+ y x) 5))(assert (> x 0))"
    assertEquals(joined(a, b), Some(Level.Order))
    assertEquals(joined(a, xy + "(assert (and (< (+ y x) 5) (> x 0)))"), Some(Level.Order)) // a conjunction split
    assertEquals(joined(a, xy + "(assert (> y 0))(assert (< (+ x y) 5))"), Some(Level.Order)) // x and y swapped
    val minus = xy + "(assert (> x 0))(assert (< (- x y) 5))"
    assertEquals(joined(minus, xy + "(assert (> y 0))(assert (< (- x y) 5))"), None) // a minuend, then a subtrahend

  test("associative operations flatten, and only those"):
    val abc = "(declare-const a Int)(declare-const b Int)(declare-const c Int)"
    assertEquals(joined(abc + "(assert (= 0 (+ a (+ b c))))", abc + "(assert (= (+ (+ c b) a) 0))"), Some(Level.Order))
    assertEquals(joined(abc + "(assert (= 0 (- a (- b c))))", abc + "(assert (= 0 (- (- a b) c)))"), None)

  test("a let's bindings are simultaneous, so they sort"):
    val pq = "(declare-const p Bool)(declare-const q Bool)"
    val a = pq + "(assert (let ((a p) (b (not q))) (and a b)))"
    assertEquals(joined(a, pq + "(assert (let ((d (not q)) (c p)) (and c d)))"), Some(Level.Order))
    // Read sequentially, the second binding would be p: simultaneously, it is the global x.
    val pqx = pq + "(declare-const x Bool)"
    assertEquals(joined(pqx + "(assert (let ((x p) (y x)) y))", pqx + "(assert (let ((x p) (y p)) y))"), None)

  test("a six-cycle of distincts and two triangles are not alike, though no symbol tells them apart"):
    val vars = "abcdef".map(v => s"(declare-const $v Bool)").mkString
    def distinct(pairs: String*) = pairs.map(p => s"(assert (distinct ${p(0)} ${p(1)}))").mkString
    val cycle = vars + distinct("ab", "bc", "cd", "de", "ef", "fa") // sat: alternate
    val triangles = vars + distinct("ab", "bc", "ca", "de", "ef", "fd") // unsat: three Booleans pairwise distinct
    assertEquals(joined(cycle, triangles), None)
    assertEquals(joined(cycle, vars + distinct("fa", "cd", "ab", "ef", "bc", "de")), Some(Level.Order))

  test("scopes and named terms keep their order"):
    val pq = "(declare-const p Bool)(declare-const q Bool)"
    // unsat, then sat: an assertion moved across the push would make both unsat.
    val scoped = pq + "(assert p)(push 1)(assert (not p))(check-sat)(pop 1)"
    assertEquals(joined(scoped, pq + "(push 1)(assert (not p))(check-sat)(pop 1)(assert p)"), None)
    val named = pq + "(assert (! p :named a))(assert (=> a q))"
    assertEquals(form(named, Level.Order).linesIterator.toList.takeRight(2).map(_.take(10)), List("(assert (!", "(assert (="))

  test("the statuses are read per check-sat"):
    val script = SExprParser.parse(
      "(set-info :status sat)(check-sat)(check-sat)(set-info :status unknown)(set-info :status unsat)(check-sat)"
    )
    assertEquals(Alike.statuses(script), List("sat", "none", "unsat"))

  test("the shapes leave names out, and a changed constant shows"):
    def shape(s: String) = Alike.shape(SExprParser.parse(s))
    val a = shape(xy + "(assert (> (+ x y) 1))(assert (< x 7))")
    assertEquals(Alike.similarity(a, shape("(declare-const p Int)(declare-const q Int)(assert (< q 7))(assert (> (+ q p) 1))")), 1.0)
    val near = Alike.similarity(a, shape(xy + "(assert (> (+ x y) 2))(assert (< x 7))"))
    assert(near > 0.5 && near < 1, near)

  test("the operations of a script are its theory symbols applied, not its literals or its own symbols"):
    def operations(s: String) = Alike.operations(SExprParser.parse(s))
    val bv = "(declare-fun x () (_ BitVec 32))(declare-fun bvfoo ((_ BitVec 32)) (_ BitVec 32))"
    assertEquals(operations(bv + "(assert (= (bvfoo x) (_ bv5 32)))").filter(_.startsWith("bv")), Set.empty[String])
    assert(operations(bv + "(assert (bvsle x (_ bv5 32)))").contains("bvsle"))
    assert(operations(bv + "(assert (= ((_ extract 7 0) x) #x00))").contains("extract"))

