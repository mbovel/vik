package net.bovel.egraph

import java.util.Arrays

import scala.collection.immutable.ArraySeq
import scala.collection.mutable.{ArrayBuffer, HashMap}

type EClassId = Int

/** A function application `fun(args)`, the input of [[EGraph.add]]: `fun` and
 *  every argument are e-classes (function symbols are e-classes too, so `f = g`
 *  makes `f(a)` and `g(a)` congruent). `args` is non-empty: a constant is its
 *  own e-class, not an application.
 */
final case class App(fun: EClassId, args: ArraySeq[EClassId]):
  require(args.nonEmpty, "an application needs at least one argument; a constant is its own e-class")

/** Theory rules run by an [[EGraph]]: callbacks on the nodes added and the
 *  classes merged, a global check, and levels. They may merge classes with
 *  [[EGraph.derive]] (an equality justified by rule premises), add nodes and
 *  raise a [[EGraph.contradiction]]. Merges are reported by [[EGraph.rebuild]],
 *  after the union, so a callback sees a consistent graph and may cause further
 *  merges, reported in turn. Datatypes are decided this way (`smt.AdtRules`);
 *  see `docs/adt-plan.md`.
 */
trait Rules:
  /** A node was added for `app`, in the fresh class `cls` (already merged with
   *  the class of a congruent node, if any).
   */
  def added(eg: EGraph, app: App, cls: EClassId): Unit = ()

  /** The class of `from` was merged into the class of `into` (their
   *  representatives at the time); `parents` are the nodes that mentioned the
   *  absorbed class.
   */
  def merged(eg: EGraph, from: EClassId, into: EClassId, parents: Iterable[App]): Unit = ()

  /** A global condition on the closure, checked at the theory's final check:
   *  the reasons of a conflict, if any.
   */
  def finalCheck(eg: EGraph): Option[ArrayBuffer[Int]] = None

  def push(): Unit = ()
  def pop(): Unit = ()

object NoRules extends Rules

/** The internal types of [[EGraph]]. */
object EGraph:
  /** An e-node: an [[App]] together with its canonical form `fun(args)`, the
   *  representatives of the application's function and arguments as of the last
   *  [[EGraph.rebuild]], and `cls`, a member of the e-class it was added to
   *  (use [[EGraph.find]] for its representative). The canonical form is
   *  updated in place, so a node must be removed from the congruence table
   *  before and re-inserted after. Equality and hashing are on the canonical
   *  form (arrays compare by reference, hence the overrides).
   */
  private final case class ENode(app: App, var fun: EClassId, args: Array[EClassId], cls: EClassId):
    /** Set once another live node has the same canonical form; the node is then
     *  dropped from the congruence table.
     */
    var dead: Boolean = false

    override def hashCode: Int = 31 * Arrays.hashCode(args) + fun
    override def equals(that: Any): Boolean = that match
      case that: ENode => fun == that.fun && Arrays.equals(args, that.args)
      case _ => false
    override def toString: String = s"$fun(${args.mkString(", ")})"

  /** Why two terms were equated: an equality asserted for `reason` (a literal,
   *  in CDCL(T)), or the congruence of two nodes.
   */
  private enum Justification:
    case Asserted(reason: Int)
    case Congruence(n1: ENode, n2: ENode)

    /** A theory rule whose premises are the given equalities (see [[Rules]]).
     */
    case Rule(premises: ArraySeq[(EClassId, EClassId)])

  /** A merge to report to the rules: the two representatives and the parents of
   *  the absorbed class.
   */
  private final case class Merge(from: EClassId, into: EClassId, parents: ArrayBuffer[ENode] | Null)

  /** An edge of the union tree: the class of `a` was merged into the class of
   *  `into` because the terms `a` (then in the absorbed class) and `b` (then in
   *  the surviving one) were equated for `why`. `time` orders the merges.
   */
  private final case class Edge(into: EClassId, a: EClassId, b: EClassId, why: Justification, time: Int)

  /** A disequality `a != b` asserted for `reason`. */
  private final case class Diseq(a: EClassId, b: EClassId, reason: Int)

  /** What [[EGraph.pop]] must undo, one record per mutation done inside a
   *  level. Records are undone in reverse order, so each one sees the exact
   *  state its mutation left behind (`Repaired` and the `Merged` of the
   *  congruence it may trigger touch disjoint state, so their order is free).
   *  `Merged` keeps the lengths of the surviving class's lists and the lists of
   *  the absorbed class, which `merge` appended to the former.
   */
  private enum Undo:
    case NewClass
    case Added(node: ENode)
    case Merged(
        from: EClassId,
        into: EClassId,
        intoUsesLen: Int,
        fromUses: ArrayBuffer[ENode] | Null,
        intoForbidLen: Int,
        fromForbid: ArrayBuffer[Diseq] | Null
    )
    case Repaired(node: ENode, fun: EClassId, args: Array[EClassId], died: Boolean)
    case Disequated(ra: EClassId, rb: EClassId)
    case Violated

