package io.bovel.smtlib

import java.nio.file.Files
import java.util.concurrent.{Executors, TimeUnit}

import scala.jdk.CollectionConverters.*

/** Answers the corpus files' `check-sat`s with another solver and holds the
 *  files' statuses to it: a `:status unknown` (or no status at all) before a
 *  `check-sat` is replaced by the solver's answer, so that `CorpusTest` checks
 *  the file from then on — half the corpus was `unknown`, the compiler's queries
 *  above all, and an `unknown` checks nothing but termination; a `sat` or
 *  `unsat` the solver contradicts is reported, never rewritten: one of the two
 *  is wrong, and that is for a person to settle. A stamped file records the
 *  solver in its header. Usage:
 *  `sbt "smtlibutils/runMain io.bovel.smtlib.statusstamp [z3|cvc5] [seconds] [prefix ...]"`
 *  — the solver on the PATH (default `z3`), its time per file (default 20), the
 *  corpus paths to consider (default all, `ignore.txt` included).
 */
@main def statusstamp(args: String*): Unit =
  val solver = args.headOption.getOrElse("z3")
  val seconds = args.lift(1).map(_.toInt).getOrElse(20)
  val prefixes = args.drop(2)
  val files = Corpus.files.filter(f => prefixes.isEmpty || prefixes.exists(f.startsWith))
  val version = run(List(solver, "--version"), 10).headOption.getOrElse("").trim.replace(" - 64 bit", "")
  println(s"$version on ${files.length} files, $seconds s each")

  val pool = Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors() min 16)
  val results = files.map(file => file -> pool.submit(() => answers(solver, file, seconds)))
  var stamped = 0
  val disagreements = List.newBuilder[String]
  val unanswered = List.newBuilder[String]
  for (file, future) <- results do
    val path = Corpus.root.resolve(file)
    val lines = Files.readAllLines(path).asScala.toList
    val checks = lines.zipWithIndex.collect { case (l, i) if isCheckSat(l) => i }
    val got = future.get()
    if got.length != checks.length then unanswered += s"$file: ${got.length} answers to ${checks.length} check-sats"
    else
      // The status of a check-sat: the last `(set-info :status ...)` since the previous check-sat.
      val statuses = checks.zip(-1 :: checks).map((at, previous) =>
        (previous + 1 until at).findLast(i => lines(i).startsWith("(set-info :status ")).map(i =>
          (i, lines(i).stripPrefix("(set-info :status ").stripSuffix(")").trim)
        )
      )
      val edits = scala.collection.mutable.TreeMap.empty[Int, (Boolean, String)] // line -> (insert before, text)
      for ((at, status), answer) <- checks.zip(statuses).zip(got) do
        (status, answer) match
          case (_, "unknown") => ()
          case (Some((_, s)), a) if s == a => ()
          case (Some((_, s)), a) if s == "sat" || s == "unsat" => disagreements += s"$file: status $s, $solver $a"
          case (Some((i, _)), a) => edits(i) = (false, s"(set-info :status $a)")
          case (None, a) => edits(at) = (true, s"(set-info :status $a)")
      if edits.nonEmpty then
        val header = lines.takeWhile(_.startsWith(";"))
        val note = s"; statuses: the `unknown` ones answered by $version (statusstamp, ${java.time.LocalDate.now})"
        val body = lines.drop(header.length).zipWithIndex.flatMap((l, j) =>
          val i = j + header.length
          edits.get(i) match
            case Some((true, text)) => List(text, l)
            case Some((false, text)) => List(text)
            case None => List(l)
        )
        Files.writeString(path, (header ++ List(note) ++ body).mkString("", "\n", "\n"))
        stamped += 1
  pool.shutdown()
  println(s"$stamped files stamped")
  val d = disagreements.result()
  val u = unanswered.result()
  if u.nonEmpty then println(s"${u.length} files not fully answered (timeouts, errors):"); u.foreach(l => println(s"  $l"))
  if d.nonEmpty then println(s"${d.length} disagreements with the files' statuses:"); d.foreach(l => println(s"  $l"))

private def isCheckSat(line: String): Boolean =
  val t = line.trim
  t == "(check-sat)" || t.startsWith("(check-sat-assuming") || t.startsWith("(check-sat-using") || t.startsWith("(check-sat ")

/** The solver's answers to the file's `check-sat`s, in order: the lines that
 *  are `sat`, `unsat` or `unknown` (a model or a core in between is skipped);
 *  none when the solver reported an error.
 */
private def answers(solver: String, file: String, seconds: Int): List[String] =
  // The solver reads a copy without the statuses: z3 and cvc5 error on a status they contradict, and
  // the error dropped every answer, so a disagreement was never reported (issue 57).
  val blind = Files.createTempFile("statusstamp", ".smt2")
  Files.writeString(blind, withoutStatuses(Files.readString(Corpus.root.resolve(file))))
  val path = blind.toString
  val command = solver match
    case "z3" => List("z3", "-smt2", s"-T:$seconds", path)
    case "cvc5" => List("cvc5", "--lang=smt2", "--incremental", s"--tlimit=${seconds * 1000}", path)
    case other => List(other, path)
  val output =
    try run(command, seconds + 5).map(_.trim)
    finally Files.delete(blind)
  // An error drops the assertion it is in: the answers after it are to another problem.
  if output.exists(_.startsWith("(error")) then Nil
  else output.filter(l => l == "sat" || l == "unsat" || l == "unknown")

/** The script with every `(set-info :status ...)` line saying `unknown`. */
def withoutStatuses(text: String): String =
  text.linesIterator.map(l => if l.startsWith("(set-info :status ") then "(set-info :status unknown)" else l)
    .mkString("", "\n", "\n")

/** Runs a command with a time limit; its output lines (empty when killed or failed). */
private def run(command: List[String], seconds: Int): List[String] =
  val process = ProcessBuilder(command*).redirectErrorStream(true).start()
  val output = new String(process.getInputStream.readAllBytes())
  if !process.waitFor(seconds, TimeUnit.SECONDS) then
    process.destroyForcibly()
    Nil
  else output.linesIterator.toList
