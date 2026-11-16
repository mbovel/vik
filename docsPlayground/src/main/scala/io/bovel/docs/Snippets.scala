package io.bovel.docs

import scala.scalajs.js.annotation.JSExportTopLevel

import io.bovel.smtlib.{Parser, Printer}
import io.bovel.vik.Session

/** The solver as a WebAssembly module for the documentation pages: the
 *  snippets of the guide are run by `solve`, in the reader's browser. Linked
 *  into `docs/_assets/js/vik/` by the `docsAssets` task of the build.
 */
object Snippets:
  /** The responses of `script` (`sat`, `unsat`, `unknown`, `((:reason-unknown
   *  ...))`, ...), one per line, or the error; each `check-sat` gets `fuel`
   *  units of work (see `Solver.spent`), so a snippet cannot hang the page.
   */
  @JSExportTopLevel("solve")
  def solve(script: String, fuel: Double): String =
    try Session.run(Parser.parse(script), fuel.toLong).mkString("\n")
    catch case e: Exception => s"error: ${e.getMessage}"

  /** The script as VIK plays it: simplified sorts, bit-vectors lowered to
   *  integers, `ite` named — what the solver actually sees.
   */
  @JSExportTopLevel("lower")
  def lower(script: String): String =
    try Printer.print(Parser.parse(script))
    catch case e: Exception => s"error: ${e.getMessage}"
