package net.bovel.bench

import com.microsoft.z3.Context
import io.github.cvc5.Context as Cvc5Context
import net.bovel.smtlib.{Corpus, Parser}

/** Checks a solver's [[Replay]] against its own front end on some files:
 *  `sbt "bench/runMain net.bovel.bench.replaycheck z3 3 file.smt2 …"` (or
 *  `cvc5`, `princess`, `smtinterpol`). Each file gets both the replay and the
 *  [[TextOracle]], best of `reps` runs, and a line with their times: a mismatch
 *  in answers or a replay slower than twice the front end is flagged, since the
 *  replay then hands the solver something its parser would not have built.
 *  Prints the number of flagged files last.
 *
 *  Each run holds to the ten-second budget of the warm table: past it, that
 *  side reads 🐢 and is flagged only if the other side finished. The solvers
 *  cannot be interrupted, so a run past the budget is left to its thread and
 *  outlived — which is why z3 gets a context per run, closed by the thread that
 *  used it.
 */
@main def replaycheck(solver: String, reps: Int, files: String*): Unit =
  Natives.z3
  val budgetMs = 10000L

  var leaked = false // a run past the budget, still on its thread

  /** The best time of the runs that finished — infinite when none did, the runs
   *  stopping at the first past the budget.
   */
  def best(run: => List[Boolean]): (List[Boolean], Double) =
    var answers: List[Boolean] = Nil
    var ms = Double.PositiveInfinity
    var over = false
    for _ <- 1 to reps if !over do
      val thread = Thread(() => answers = run)
      thread.setDaemon(true)
      val start = System.nanoTime()
      thread.start()
      thread.join(budgetMs)
      if thread.isAlive then over = true else ms = ms min (System.nanoTime() - start) / 1e6
    leaked |= over
    // cvc5's binding frees the objects of its runs only all together — never from under a leaked run.
    if solver == "cvc5" && !leaked then Cvc5Context.deletePointers()
    (answers, ms)
  def show(answers: List[Boolean], ms: Double): String =
    if ms.isInfinite then "🐢" else answers.map(if _ then "sat" else "unsat").mkString(",")
  def time(ms: Double): String = if ms.isInfinite then "        🐢" else f"$ms%9.2f ms"
  var flagged = 0
  for file <- files do
    val text = java.nio.file.Files.readString(Corpus.find(file))
    val program = Replay.compile(Parser.parse(text))
    val (replayed, replayMs) = best(solver match
      case "z3" =>
        val ctx = Context();
        try Z3Replay.check(program, ctx)
        finally ctx.close()
      case "cvc5" => Cvc5Replay.check(program)
      case "princess" => PrincessReplay.check(program)
      case "smtinterpol" => SmtInterpolReplay.check(program))
    val (parsed, textMs) = best(solver match
      case "z3" => TextOracle.z3(text)
      case "cvc5" => TextOracle.cvc5(text)
      case "princess" => TextOracle.princess(text)
      case "smtinterpol" => TextOracle.smtinterpol(text))
    val flag =
      if replayMs.isInfinite && textMs.isInfinite then ""
      else if show(replayed, replayMs) != show(parsed, textMs) then
        s"  ANSWERS ${show(replayed, replayMs)} vs ${show(parsed, textMs)}"
      else if replayMs > 2 * textMs && replayMs - textMs > 1 then "  SLOWER"
      else ""
    if flag.nonEmpty then flagged += 1
    println(f"$file%-60s replay ${time(replayMs)}  text ${time(textMs)}$flag")
  println(s"$flagged flagged")
  System.exit(0) // not waiting on the runs past the budget
