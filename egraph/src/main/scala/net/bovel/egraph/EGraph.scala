package net.bovel.egraph

import java.util.Arrays

import scala.collection.mutable.{ArrayBuffer, HashMap}

type EClassId = Int

/** An application e-node `fun(args)`, where `fun` and every argument are e-classes (function symbols are e-classes
  * too, so `f = g` makes `f(a)` and `g(a)` congruent).
  *
  * Nodes are canonicalized in place by [[EGraph.rebuild]]: after a rebuild, `fun` and `args` are representatives.
  */
final class ENode private[egraph] (private[egraph] var _fun: EClassId, private[egraph] val _args: Array[EClassId]):
  /** A member of the e-class this node belongs to (use [[EGraph.find]] for its representative). */
  private[egraph] var cls: EClassId = -1
  /** Set once another live node has the same canonical form; the node is then dropped from the hashcons. */
  private[egraph] var dead: Boolean = false

  def fun: EClassId = _fun
  def args: IArray[EClassId] = IArray.unsafeFromArray(_args)

  override def hashCode: Int = 31 * Arrays.hashCode(_args) + _fun
  override def equals(that: Any): Boolean = that match
    case that: ENode => _fun == that._fun && Arrays.equals(_args, that._args)
    case _           => false
  override def toString: String = s"$fun(${_args.mkString(", ")})"

/** A congruence closure over e-classes identified by dense `Int` ids.
  *
  * Union-find: `repr(id)` is the representative of `id`, so `find` is one array read. `merge` repoints every member of
  * the smaller class (they are kept in a circular list `next`), as Z3 and cvc5 do. Congruence closure is deferred,
  * egg-style: `merge` records the parents of the absorbed class as `pending`, and `rebuild` re-canonicalizes them
  * and merges the classes of nodes that became identical, until no node is pending.
  */
final class EGraph:
  private var count = 0
  private var repr = new Array[EClassId](16) // representative of each id
  private var next = new Array[EClassId](16) // circular list of the members of a class
  private var size = new Array[Int](16) // number of members, valid for representatives
  private var uses = new Array[ArrayBuffer[ENode]](16) // parent nodes, valid for representatives; null when empty
  private val memo = new java.util.HashMap[ENode, ENode] // hashcons: canonical form -> the live node with that form
  private val pending = ArrayBuffer.empty[ENode] // nodes whose `fun` or arguments lost their representative

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
    var i = 0
    while i < args.length do
      cargs(i) = repr(args(i))
      i += 1
    val node = ENode(repr(fun), cargs)
    val existing = memo.get(node)
    if existing != null then repr(existing.cls)
    else
      node.cls = newClass()
      memo.put(node, node)
      addUse(node._fun, node)
      for arg <- cargs do addUse(arg, node)
      node.cls

  private def addUse(root: EClassId, node: ENode): Unit =
    var list = uses(root)
    if list == null then
      list = ArrayBuffer.empty
      uses(root) = list
    list += node

  /** Returns all e-classes (one per equivalence class) with their e-nodes. */
  def getClasses(): HashMap[EClassId, ArrayBuffer[ENode]] =
    val res = HashMap.empty[EClassId, ArrayBuffer[ENode]]
    for id <- 0 until count if repr(id) == id do res(id) = ArrayBuffer.empty
    memo.values.forEach(node => res(repr(node.cls)) += node)
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

    // Repoint every member of the smaller class, then splice the two circular member lists.
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
    node._fun = repr(node._fun)
    val args = node._args
    var i = 0
    while i < args.length do
      args(i) = repr(args(i))
      i += 1
    val other = memo.get(node)
    if other == null then memo.put(node, node)
    else
      // Congruence: `node` and `other` have the same canonical form.
      node.dead = true
      merge(node.cls, other.cls)
