package net.bovel.playground

import org.scalajs.dom.document

/** One Scala.js bundle for both pages: each page is recognized by its elements.
 */
object Main:
  def main(args: Array[String]): Unit =
    if document.getElementById("smt2-input") != null then Smt2Page.setup()
    else if document.getElementById("input") != null then EGraphPage.start()
