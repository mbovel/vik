package net.bovel.playground

import org.scalajs.dom
import org.scalajs.dom.{document, html}
import net.bovel.smtlib.{Parser, Printer}
import net.bovel.vik.Session

/** The SMT-LIB page: a script in, the responses of its commands and
 *  the script as VIK plays it (bit-vectors lowered to integers) out.
 */
object Smt2Page:
  def setup(): Unit =
    val input = document.getElementById("smt2-input").asInstanceOf[html.TextArea]
    val output = document.getElementById("smt2-output")
    val lowered = document.getElementById("smt2-lowered")
    val error = document.getElementById("smt2-error")

    def run(): Unit =
      error.textContent = ""
      output.textContent = ""
      lowered.textContent = ""
      try
        val script = Parser.parse(input.value)
        lowered.textContent = Printer.print(script)
        val start = System.currentTimeMillis()
        val responses = Session.run(script)
        val ms = System.currentTimeMillis() - start
        output.textContent = responses.mkString("\n") + s"\n($ms ms)"
      catch case e: Exception => error.textContent = e.getMessage

    document.getElementById("smt2-run").addEventListener("click", (_: dom.Event) => run())
    run()
