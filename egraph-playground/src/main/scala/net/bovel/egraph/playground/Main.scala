package net.bovel.egraph.playground

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

import org.scalajs.dom
import org.scalajs.dom.{document, html}
import net.bovel.egraph.{EClassId, EGraphBuilder}

@js.native
@JSGlobal("Viz")
class Viz extends js.Object:
  def renderSVGElement(dot: String): dom.svg.SVG = js.native

@js.native
@JSGlobal("Viz")
object Viz extends js.Object:
  def instance(): js.Promise[Viz] = js.native

object Main:
  private var viz: Viz = null

  def main(args: Array[String]): Unit =
    Viz.instance().`then`[Unit] { v =>
      viz = v
      setup()
    }

  private def setup(): Unit =
    val input = document.getElementById("input").asInstanceOf[html.TextArea]
    val output = document.getElementById("output")
    val errorDiv = document.getElementById("error")

    def update(): Unit =
      errorDiv.textContent = ""
      try
        val builder = EGraphBuilder()
        val lines = input.value.split("\n").map(_.trim).filter(_.nonEmpty)
        for line <- lines do
          parseLine(builder, line)
        builder.rebuild()
        val dot = builder.toDot
        val svg = viz.renderSVGElement(dot)
        output.innerHTML = ""
        output.appendChild(svg)
      catch
        case e: Exception =>
          errorDiv.textContent = e.getMessage

    input.addEventListener("input", (_: dom.Event) => update())
    update()

  /** Parse a term like "f(x, g(y))" into e-graph nodes, returning the e-class
   *  ID.
   */
  private def parseTerm(builder: EGraphBuilder, s: String): (EClassId, Int) =
    parseTermAt(builder, s, 0)

  private def parseTermAt(builder: EGraphBuilder, s: String, start: Int): (EClassId, Int) =
    var i = start
    // skip whitespace
    while i < s.length && s(i).isWhitespace do i += 1

    // read identifier
    val nameStart = i
    while i < s.length && (s(i).isLetterOrDigit || s(i) == '_') do i += 1
    val name = s.substring(nameStart, i)
    if name.isEmpty then throw Exception(s"Expected identifier at position $i")

    // skip whitespace
    while i < s.length && s(i).isWhitespace do i += 1

    if i < s.length && s(i) == '(' then
      // function application
      i += 1 // skip '('
      var args = Vector.empty[EClassId]
      // skip whitespace
      while i < s.length && s(i).isWhitespace do i += 1
      if i < s.length && s(i) != ')' then
        val (arg, ni) = parseTermAt(builder, s, i)
        args = args :+ arg
        i = ni
        while i < s.length && s(i).isWhitespace do i += 1
        while i < s.length && s(i) == ',' do
          i += 1 // skip ','
          val (arg2, ni2) = parseTermAt(builder, s, i)
          args = args :+ arg2
          i = ni2
          while i < s.length && s(i).isWhitespace do i += 1
      if i >= s.length || s(i) != ')' then throw Exception(s"Expected ')' at position $i")
      i += 1 // skip ')'
      (builder.node(name, args*), i)
    else
      // bare identifier: return the symbol class directly
      // so that "f = g" merges the function symbols (enabling congruence)
      (builder.sym(name), i)

  /** Parse a line like "f(x) = g(y)" and merge the two sides. */
  private def parseLine(builder: EGraphBuilder, line: String): Unit =
    if line.startsWith("//") || line.startsWith("#") then return
    val eqIdx = line.indexOf('=')
    if eqIdx < 0 then
      // Just a term, add it without merging
      parseTerm(builder, line)
      ()
    else
      val lhs = line.substring(0, eqIdx).trim
      val rhs = line.substring(eqIdx + 1).trim
      val (lhsId, _) = parseTerm(builder, lhs)
      val (rhsId, _) = parseTerm(builder, rhs)
      builder.merge(lhsId, rhsId)