/** A congruence closure over e-classes identified by dense `Int` ids, with
 *  disequalities, backtracking and explanations.
 *
 *  Union-find: `repr(id)` is the representative of `id`, so `find` is one array
 *  read. `merge` repoints every member of the smaller class (they are kept in a
 *  circular list `next`), as Z3 and cvc5 do. Congruence closure is deferred,
 *  egg-style: `merge` records the parents of the absorbed class as `pending`,
 *  and `rebuild` re-canonicalizes them and merges the classes of nodes that
 *  became identical, until no node is pending.
 *
 *  Every syntactically distinct term gets its own node and class; congruent
 *  terms are then merged with a justification, so `explain` can account for
 *  every equality (as in Z3, LISA and egg; a hashcons on canonical forms would
 *  identify `f(a)` with `f(c)` after `a = c` without a record of it).
 *
 *  Backtracking: `push` opens a level and `pop` undoes every `add`, `merge`,
 *  `disequate` and `rebuild` done since, through a trail of [[Undo]] records.
 *  Nothing is recorded outside a level, so non-backtracking use costs nothing
 *  extra.
 *
 *  Invariants, once `pending` is empty:
 *    - `cg` holds exactly the live nodes, keyed by their canonical form, at
 *      most one per form; dead nodes stay in `nodes` and in use lists (skipped
 *      by `rebuild`, reclaimed only by `pop`);
 *    - `uses(r)` of a representative `r` contains every node whose canonical
 *      form mentions `r`, plus stale entries;
 *    - `forbid(r)` contains every disequality with a side in `r`'s class;
 *    - `edges(x)` is `null` iff `x` is a representative, and `size` is only
 *      meaningful for representatives;
 *    - `push` completes the rebuild, so at `pop` time `pending` only holds
 *      nodes of merges of the popped level.
 */
