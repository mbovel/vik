package net.bovel.bench.evaluation

import net.bovel.smtlib.{Corpus, Parser}

/** An instance as the suites take it: the text of the file, for the solvers
 *  that parse; the [[Replay]] compiled from it once, for the ones handed the
 *  terms; the logic the script sets, for z3's solver.
 */
final class Instance(val file: String):
  val text: String = java.nio.file.Files.readString(Corpus.find(file))
  val replay: Replay = Replay.compile(Parser.parse(text))
  val logic: String | Null = "\\(set-logic (\\S+)\\)".r.findFirstMatchIn(text).map(_.group(1)).orNull
