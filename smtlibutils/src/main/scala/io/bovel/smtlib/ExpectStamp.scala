package io.bovel.smtlib

import java.nio.file.Files

import io.bovel.vik.Session

/** Stamps the corpus files that query values — `get-value`, `get-model`,
 *  `get-unsat-core` — with the solver's own responses as `; EXPECT:` lines,
 *  replacing whatever a file brought along: the answers of another solver print
 *  differently, and a core may be a different, equally correct subset.
 *  `CorpusTest` then holds every response to these lines. Run after a change
 *  that legitimately moves them, review the diff, and commit it:
 *  `sbt "smtlibutils/runMain io.bovel.smtlib.expectstamp"`.
 */
@main def expectstamp(): Unit =
  val fuel = 20_000_000_000L // as CorpusTest
  val ignored = Corpus.ignored
  var stamped = 0
  for file <- Corpus.files if !ignored.contains(file) do
    val path = Corpus.root.resolve(file)
    val text = Files.readString(path)
    if List("(get-value", "(get-model", "(get-unsat-core").exists(text.contains) then
      val responses = Session.run(Parser.parse(text), fuel = fuel)
      val kept = text.linesIterator.filterNot(_.startsWith("; EXPECT:")).toList
      val (header, rest) = kept.span(_.startsWith(";"))
      val expects = responses.map(r => s"; EXPECT: $r")
      Files.writeString(path, (header ++ expects ++ rest).mkString("", "\n", "\n"))
      stamped += 1
  println(s"$stamped files stamped")
