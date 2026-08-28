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
    def push(): Unit = eg.push()
    def pop(): Unit = eg.pop()

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

  test("pop undoes merges and their congruences"):
    val t = TestGraph()
    val a = t.sym("a")
    val b = t.sym("b")
    val fa = t.node("f", a)
    val fb = t.node("f", b)
    t.push()
    t.merge(a, b)
    t.rebuild()
    assertEquals(t.find(fa), t.find(fb))
    t.pop()
    assertNotEquals(t.find(a), t.find(b))
    assertNotEquals(t.find(fa), t.find(fb))
    assertEquals(t.node("f", b), fb) // the hashcons is restored too
    assertEquals(t.eg.level, 0)

  test("pop undoes nodes and classes added in the level"):
    val t = TestGraph()
    val a = t.sym("a")
    val fa = t.node("f", a)
    t.sym("g") // symbols created inside the level would be popped with it
    val n = t.eg.classCount
    t.push()
    val ga = t.node("g", a)
    t.merge(ga, fa)
    t.rebuild()
    t.pop()
    assertEquals(t.eg.classCount, n)
    assertEquals(t.node("f", a), fa) // still there
    assertEquals(t.node("g", a), ga) // fresh again, and gets the same id back
    assertNotEquals(t.find(ga), t.find(fa))

  test("levels nest"):
    val t = TestGraph()
    val a = t.sym("a")
    val b = t.sym("b")
    val c = t.sym("c")
    t.push()
    t.merge(a, b)
    t.push()
    t.merge(b, c)
    t.rebuild()
    assertEquals(t.find(a), t.find(c))
    t.pop()
    assertEquals(t.find(a), t.find(b))
    assertNotEquals(t.find(a), t.find(c))
    t.pop()
    assertNotEquals(t.find(a), t.find(b))

  test("pop without push fails"):
    val t = TestGraph()
    intercept[IllegalArgumentException]:
      t.eg.pop()

  test("random operations with push/pop match a replay of the kept operations"):
    enum Op:
      case Add(f: EClassId, x: EClassId, y: EClassId)
      case Merge(x: EClassId, y: EClassId)
      case Rebuild
    // The kept operations: one list per open level, the last one being the current level.
    val kept = ArrayBuffer(ArrayBuffer.empty[Op])
    def replay(): EGraph =
      val eg = EGraph()
      for _ <- 0 to 20 do eg.newClass() // h and the leaves
      for level <- kept; op <- level do
        op match
          case Op.Add(f, x, y) => eg.add(f, Array(x, y))
          case Op.Merge(x, y) => eg.merge(x, y)
          case Op.Rebuild => eg.rebuild()
      eg
    // Representation-independent view: classes as sets of ids, nodes keyed by the smallest id of their class.
    def state(eg: EGraph) =
      val classes = (0 until eg.classCount).groupBy(eg.find).values.map(_.toSet).toSet
      val canon = (0 until eg.classCount).map(id =>
        id -> (0 until eg.classCount).filter(eg.find(_) == eg.find(id)).min
      ).toMap
      val nodes = eg.getClasses().map((cls, ns) => canon(cls) -> ns.map((f, args) => (canon(f), args.map(canon))).toSet)
      (eg.classCount, classes, nodes)

    val rnd = scala.util.Random(11)
    val eg = EGraph()
    val h = eg.newClass()
    val ids = ArrayBuffer.fill(20)(eg.newClass())
    val snapshots = ArrayBuffer.empty[Any] // state at each push
    var pops = 0
    for step <- 0 until 600 do
      rnd.nextInt(10) match
        case 0 | 1 | 2 | 3 =>
          val x = ids(rnd.nextInt(ids.size))
          val y = ids(rnd.nextInt(ids.size))
          ids += eg.add(h, Array(x, y))
          kept.last += Op.Add(h, x, y)
        case 4 | 5 | 6 =>
          val x = ids(rnd.nextInt(ids.size))
          val y = ids(rnd.nextInt(ids.size))
          eg.merge(x, y)
          kept.last += Op.Merge(x, y)
          if rnd.nextBoolean() then
            eg.rebuild()
            kept.last += Op.Rebuild
        case 7 | 8 =>
          eg.push() // rebuilds first
          kept.last += Op.Rebuild
          snapshots += state(eg)
          kept += ArrayBuffer.empty
        case _ if eg.level > 0 =>
          eg.pop()
          kept.remove(kept.length - 1)
          pops += 1
          ids.filterInPlace(_ < eg.classCount) // drop ids of popped classes
          assertEquals(state(eg), snapshots.remove(snapshots.length - 1), s"pop at step $step vs. push snapshot")
          assertEquals(state(eg), state(replay()), s"pop at step $step vs. replay")
        case _ => ()
    assert(pops > 20)
