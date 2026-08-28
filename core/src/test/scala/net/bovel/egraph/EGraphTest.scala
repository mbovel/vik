package net.bovel.egraph

import scala.collection.immutable.ArraySeq

class EGraphTest extends munit.FunSuite:

  class TestGraph:
    val eg = EGraph()
    private val syms = scala.collection.mutable.HashMap.empty[String, EClassId]

    def sym(name: String): EClassId =
      syms.getOrElseUpdate(name, eg.newClass())

    def node(funName: String, args: EClassId*): EClassId =
      eg.add(ENode(sym(funName), ArraySeq.from(args)))

    def leaf(name: String): EClassId = node(name)

    def find(id: EClassId): EClassId = eg.find(id)
    def merge(a: EClassId, b: EClassId): Unit = eg.merge(a, b)
    def rebuild(): Unit = eg.rebuild()

  test("add returns same id for identical nodes"):
    val t = TestGraph()
    val a = t.leaf("a")
    val a2 = t.leaf("a")
    assertEquals(a, a2)

  test("add returns different ids for different nodes"):
    val t = TestGraph()
    val a = t.leaf("a")
    val b = t.leaf("b")
    assertNotEquals(a, b)

  test("find returns self for fresh class"):
    val t = TestGraph()
    val a = t.leaf("a")
    assertEquals(t.find(a), a)

  test("merge makes find return same representative"):
    val t = TestGraph()
    val a = t.leaf("a")
    val b = t.leaf("b")
    t.merge(a, b)
    assertEquals(t.find(a), t.find(b))

  test("merge is idempotent"):
    val t = TestGraph()
    val a = t.leaf("a")
    val b = t.leaf("b")
    t.merge(a, b)
    t.merge(a, b)
    assertEquals(t.find(a), t.find(b))

  test("merge is transitive"):
    val t = TestGraph()
    val a = t.leaf("a")
    val b = t.leaf("b")
    val c = t.leaf("c")
    t.merge(a, b)
    t.merge(b, c)
    assertEquals(t.find(a), t.find(c))

  test("congruence: merge args triggers merge of parents"):
    val t = TestGraph()
    val a = t.leaf("a")
    val b = t.leaf("b")
    val fa = t.node("f", a)
    val fb = t.node("f", b)
    assertNotEquals(t.find(fa), t.find(fb))

    t.merge(a, b)
    t.rebuild()
    assertEquals(t.find(fa), t.find(fb))

  test("congruence propagates through multiple levels"):
    val t = TestGraph()
    val a = t.leaf("a")
    val b = t.leaf("b")
    val fa = t.node("f", a)
    val fb = t.node("f", b)
    val gfa = t.node("g", fa)
    val gfb = t.node("g", fb)

    t.merge(a, b)
    t.rebuild()
    assertEquals(t.find(fa), t.find(fb))
    assertEquals(t.find(gfa), t.find(gfb))

  test("congruence with fun merge: merging function symbols"):
    val t = TestGraph()
    val a = t.leaf("a")
    val f = t.sym("f")
    val g = t.sym("g")
    val fa = t.node("f", a)
    val ga = t.node("g", a)
    assertNotEquals(t.find(fa), t.find(ga))

    t.merge(f, g)
    t.rebuild()
    assertEquals(t.find(fa), t.find(ga))

  test("add after merge uses canonical form"):
    val t = TestGraph()
    val a = t.leaf("a")
    val b = t.leaf("b")
    val fa = t.node("f", a)
    t.merge(a, b)
    t.rebuild()
    val fb = t.node("f", b)
    assertEquals(t.find(fa), t.find(fb))

  test("binary node congruence"):
    val t = TestGraph()
    val a = t.leaf("a")
    val b = t.leaf("b")
    val c = t.leaf("c")
    val d = t.leaf("d")
    val ab = t.node("+", a, b)
    val cd = t.node("+", c, d)
    assertNotEquals(t.find(ab), t.find(cd))

    t.merge(a, c)
    t.merge(b, d)
    t.rebuild()
    assertEquals(t.find(ab), t.find(cd))

  // Cyclic tests inspired by Nelson & Oppen's congruence closure

  test("cycle: f(a) = a (fixed point)"):
    val t = TestGraph()
    val a = t.leaf("a")
    val fa = t.node("f", a)
    t.merge(a, fa)
    t.rebuild()
    assertEquals(t.find(a), t.find(fa))
    // f(f(a)) should also equal a by congruence
    val ffa = t.node("f", fa)
    assertEquals(t.find(a), t.find(ffa))

  test("cycle: f(f(a)) = a (2-cycle)"):
    val t = TestGraph()
    val a = t.leaf("a")
    val fa = t.node("f", a)
    val ffa = t.node("f", fa)
    t.merge(a, ffa)
    t.rebuild()
    assertEquals(t.find(a), t.find(ffa))
    // f(a) and f(f(f(a))) should also be equivalent
    val fffa = t.node("f", ffa)
    assertEquals(t.find(fa), t.find(fffa))

  test("cycle: f(a) = a implies f(b) = b when a = b"):
    val t = TestGraph()
    val a = t.leaf("a")
    val b = t.leaf("b")
    val fa = t.node("f", a)
    val fb = t.node("f", b)
    t.merge(a, fa) // f(a) = a
    t.merge(a, b) // a = b
    t.rebuild()
    assertEquals(t.find(b), t.find(fb))

  test("cycle: f(a) = a and g(a) = a implies f(g(a)) = a"):
    val t = TestGraph()
    val a = t.leaf("a")
    val fa = t.node("f", a)
    val ga = t.node("g", a)
    t.merge(a, fa) // f(a) = a
    t.merge(a, ga) // g(a) = a
    t.rebuild()
    // f(g(a)) = f(a) = a
    val fga = t.node("f", ga)
    assertEquals(t.find(a), t.find(fga))

  test("cycle: mutual fixed points f(a) = b, f(b) = a implies a = b"):
    val t = TestGraph()
    val a = t.leaf("a")
    val b = t.leaf("b")
    val fa = t.node("f", a)
    val fb = t.node("f", b)
    t.merge(fa, b) // f(a) = b
    t.merge(fb, a) // f(b) = a
    t.rebuild()
    assertEquals(t.find(fa), t.find(b))
    assertEquals(t.find(fb), t.find(a))
