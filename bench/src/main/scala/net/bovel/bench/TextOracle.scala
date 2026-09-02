package net.bovel.bench

import java.io.{ByteArrayOutputStream, PrintStream, StringReader}

import ap.api.SimpleAPI
import com.microsoft.z3.{Context, Native}
import io.github.cvc5.{InputParser, Solver as Cvc5Solver, TermManager}
import io.github.cvc5.modes.InputLanguage
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

  /** VIK's own front end: a `Session` on the text. */
  def vik(text: String): List[Boolean] =
    net.bovel.vik.Session().run(text).collect { case "sat" => true; case "unsat" => false }

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

  /** cvc5's `InputParser` on a fresh solver, each command invoked as its binary
   *  would (`incremental` set for a script that pushes, as the binary's command
   *  line would): the `cvc5` benchmark. The binding's objects are the caller's
   *  to free, as after [[Cvc5Replay.check]].
   */
  def cvc5(text: String): List[Boolean] =
    val tm = TermManager()
    val solver = Cvc5Solver(tm)
    try
      if text.contains("(push") then solver.setOption("incremental", "true")
      val parser = InputParser(solver)
      parser.setStringInput(InputLanguage.SMT_LIB_2_6, text, "script")
      val symbols = parser.getSymbolManager
      var results = List.empty[Boolean]
      var command = parser.nextCommand()
      while !command.isNull do
        val output = command.invoke(solver, symbols)
        if output.startsWith("sat") || output.startsWith("unsat") then results ::= output.startsWith("sat")
        command = parser.nextCommand()
      results.reverse
    finally solver.deletePointer()

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
