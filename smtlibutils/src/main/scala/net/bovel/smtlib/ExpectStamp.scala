package net.bovel.smtlib

import net.bovel.vik.{Player, Solver, Model}
import java.nio.file.Files

/** Stamps the corpus files that query values — `get-value`, `get-model`,
 *  `get-unsat-core` — with the solver's own responses as `; EXPECT:` lines,
 *  replacing whatever a file brought along: the answers of another solver
 *  print differently, and a core may be a different, equally correct subset.
 *  `SmtLibTest` then holds every response to these lines. Run after a change
 *  that legitimately moves them, review the diff, and commit it:
 *  `sbt "smtlibutils/runMain net.bovel.smtlib.expectstamp"`.
 */
@main def expectstamp(): Unit =
  val fuel = 2000000L
  val ignored = Corpus.ignored
  var stamped = 0
  for file <- Corpus.files if !ignored.contains(file) do
    val path = Corpus.root.resolve(file)
    val text = Files.readString(path)
    if List("(get-value", "(get-model", "(get-unsat-core").exists(text.contains) then
      val responses = Player.run(Parser.parse(text), fuel = fuel)
      val kept = text.linesIterator.filterNot(_.startsWith("; EXPECT:")).toList
      val (header, rest) = kept.span(_.startsWith(";"))
      val expects = responses.map(r => s"; EXPECT: $r")
      Files.writeString(path, (header ++ expects ++ rest).mkString("", "\n", "\n"))
      stamped += 1
  println(s"$stamped files stamped")
