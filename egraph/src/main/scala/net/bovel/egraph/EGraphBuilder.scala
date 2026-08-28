package net.bovel.egraph

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet

/** High-level API for building e-graphs using string-named symbols. */
final class EGraphBuilder:

  val eg = EGraph()
  private val syms = HashMap.empty[String, EClassId]

  def sym(name: String): EClassId =
    syms.getOrElseUpdate(name, eg.newClass())

  /** Adds the application of `funName` to `args`; for a constant use `sym`
   *  instead.
   */
  def node(funName: String, args: EClassId*): EClassId =
    require(args.nonEmpty, s"node(\"$funName\") needs at least one argument; use sym(\"$funName\") for a constant")
    eg.add(sym(funName), args.toArray)

  def find(id: EClassId): EClassId = eg.find(id)
  def merge(a: EClassId, b: EClassId): Unit = eg.merge(a, b)
  def rebuild(): Unit = eg.rebuild()

  /** Returns the symbol name for an e-class ID, if it was created via sym(). */
  def nameOf(id: EClassId): Option[String] =
    syms.collectFirst { case (name, sid) if eg.find(sid) == eg.find(id) => name }

  /** Exports the e-graph as a DOT string for Graphviz rendering. */
  def toDot: String =
    val sb = StringBuilder()
    sb ++= "digraph {\n"
    sb ++= "  compound=true\n"
    sb ++= "  rankdir=TB\n"
    sb ++= "  ranksep=0.8\n"
    sb ++= "  nodesep=0.2\n"
    sb ++= "  node [fontname=\"sans-serif\" fontsize=12 height=0.3 width=0.4]\n"
    sb ++= "  edge [arrowsize=0.6 penwidth=0.8]\n"
    sb ++= "  graph [style=\"dashed,rounded\"]\n"
    sb ++= "\n"

    val names =
      syms.toSeq.groupBy((_, id) => eg.find(id)).mapValues(_.map(_._1))

    val links = ArrayBuffer.empty[(String, EClassId)]

    for (id, nodes) <- eg.getClasses() do
      val myNames = names.getOrElse(id, Nil).mkString(", ")
      sb ++= s"  subgraph cluster_$id {\n"
      sb ++= "    style=dashed\n"
      sb ++= "    margin=8\n"
      sb ++= s"    label=\"$myNames\"\n"
      sb ++= "    fontcolor=\"#888888\"\n"
      sb ++= "    fontname=\"sans-serif\"\n"
      sb ++= "    fontsize=10\n"

      for ((fun, args), idx) <- nodes.zipWithIndex do
        val nodeId = s"n${id}_$idx"
        val funSelf = eg.find(fun) == id
        val funLabel = if funSelf then "⟲" else ""
        val argLabels = args.indices.map { i =>
          val argSelf = eg.find(args(i)) == id
          if argSelf then s"<a$i>⟲" else s"<a$i>"
        }
        val label = (s"<f>$funLabel" +: argLabels).mkString("|")
        if !funSelf then links += ((s"$nodeId:f", fun))
        for (arg, argIdx) <- args.zipWithIndex do
          if eg.find(arg) != id then links += ((s"$nodeId:a$argIdx", arg))
        sb ++= s"    $nodeId [label=\"$label\" shape=record]\n"

      if nodes.isEmpty then
        // If the e-class has no enodes, add an invisible anchor node so edges have somewhere to point to
        sb ++= s"    n${id}_0 [label=\"\" shape=none style=invisible width=0.01 height=0.01]\n"

      sb ++= "  }\n\n"
    end for

    // Draw edges from e-node ports to e-class anchors
    for (from, to) <- links do
      val target = eg.find(to)
      sb ++= s"  $from -> n${target}_0 [lhead=cluster_$target]\n"

    sb ++= "}\n"
    sb.toString
