package net.bovel.egraph

import scala.collection.immutable.ArraySeq
import scala.collection.mutable.{ArrayBuffer, HashMap, LongMap}
import scala.collection.MapView

type EClassId = Long

final case class EClass(
    repr: EClassId,
    members: ArrayBuffer[EClassId] = ArrayBuffer.empty,
    uses: ArrayBuffer[(ENode, EClassId)] = ArrayBuffer.empty
)

final case class ENode(
    fun: EClassId,
    args: ArraySeq[EClassId]
)

final class EGraph:
  private var nextClassId = 0L
  private val classes = LongMap.empty[EClass]
  private val hashcons = HashMap.empty[ENode, EClassId]
  private val worklist = ArrayBuffer.empty[EClassId]

  def find(id: EClassId): EClassId =
    classes(id).repr

  /** Returns all e-classes (one per equivalence class). */
  def getClasses(): HashMap[EClassId, ArrayBuffer[ENode]] =
    val res = HashMap.empty[EClassId, ArrayBuffer[ENode]]
    for (id, eclass) <- classes if id == eclass.repr do
      res(id) = ArrayBuffer.empty
    for ((enode, eclassId)) <- hashcons do
      res(find(eclassId)) += enode
    res

  def getClass(id: EClassId): EClass =
    classes(find(id))

  def newClass(): EClassId =
    val id = nextClassId
    nextClassId += 1
    classes(id) = EClass(repr = id, members = ArrayBuffer(id))
    id

  /** Adds an application e-node. `args` must be non-empty: a constant is simply its own e-class. */
  def add(rawNode: ENode): EClassId =
    require(rawNode.args.nonEmpty, "an e-node must have at least one argument; a constant is its own e-class")
    val node = canonicalize(rawNode)
    hashcons.getOrElse(
      node, {
        val id = newClass()
        classes(find(node.fun)).uses += ((node, id))

        for arg <- node.args do
          classes(find(arg)).uses += ((node, id))

        hashcons(node) = id
        id
      }
    )

  def canonicalize(node: ENode): ENode =
    ENode(find(node.fun), node.args.map(find))

  def merge(rawA: EClassId, rawB: EClassId): Unit =
    val a = find(rawA)
    val b = find(rawB)
    if a == b then return

    val (from, into) =
      if classes(a).members.size >= classes(b).members.size then (b, a)
      else (a, b)

    val fromClass = classes(from)
    val intoClass = classes(into)

    for member <- fromClass.members do
      classes(member) = intoClass

    intoClass.members ++= fromClass.members
    intoClass.uses ++= fromClass.uses
    worklist += into

  def rebuild(): Unit =
    while worklist.nonEmpty do
      val todo = worklist.map(find).distinct
      worklist.clear()
      for id <- todo do
        repair(id)

  private def repair(id: EClassId): Unit =
    val eclass = classes(id)
    val oldUses = eclass.uses.toArray
    eclass.uses.clear()

    // Re-canonicalize all uses in the hashcons
    for (oldNode, oldEClassId) <- oldUses do
      hashcons.remove(oldNode)
      val newNode = canonicalize(oldNode)
      hashcons(newNode) = find(oldEClassId)

    // Check for congruences: e-nodes that became identical after canonicalization
    val newUses = HashMap.empty[ENode, EClassId]
    for (oldNode, oldEClassId) <- oldUses do
      val newNode = canonicalize(oldNode)
      newUses.get(newNode) match
        case Some(existingId) =>
          merge(oldEClassId, existingId)
        case None =>
          newUses(newNode) = find(oldEClassId)

    // Assign uses to the (possibly updated) representative
    val repr = classes(find(id))
    for (node, eClassId) <- newUses do
      repr.uses += ((node, eClassId))
