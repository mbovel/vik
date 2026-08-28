package net.bovel.egraph

import java.util.Arrays

import scala.collection.immutable.ArraySeq
import scala.collection.mutable.{ArrayBuffer, HashMap}

type EClassId = Int

/** An application e-node `fun(args)`, where `fun` and every argument are
 *  e-classes (function symbols are e-classes too, so `f = g` makes `f(a)` and
 *  `g(a)` congruent).
 *
 *  Nodes are canonicalized in place by [[EGraph.rebuild]], so a node must be
 *  removed from the hashcons before and re-inserted after. Arrays compare by
 *  reference, hence the structural `equals` and `hashCode`.
 */
private final case class ENode(var fun: EClassId, args: Array[EClassId]):
  /** A member of the e-class this node belongs to (use [[EGraph.find]] for its
   *  representative).
   */
  var cls: EClassId = -1

  /** Set once another live node has the same canonical form; the node is then
   *  dropped from the hashcons.
   */
  var dead: Boolean = false

  override def hashCode: Int = 31 * Arrays.hashCode(args) + fun
  override def equals(that: Any): Boolean = that match
    case that: ENode => fun == that.fun && Arrays.equals(args, that.args)
    case _ => false
  override def toString: String = s"$fun(${args.mkString(", ")})"

/** What [[EGraph.pop]] must undo, one record per mutation done inside a level.
 *  Records are undone in reverse order, so each one sees the exact state its
 *  mutation left behind.
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

/** A disequality `a != b` asserted between two terms. */
private final case class Diseq(a: EClassId, b: EClassId)

/** A congruence closure over e-classes identified by dense `Int` ids.
 *
 *  Union-find: `repr(id)` is the representative of `id`, so `find` is one array
 *  read. `merge` repoints every member of the smaller class (they are kept in a
 *  circular list `next`), as Z3 and cvc5 do. Congruence closure is deferred,
 *  egg-style: `merge` records the parents of the absorbed class as `pending`,
 *  and `rebuild` re-canonicalizes them and merges the classes of nodes that
 *  became identical, until no node is pending.
 *
 *  Backtracking: `push` opens a level and `pop` undoes every `add`, `merge` and
 *  `rebuild` done since, through a trail of [[Undo]] records. Nothing is
 *  recorded outside a level, so non-backtracking use costs nothing extra.
 */
