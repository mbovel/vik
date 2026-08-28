package net.bovel.egraph

import java.util.Arrays

import scala.collection.immutable.ArraySeq
import scala.collection.mutable.{ArrayBuffer, HashMap}

type EClassId = Int

/** An application e-node `fun(args)`, where `fun` and every argument are e-classes (function symbols are e-classes
  * too, so `f = g` makes `f(a)` and `g(a)` congruent).
  *
  * Nodes are canonicalized in place by [[EGraph.rebuild]], so a node must be removed from the hashcons before and
  * re-inserted after. Arrays compare by reference, hence the structural `equals` and `hashCode`.
  */
private final case class ENode(var fun: EClassId, args: Array[EClassId]):
  /** A member of the e-class this node belongs to (use [[EGraph.find]] for its representative). */
  var cls: EClassId = -1
  /** Set once another live node has the same canonical form; the node is then dropped from the hashcons. */
  var dead: Boolean = false

  override def hashCode: Int = 31 * Arrays.hashCode(args) + fun
  override def equals(that: Any): Boolean = that match
    case that: ENode => fun == that.fun && Arrays.equals(args, that.args)
    case _           => false
  override def toString: String = s"$fun(${args.mkString(", ")})"

/** A congruence closure over e-classes identified by dense `Int` ids.
  *
  * Union-find: `repr(id)` is the representative of `id`, so `find` is one array read. `merge` repoints every member of
  * the smaller class (they are kept in a circular list `next`), as Z3 and cvc5 do. Congruence closure is deferred,
  * egg-style: `merge` records the parents of the absorbed class as `pending`, and `rebuild` re-canonicalizes them
  * and merges the classes of nodes that became identical, until no node is pending.
  */
final class EGraph:
  /** Number of e-classes created so far; ids are `0 until count` and index the arrays below. */
  private var count = 0

  /** Representative of each e-class id. */
  private var repr = new Array[EClassId](16)

  /** Next member in the circular list of the members of a class. The list is circular rather than nil-terminated so
    * that two classes are joined by swapping the `next` of their representatives, which is O(1), needs no tail
    * pointer or terminator, and is its own inverse (useful for backtracking later).
    */
  private var next = new Array[EClassId](16)

  /** Number of members of a class; only meaningful for representatives. */
  private var size = new Array[Int](16)

  /** Parent nodes of a class (nodes having it as `fun` or argument); only meaningful for representatives, `null`
    * when empty.
    */
  private var uses = new Array[ArrayBuffer[ENode]](16)

  /** Hashcons: maps a canonical form to the live node with that form. */
  private val memo = new java.util.HashMap[ENode, ENode]

  /** Nodes whose `fun` or arguments lost their representative since the last [[rebuild]]. */
  private val pending = ArrayBuffer.empty[ENode]

  def find(id: EClassId): EClassId = repr(id)

  /** The number of e-classes ever created (ids range over `0 until classCount`). */
  def classCount: Int = count

  def newClass(): EClassId =
    if count == repr.length then grow()
    val id = count
    count += 1
    repr(id) = id
    next(id) = id
    size(id) = 1
    id

  private def grow(): Unit =
    val n = repr.length * 2
    repr = Arrays.copyOf(repr, n)
    next = Arrays.copyOf(next, n)
    size = Arrays.copyOf(size, n)
    uses = Arrays.copyOf(uses, n)

  /** Adds the application `fun(args)` and returns its e-class. `args` must be non-empty: a constant is its own
    * e-class.
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
      node.cls

  /** Writes the representative of every element of `src` into `dst`; `dst` may be `src` itself. */
  private def canonicalArgs(src: Array[EClassId], dst: Array[EClassId]): Unit =
    var i = 0
    while i < src.length do
      dst(i) = repr(src(i))
      i += 1

  private def addUse(root: EClassId, node: ENode): Unit =
    var list = uses(root)
    if list == null then
      list = ArrayBuffer.empty
      uses(root) = list
    list += node

  /** Returns all e-classes (one per equivalence class) with the `(fun, args)` of their e-nodes. */
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
    if fromUses != null then
      pending ++= fromUses
      val intoUses = uses(into)
      if intoUses == null then uses(into) = fromUses else intoUses ++= fromUses
      uses(from) = null

  /** Restores the congruence closure after merges. */
  def rebuild(): Unit =
    while pending.nonEmpty do
      val node = pending.remove(pending.length - 1)
      if !node.dead then repair(node)

  private def repair(node: ENode): Unit =
    memo.remove(node) // stored under its previous form
    node.fun = repr(node.fun)
    canonicalArgs(node.args, node.args)
    val other = memo.get(node)
    if other == null then memo.put(node, node)
    else
      // Congruence: `node` and `other` have the same canonical form.
      node.dead = true
      merge(node.cls, other.cls)
