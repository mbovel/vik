package net.bovel.egraph

import scala.collection.mutable.{ArrayBuffer, HashMap}

class EGraphTest extends munit.FunSuite:

  class TestGraph:
    val eg = EGraph()
    private val syms = scala.collection.mutable.HashMap.empty[String, EClassId]

    def sym(name: String): EClassId =
      syms.getOrElseUpdate(name, eg.newClass())

    def node(funName: String, args: EClassId*): EClassId =
      eg.add(sym(funName), args.toArray)

    def find(id: EClassId): EClassId = eg.find(id)
    def merge(a: EClassId, b: EClassId): Unit = eg.merge(a, b)
    def rebuild(): Unit = eg.rebuild()

  test("add returns same id for identical nodes"):
    val t = TestGraph()
    val a = t.sym("a")
    val fa = t.node("f", a)
    val fa2 = t.node("f", a)
    assertEquals(fa, fa2)

  test("add returns different ids for different nodes"):
    val t = TestGraph()
    val a = t.sym("a")
    val b = t.sym("b")
    val fa = t.node("f", a)
    val fb = t.node("f", b)
    assertNotEquals(fa, fb)

  test("add rejects e-nodes without arguments"):
    val t = TestGraph()
    intercept[IllegalArgumentException]:
      t.eg.add(t.sym("a"), Array.empty)

  test("find returns self for fresh class"):
    val t = TestGraph()
    val a = t.sym("a")
    assertEquals(t.find(a), a)

  test("merge makes find return same representative"):
    val t = TestGraph()
    val a = t.sym("a")
    val b = t.sym("b")
    t.merge(a, b)
    assertEquals(t.find(a), t.find(b))

  test("merge is idempotent"):
    val t = TestGraph()
    val a = t.sym("a")
    val b = t.sym("b")
    t.merge(a, b)
    t.merge(a, b)
    assertEquals(t.find(a), t.find(b))

  test("merge is transitive"):
    val t = TestGraph()
    val a = t.sym("a")
    val b = t.sym("b")
    val c = t.sym("c")
    t.merge(a, b)
    t.merge(b, c)
    assertEquals(t.find(a), t.find(c))

  test("congruence: merge args triggers merge of parents"):
    val t = TestGraph()
    val a = t.sym("a")
    val b = t.sym("b")
    val fa = t.node("f", a)
    val fb = t.node("f", b)
    assertNotEquals(t.find(fa), t.find(fb))

    t.merge(a, b)
    t.rebuild()
    assertEquals(t.find(fa), t.find(fb))

  test("congruence propagates through multiple levels"):
    val t = TestGraph()
    val a = t.sym("a")
    val b = t.sym("b")
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
    val a = t.sym("a")
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
    val a = t.sym("a")
    val b = t.sym("b")
    val fa = t.node("f", a)
    t.merge(a, b)
    t.rebuild()
    val fb = t.node("f", b)
    assertEquals(t.find(fa), t.find(fb))

  test("binary node congruence"):
    val t = TestGraph()
    val a = t.sym("a")
    val b = t.sym("b")
    val c = t.sym("c")
    val d = t.sym("d")
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
    val a = t.sym("a")
    val fa = t.node("f", a)
    t.merge(a, fa)
    t.rebuild()
    assertEquals(t.find(a), t.find(fa))
    // f(f(a)) should also equal a by congruence
    val ffa = t.node("f", fa)
    assertEquals(t.find(a), t.find(ffa))

  test("cycle: f(f(a)) = a (2-cycle)"):
    val t = TestGraph()
    val a = t.sym("a")
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
    val a = t.sym("a")
    val b = t.sym("b")
    val fa = t.node("f", a)
    val fb = t.node("f", b)
    t.merge(a, fa) // f(a) = a
    t.merge(a, b) // a = b
    t.rebuild()
    assertEquals(t.find(b), t.find(fb))

  test("cycle: f(a) = a and g(a) = a implies f(g(a)) = a"):
    val t = TestGraph()
    val a = t.sym("a")
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
    val a = t.sym("a")
    val b = t.sym("b")
    val fa = t.node("f", a)
    val fb = t.node("f", b)
    t.merge(fa, b) // f(a) = b
    t.merge(fb, a) // f(b) = a
    t.rebuild()
    assertEquals(t.find(fa), t.find(b))
    assertEquals(t.find(fb), t.find(a))

  test("random merges: every canonical form belongs to exactly one class"):
    val t = TestGraph()
    val rnd = scala.util.Random(7)
    val ids = ArrayBuffer.fill(50)(t.eg.newClass())
    for _ <- 0 until 200 do ids += t.node("h", ids(rnd.nextInt(ids.size)), ids(rnd.nextInt(ids.size)))
    for _ <- 0 until 40 do t.merge(ids(rnd.nextInt(ids.size)), ids(rnd.nextInt(ids.size)))
    t.rebuild()
    val classOfForm = HashMap.empty[(EClassId, Seq[EClassId]), EClassId]
    var nodes = 0
    for (cls, ns) <- t.eg.getClasses(); (fun, args) <- ns do
      nodes += 1
      val form = (t.find(fun), args.map(t.find))
      assertEquals(classOfForm.getOrElseUpdate(form, cls), cls) // congruent nodes share a class
      assertEquals(t.find(t.eg.add(fun, args.toArray)), cls) // and the hashcons knows the node
    assert(nodes > 100)