final class EGraph:
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

  /** Parent nodes of a class (nodes having it as `fun` or argument); only
   *  meaningful for representatives, `null` when empty.
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

  /** The first disequality found violated; the graph is inconsistent from then
   *  on.
   */
  private var violated: Diseq | Null = null

  /** Hashcons: maps a canonical form to the live node with that form.
   *
   *  `java.util.HashMap` rather than Scala's `mutable.HashMap`: with the same
   *  code otherwise, the Scala map measured about 5% slower on `randomBatch`
   *  (disjoint 95% confidence intervals) and equal within noise elsewhere; see
   *  `egraph-bench/results/2026-08-28-laraserver4-scala-hashmap.md`.
   */
  private val memo = new java.util.HashMap[ENode, ENode]

  /** Nodes whose `fun` or arguments lost their representative since the last
   *  [[rebuild]].
   */
  private val pending = ArrayBuffer.empty[ENode]

  /** Undo records of the mutations done inside the current levels. */
  private val trail = ArrayBuffer.empty[Undo]

  /** Length of `trail` when each open level was pushed. */
  private val levels = ArrayBuffer.empty[Int]

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

  /** Adds the application `fun(args)` and returns its e-class. `args` must be
   *  non-empty: a constant is its own e-class.
   */
  def add(fun: EClassId, args: Array[EClassId]): EClassId =
    require(args.nonEmpty, "an e-node must have at least one argument; a constant is its own e-class")
    val cargs = new Array[EClassId](args.length)
    canonicalArgs(args, cargs)
    val node = ENode(repr(fun), cargs)
    val existing = memo.get(node)
    if existing != null then repr(existing.cls)
    else
      node.cls = newClass()
      memo.put(node, node)
      addUse(node.fun, node)
      for arg <- node.args do addUse(arg, node)
      if recording then trail += Undo.Added(node)
      node.cls

  /** Writes the representative of every element of `src` into `dst`; `dst` may
   *  be `src` itself.
   */
  private def canonicalArgs(src: Array[EClassId], dst: Array[EClassId]): Unit =
    var i = 0
    while i < src.length do
      dst(i) = repr(src(i))
      i += 1

  private def addUse(root: EClassId, node: ENode): Unit =
    val list = uses(root)
    if list == null then uses(root) = ArrayBuffer(node)
    else list += node

  private def removeLastUse(root: EClassId): Unit =
    val list = uses(root).nn
    list.remove(list.length - 1)

  /** Asserts `a != b`; a conflict if they are already equal. */
  def disequate(a: EClassId, b: EClassId): Unit =
    val ra = repr(a)
    val rb = repr(b)
    val d = Diseq(a, b)
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

  /** Whether `a != b` follows from the asserted disequalities and equalities.
   *  `false` when `a` and `b` are equal, even if the graph is inconsistent (see
   *  [[conflict]]).
   */
  def areDisequal(a: EClassId, b: EClassId): Boolean =
    val ra = repr(a)
    val rb = repr(b)
    val fa = forbid(ra)
    val fb = forbid(rb)
    if ra == rb || fa == null || fb == null then false
    else if fa.length <= fb.length then fa.exists(d => repr(d.a) == rb || repr(d.b) == rb)
    else fb.exists(d => repr(d.a) == ra || repr(d.b) == ra)

  /** The two sides of a violated disequality, if the asserted equalities and
   *  disequalities are inconsistent.
   */
  def conflict: Option[(EClassId, EClassId)] =
    val v = violated
    if v == null then None else Some((v.a, v.b))

  /** Returns all e-classes (one per equivalence class) with the `(fun, args)`
   *  of their e-nodes.
   */
  def getClasses(): HashMap[EClassId, ArrayBuffer[(EClassId, ArraySeq[EClassId])]] =
    val res = HashMap.empty[EClassId, ArrayBuffer[(EClassId, ArraySeq[EClassId])]]
    for id <- 0 until count if repr(id) == id do res(id) = ArrayBuffer.empty
    memo.values.forEach(node => res(repr(node.cls)) += ((node.fun, ArraySeq.unsafeWrapArray(node.args.clone()))))
    res

  def merge(a: EClassId, b: EClassId): Unit =
    val ra = repr(a)
    val rb = repr(b)
    if ra == rb then return
    var from = ra
    var into = rb
    if size(ra) > size(rb) then
      from = rb
      into = ra

    // A disequality between the two classes is violated by this merge (checked before repointing).
    val fromForbid = forbid(from)
    val intoForbid = forbid(into)
    if fromForbid != null && violated == null then
      fromForbid.find(d => repr(d.a) == into || repr(d.b) == into).foreach(violate)

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
        if intoUses == null then -1 else intoUses.length,
        fromUses,
        if intoForbid == null then -1 else intoForbid.length,
        fromForbid
      )
    if fromUses != null then
      pending ++= fromUses
      if intoUses == null then uses(into) = fromUses else intoUses ++= fromUses
      uses(from) = null
    if fromForbid != null then
      if intoForbid == null then forbid(into) = fromForbid else intoForbid ++= fromForbid
      forbid(from) = null

  /** Restores the congruence closure after merges. */
  def rebuild(): Unit =
    while pending.nonEmpty do
      val node = pending.remove(pending.length - 1)
      if !node.dead then repair(node)

  private def repair(node: ENode): Unit =
    memo.remove(node) // stored under its previous form
    val oldFun = node.fun
    val oldArgs: Array[EClassId] | Null = if recording then node.args.clone() else null
    node.fun = repr(node.fun)
    canonicalArgs(node.args, node.args)
    val other = memo.get(node)
    if other == null then memo.put(node, node)
    else node.dead = true // congruence: `node` and `other` have the same canonical form
    if recording then trail += Undo.Repaired(node, oldFun, oldArgs.nn, other != null)
    if other != null then merge(node.cls, other.cls)

  /** Opens a level: [[pop]] will undo everything done after this call.
   *  Completes the pending [[rebuild]] first, so that a level starts from a
   *  closed congruence closure.
   */
  def push(): Unit =
    rebuild()
    levels += trail.length

  /** Undoes every `add`, `merge`, `disequate` and `rebuild` done since the last
   *  [[push]].
   */
  def pop(): Unit =
    require(levels.nonEmpty, "pop without a matching push")
    pending.clear() // everything pending was merged in this level
    val mark = levels.remove(levels.length - 1)
    while trail.length > mark do undo(trail.remove(trail.length - 1))

  private def undo(record: Undo): Unit = record match
    case Undo.NewClass =>
      count -= 1
      uses(count) = null
      forbid(count) = null
    case Undo.Added(node) =>
      memo.remove(node)
      removeLastUse(node.fun)
      for arg <- node.args do removeLastUse(arg)
    case Undo.Merged(from, into, intoUsesLen, fromUses, intoForbidLen, fromForbid) =>
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
        if intoUsesLen < 0 then uses(into) = null else uses(into).nn.takeInPlace(intoUsesLen)
        uses(from) = fromUses
      if fromForbid != null then
        if intoForbidLen < 0 then forbid(into) = null else forbid(into).nn.takeInPlace(intoForbidLen)
        forbid(from) = fromForbid
    case Undo.Repaired(node, fun, args, died) =>
      if died then node.dead = false else memo.remove(node)
      node.fun = fun
      System.arraycopy(args, 0, node.args, 0, args.length)
      memo.put(node, node)
    case Undo.Disequated(ra, rb) =>
      val fa = forbid(ra).nn
      val fb = forbid(rb).nn
      fa.remove(fa.length - 1)
      fb.remove(fb.length - 1)
    case Undo.Violated =>
      violated = null
