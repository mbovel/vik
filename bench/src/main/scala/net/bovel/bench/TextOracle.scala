package net.bovel.bench

import java.io.{ByteArrayOutputStream, PrintStream, StringReader}

import ap.api.SimpleAPI
import com.microsoft.z3.{Context, Native}
import de.uni_freiburg.informatik.ultimate.smtinterpol.DefaultLogger
import de.uni_freiburg.informatik.ultimate.smtinterpol.option.OptionMap
import de.uni_freiburg.informatik.ultimate.smtinterpol.smtlib2.{ParseEnvironment, SMTInterpol}

/** Each solver's own SMT-LIB front end, run in-process on the script's text:
 *  the reference a [[Replay]] is checked against — same answers, and a time the
 *  replay must not stray far from, or it hands the solver something its parser
 *  would not have built.
 */
object TextOracle:
  private def answers(output: String): List[Boolean] =
    output.linesIterator.map(_.trim).collect { case "sat" => true; case "unsat" => false }.toList

  /** `SimpleAPI.execSMTLIB`, what `ap.CmdlMain +incremental` runs. */
  def princess(text: String): List[Boolean] =
    val out = ByteArrayOutputStream()
    Console.withOut(out) {
      SimpleAPI.withProver(p => p.execSMTLIB(StringReader(text)))
    }
    answers(out.toString)

  /** `Z3_eval_smtlib2_string` in a context of its own: the call keeps the
   *  script's logic and declarations in the context, and a second script would
   *  collide with them.
   */
  def z3(text: String): List[Boolean] =
    val ctx = Context()
    try answers(Native.evalSmtlib2String(ctx.nCtx(), text))
    finally ctx.close()

  /** SMTInterpol's `ParseEnvironment`, what its `Main` runs, minus the
   *  `System.exit` of `(exit)`.
   */
  def smtinterpol(text: String): List[Boolean] =
    // The front end writes to the System.out of its construction: redirect first.
    val out = ByteArrayOutputStream()
    val saved = System.out
    System.setOut(PrintStream(out, true))
    try
      val options = OptionMap(DefaultLogger(), true)
      val script = SMTInterpol(options) // its constructor resets the options: set them after
      options.set(":verbosity", 0)
      options.set(":print-success", false)
      val env = new ParseEnvironment(script, options):
        override def exit(): Unit = ()
        override def exitWithStatus(status: Int): Unit = ()
      env.parseStream(StringReader(text), "<script>")
    finally System.setOut(saved)
    answers(out.toString)