final class EGraph(rules: Rules = NoRules):
  import EGraph.*

  /** Number of e-classes created so far; ids are `0 until count` and index the
   *  arrays below.
   */
  private var count = 0

  /** Representative of each e-class id. */
  private var repr = new Array[EClassId](16)

  /** Next member in the circular list of the members of a class. The list is
   *  circular rather than nil-terminated so that two classes are joined by
   *  swapping the `next` of their representatives, which is O(1), needs no tail
   *  pointer or terminator, and is its own inverse (used by `pop`).
   */
  private var next = new Array[EClassId](16)

  /** Number of members of a class; only meaningful for representatives. */
  private var size = new Array[Int](16)

  /** Parent nodes of a class (nodes having it as canonical `fun` or argument);
   *  only meaningful for representatives, `null` when empty.
   */
  private var uses = new Array[ArrayBuffer[ENode] | Null](16)

  /** Disequalities involving a class: "forbid lists" (Nelson 1980), as in the
   *  die-graphs of Zakhour et al., POPL 2025. A `Diseq` sits in the lists of
   *  both its sides, and `merge` moves the list of the absorbed class to the
   *  surviving one, so the list of a representative holds every disequality of
   *  its class. A class forbidding itself is a conflict; `merge` detects it by
   *  scanning the list of the absorbed class. Only meaningful for
   *  representatives, `null` when empty.
   */
  private var forbid = new Array[ArrayBuffer[Diseq] | Null](16)

  /** The node added in each class, `null` for a class of [[newClass]]. */
  private var nodeOf = new Array[ENode | Null](16)

  /** The first disequality found violated, or the reasons of the first
   *  contradiction raised by a rule; the graph is inconsistent from then on.
   */
  private var violated: Diseq | Null = null
  private var contradicted: ArrayBuffer[Int] | Null = null

  /** Merges done since the last [[rebuild]], to report to the rules. */
  private val merges = ArrayBuffer.empty[Merge]

  /** The union tree, for explanations: `edges(from)` is the [[Edge]] by which
   *  the class of `from` was absorbed, `null` while `from` is a representative.
   *  Ids point to the root that absorbed them when they were roots, so
   *  following `edges` from any id leads to its representative. Unlike `repr`,
   *  the tree is never compressed, and union by size keeps it shallow.
   */
  private var edges = new Array[Edge | Null](16)

  /** Timestamp of the next merge. */
  private var time = 0

  /** Every node, in creation order. */
  private val nodes = ArrayBuffer.empty[ENode]

  /** Congruence table: maps a canonical form to the live node with that form.
   *
   *  `java.util.HashMap` rather than Scala's `mutable.HashMap`: with the same
   *  code otherwise, the Scala map measured about 5% slower on `randomBatch`
   *  (disjoint 95% confidence intervals) and equal within noise elsewhere; see
   *  `egraph-bench/results/2026-08-28-laraserver4-scala-hashmap.md`.
   */
  private val cg = new java.util.HashMap[ENode, ENode]

  /** Nodes whose canonical `fun` or arguments lost their representative since
   *  the last [[rebuild]].
   */
  private val pending = ArrayBuffer.empty[ENode]

  /** Undo records of the mutations done inside the current levels. */
  private val trail = ArrayBuffer.empty[Undo]

  /** Length of `trail` when each open level was pushed. */
  private val levels = ArrayBuffer.empty[Int]

  /** Marks of the ids visited by the current `explainInto` call (see there). */
  private var visited = new Array[Int](0)
  private var stamp = 0

  def find(id: EClassId): EClassId = repr(id)

  /** The number of e-classes ever created (ids range over
   *  `0 until classCount`).
   */
  def classCount: Int = count

  /** The number of open levels. */
  def level: Int = levels.length

  private def recording: Boolean = levels.nonEmpty

  def newClass(): EClassId =
    if count == repr.length then grow()
    val id = count
    count += 1
    repr(id) = id
    next(id) = id
    size(id) = 1
    if recording then trail += Undo.NewClass
    id

  private def grow(): Unit =
    val n = repr.length * 2
    repr = Arrays.copyOf(repr, n)
    next = Arrays.copyOf(next, n)
    size = Arrays.copyOf(size, n)
    uses = Arrays.copyOf(uses, n)
    forbid = Arrays.copyOf(forbid, n)
    edges = Arrays.copyOf(edges, n)
    nodeOf = Arrays.copyOf(nodeOf, n)

  /** Adds a node for `app` and returns its e-class, a fresh one, merged at once
   *  with the class of any congruent node. Adding the same application twice
   *  gives two nodes in one class; callers that may add a term several times
   *  should memoize, as [[EGraphBuilder]] does and as the hash-consing of terms
   *  in an SMT solver does.
   */
  def add(app: App): EClassId =
    val cls = newClass()
    val node = ENode(app, repr(app.fun), Array.tabulate(app.args.length)(i => repr(app.args(i))), cls)
    nodes += node
    addUse(node.fun, node)
    var i = 0 // a while loop: `for` over an Array[Int] boxes every element
    while i < node.args.length do
      addUse(node.args(i), node)
      i += 1
    nodeOf(cls) = node
    if recording then trail += Undo.Added(node)
    insertOrMerge(node)
    rules.added(this, app, cls)
    cls

  /** Inserts `node`, whose canonical form is up to date, into the congruence
   *  table, or, if a live node already has that form, marks it dead and merges
   *  the two classes by congruence. Returns whether it died.
   */
  private def insertOrMerge(node: ENode): Boolean =
    val other = cg.putIfAbsent(node, node)
    if other == null then false
    else
      node.dead = true
      union(node.cls, other.cls, Justification.Congruence(node, other))
      true

  /** Replaces every element of `args` by its representative. */
  private def canonicalize(args: Array[EClassId]): Unit =
    var i = 0
    while i < args.length do
      args(i) = repr(args(i))
      i += 1

  private def addUse(root: EClassId, node: ENode): Unit =
    val list = uses(root)
    if list == null then uses(root) = ArrayBuffer(node)
    else list += node

  private def removeLastUse(root: EClassId): Unit =
    val list = uses(root).nn
    list.remove(list.length - 1)

  /** Asserts `a != b` for `reason`; a conflict if they are already equal. */
  def disequate(a: EClassId, b: EClassId, reason: Int): Unit =
    val ra = repr(a)
    val rb = repr(b)
    val d = Diseq(a, b, reason)
    if ra == rb then violate(d)
    else
      addForbid(ra, d)
      addForbid(rb, d)
      if recording then trail += Undo.Disequated(ra, rb)

  private def addForbid(root: EClassId, d: Diseq): Unit =
    val list = forbid(root)
    if list == null then forbid(root) = ArrayBuffer(d)
    else list += d

  private def violate(d: Diseq): Unit =
    if violated == null then
      violated = d
      if recording then trail += Undo.Violated

  /** A disequality of `list`, the forbid list of a representative, whose other
   *  side is in the class of `root`.
   */
  private def forbidding(list: ArrayBuffer[Diseq], root: EClassId): Option[Diseq] =
    list.find(d => repr(d.a) == root || repr(d.b) == root)

  /** A disequality between the classes of `a` and `b`, if any: its side in
   *  `a`'s class, its side in `b`'s class, and its reason. With `explain` of
   *  `a` and `b` with those sides, this explains why `a = b` is false, e.g. to
   *  propagate a negated equality atom in CDCL(T). `None` when `a` and `b` are
   *  equal, even if the graph is inconsistent (see [[conflict]]).
   */
  def disequality(a: EClassId, b: EClassId): Option[(EClassId, EClassId, Int)] =
    val ra = repr(a)
    val rb = repr(b)
    val fa = forbid(ra)
    val fb = forbid(rb)
    if ra == rb || fa == null || fb == null then None
    else
      val found = if fa.length <= fb.length then forbidding(fa, rb) else forbidding(fb, ra)
      found.map(d => if repr(d.a) == ra then (d.a, d.b, d.reason) else (d.b, d.a, d.reason))

  /** Whether `a != b` follows from the asserted disequalities and equalities.
   */
  def areDisequal(a: EClassId, b: EClassId): Boolean = disequality(a, b).isDefined

  /** The reasons of the inconsistency, if the asserted equalities and
   *  disequalities (and the rules) are inconsistent: for a violated
   *  disequality, its reason and the explanation of the equality of its sides.
   */
  def conflict: Option[ArrayBuffer[Int]] =
    val v = violated
    val c = contradicted
    if v != null then Some(explain(v.a, v.b) += v.reason) else if c != null then Some(c) else None

  /** Raised by a rule: the graph is inconsistent for these reasons. */
  def contradiction(reasons: ArrayBuffer[Int]): Unit =
    if violated == null && contradicted == null then
      contradicted = reasons
      if recording then trail += Undo.Violated

  /** Merges `a` and `b` for a rule whose premises are the given equalities,
   *  which must hold before the call: their paths in the union tree then
   *  predate the new edge, so explaining them never runs into it. `explain`
   *  accounts for the merge by the premises.
   */
  def derive(a: EClassId, b: EClassId, premises: Seq[(EClassId, EClassId)]): Unit =
    union(a, b, Justification.Rule(ArraySeq.from(premises)))

  /** The applications whose node mentions the class of `cls` (as function or
   *  argument), possibly with entries that stopped mentioning it after merges.
   */
  def parents(cls: EClassId): Iterator[App] =
    val list = uses(repr(cls))
    if list == null then Iterator.empty else list.iterator.map(_.app)

  /** The applications added in the class of `cls`, each with its own class id
   *  (a member of the class: the id to use in premises about the node).
   */
  def nodes(cls: EClassId): Iterator[(App, EClassId)] =
    val root = repr(cls)
    Iterator.iterate(root)(next).drop(1).takeWhile(_ != root).++(Iterator(root)).flatMap { m =>
      val node = nodeOf(m)
      if node == null then Iterator.empty else Iterator((node.app, m))
    }

  /** The rules' global check (see [[Rules.finalCheck]]). */
  def finalCheck(): Option[ArrayBuffer[Int]] = rules.finalCheck(this)

  /** All e-classes (one per equivalence class) with the distinct applications
   *  added to them.
   */
  def classes: HashMap[EClassId, ArrayBuffer[App]] =
    val res = HashMap.empty[EClassId, ArrayBuffer[App]]
    for id <- 0 until count if repr(id) == id do res(id) = ArrayBuffer.empty
    for node <- nodes do res(repr(node.cls)) += node.app
    res.mapValuesInPlace((_, apps) => apps.distinct)
    res

  /** Asserts `a = b` for `reason` (a literal, in CDCL(T)). */
  def merge(a: EClassId, b: EClassId, reason: Int): Unit = union(a, b, Justification.Asserted(reason))

  /** Merges the classes of `a` and `b` for `why`. */
  private def union(a: EClassId, b: EClassId, why: Justification): Unit =
    val ra = repr(a)
    val rb = repr(b)
    if ra == rb then return
    var from = ra
    var into = rb
    if size(ra) > size(rb) then
      from = rb
      into = ra

    // Record the edge of the union tree, with `a` on the absorbed side.
    edges(from) = if repr(a) == from then Edge(into, a, b, why, time) else Edge(into, b, a, why, time)
    time += 1

    // A disequality between the two classes is violated by this merge (checked before repointing).
    val fromForbid = forbid(from)
    val intoForbid = forbid(into)
    if fromForbid != null && violated == null then forbidding(fromForbid, into).foreach(violate)

    // Repoint every member of the smaller class, then join the two circular member lists (see `next`).
    repr(from) = into
    var m = next(from)
    while m != from do
      repr(m) = into
      m = next(m)
    val t = next(from)
    next(from) = next(into)
    next(into) = t
    size(into) += size(from)

    // Only the parents of the absorbed class need re-canonicalization.
    val fromUses = uses(from)
    val intoUses = uses(into)
    if recording then
      trail += Undo.Merged(
        from,
        into,
        if intoUses == null then 0 else intoUses.length,
        fromUses,
        if intoForbid == null then 0 else intoForbid.length,
        fromForbid
      )
    if fromUses != null then
      pending ++= fromUses
      if intoUses == null then uses(into) = ArrayBuffer.from(fromUses) else intoUses ++= fromUses
      uses(from) = null
    if fromForbid != null then
      if intoForbid == null then forbid(into) = ArrayBuffer.from(fromForbid) else intoForbid ++= fromForbid
      forbid(from) = null
    if rules ne NoRules then merges += Merge(from, into, fromUses)

  /** Restores the congruence closure after merges, and reports the merges to
   *  the rules, whose consequences are processed in turn, until no node is
   *  pending and no merge is unreported.
   */
  def rebuild(): Unit =
    while pending.nonEmpty || merges.nonEmpty do
      while pending.nonEmpty do
        val node = pending.remove(pending.length - 1)
        if !node.dead then repair(node)
      var i = 0
      while i < merges.length do // callbacks may append merges
        val m = merges(i)
        i += 1
        val parents = if m.parents == null then Nil else m.parents.view.map(_.app)
        rules.merged(this, m.from, m.into, parents)
      merges.clear()

  private def repair(node: ENode): Unit =
    cg.remove(node) // stored under its previous form
    val oldFun = node.fun
    val oldArgs = if recording then node.args.clone() else null
    node.fun = repr(node.fun)
    canonicalize(node.args)
    val died = insertOrMerge(node)
    if oldArgs != null then trail += Undo.Repaired(node, oldFun, oldArgs, died)

  /** The reasons that make `x` and `y` equal: a set of reasons of `merge`s from
   *  which `x = y` follows by congruence closure. Requires
   *  `find(x) == find(y)`.
   *
   *  The explanation is read off the union tree the way LISA's
   *  `Congruence.scala` does (EPFL LARA): the most recent edge on the paths
   *  from `x` and `y` to their lowest common ancestor is the merge that
   *  connected them, so it is explained by its justification together with,
   *  recursively, the equalities of `x` and `y` with the two terms it equated,
   *  which hold by older edges. A congruence is explained by the equalities of
   *  the two terms' functions and arguments. This avoids the re-rooting of
   *  Nieuwenhuis and Oliveras' proof forest at the price of a weaker bound on
   *  the work per explanation.
   */
  def explain(x: EClassId, y: EClassId): ArrayBuffer[Int] =
    require(repr(x) == repr(y), "explain: the two terms are not equal")
    val reasons = ArrayBuffer.empty[Int]
    explainInto(x, y, reasons)
    reasons.distinct

  private def explainInto(x: EClassId, y: EClassId, out: ArrayBuffer[Int]): Unit =
    if x == y then return
    // The lowest common ancestor of `x` and `y` in the union tree: mark the ancestors of `x`, climb from `y`.
    stamp += 1
    if visited.length < count then visited = new Array[Int](repr.length)
    var u = x
    while u != -1 do
      visited(u) = stamp
      val e = edges(u)
      u = if e == null then -1 else e.into
    var lca = y
    while visited(lca) != stamp do lca = edges(lca).nn.into
    // The most recent merge below the lowest common ancestor is the one that connected the two sides.
    var edge = edges(if x != lca then x else y).nn
    var onPathX = x != lca
    u = x
    while u != lca do
      val e = edges(u).nn
      if e.time > edge.time then
        edge = e
        onPathX = true
      u = e.into
    u = y
    while u != lca do
      val e = edges(u).nn
      if e.time > edge.time then
        edge = e
        onPathX = false
      u = e.into
    // It equated `edge.a`, then in the absorbed class (the side of `x` iff the edge is on its path), with `edge.b`.
    val (l, r) = if onPathX then (edge.a, edge.b) else (edge.b, edge.a)
    explainInto(x, l, out)
    justify(edge, out)
    explainInto(r, y, out)

  /** Adds to `out` the reasons behind an edge: its own reason, or, for a
   *  congruence, the reasons making the two applications' functions and
   *  arguments equal.
   */
  private def justify(edge: Edge, out: ArrayBuffer[Int]): Unit = edge.why match
    case Justification.Asserted(reason) => out += reason
    case Justification.Congruence(n1, n2) =>
      explainInto(n1.app.fun, n2.app.fun, out)
      for i <- n1.app.args.indices do explainInto(n1.app.args(i), n2.app.args(i), out)
    case Justification.Rule(premises) => for (x, y) <- premises do explainInto(x, y, out)

  /** Opens a level: [[pop]] will undo everything done after this call.
   *  Completes the pending [[rebuild]] first, so that a level starts from a
   *  closed congruence closure.
   */
  def push(): Unit =
    rebuild()
    levels += trail.length
    rules.push()

  /** Undoes every `add`, `merge`, `disequate` and `rebuild` done since the last
   *  [[push]].
   */
  def pop(): Unit =
    require(levels.nonEmpty, "pop without a matching push")
    pending.clear() // everything pending was merged in this level
    merges.clear()
    val mark = levels.remove(levels.length - 1)
    while trail.length > mark do undo(trail.remove(trail.length - 1))
    rules.pop()

  private def undo(record: Undo): Unit = record match
    case Undo.NewClass =>
      count -= 1
      uses(count) = null
      forbid(count) = null
      nodeOf(count) = null
    case Undo.Added(node) =>
      nodes.remove(nodes.length - 1)
      if !node.dead then cg.remove(node) // a node congruent to an existing one at creation was never inserted
      removeLastUse(node.fun)
      var i = 0
      while i < node.args.length do
        removeLastUse(node.args(i))
        i += 1
    case Undo.Merged(from, into, intoUsesLen, fromUses, intoForbidLen, fromForbid) =>
      edges(from) = null
      size(into) -= size(from)
      val t = next(from) // the join of the member lists is its own inverse
      next(from) = next(into)
      next(into) = t
      repr(from) = from
      var m = next(from)
      while m != from do
        repr(m) = from
        m = next(m)
      if fromUses != null then
        uses(into).nn.takeInPlace(intoUsesLen)
        uses(from) = fromUses
      if fromForbid != null then
        forbid(into).nn.takeInPlace(intoForbidLen)
        forbid(from) = fromForbid
    case Undo.Repaired(node, fun, args, died) =>
      if died then node.dead = false else cg.remove(node)
      node.fun = fun
      System.arraycopy(args, 0, node.args, 0, args.length)
      cg.put(node, node)
    case Undo.Disequated(ra, rb) =>
      val fa = forbid(ra).nn
      val fb = forbid(rb).nn
      fa.remove(fa.length - 1)
      fb.remove(fb.length - 1)
    case Undo.Violated =>
      violated = null
      contradicted = null
