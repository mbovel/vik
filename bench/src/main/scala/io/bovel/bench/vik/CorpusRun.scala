package io.bovel.bench.vik

import java.nio.file.{Files, Path}
import java.util.concurrent.Executors

import scala.jdk.CollectionConverters.*

import io.bovel.smtlib.{Corpus, Parser}
import io.bovel.vik.Session

/** Runs corpus files through VIK, warm, in one JVM, and writes how long each
 *  took and what it answered:
 *  `sbt "bench/runMain io.bovel.bench.vik.CorpusRun <seconds per file> <threads> <passes> <output tsv> [regex]"`.
 *  The regex selects files among the whole corpus (`/stainless/`, `^QF_BV/`;
 *  all of them by default); an absolute directory in its place is run instead,
 *  for files outside the corpus — the sessions `bench/import-stainless.py` is
 *  about to import. The tsv has a line per file of the last pass, the earlier
 *  ones being the warm-up: its path, the milliseconds, then `ok`, `timeout` or
 *  the error, then the answers as `s`, `u` and `?`.
 *
 *  The times are for comparing two builds file by file, not for a table: the
 *  files share the JVM, its compiler and its collector, `threads` at a time.
 *  The budget is the session's deadline, which ends a search; a file that is
 *  still running two seconds past it is somewhere the deadline does not reach,
 *  and is left behind on its thread, which it keeps along with its memory. The
 *  files that `corpus/ignore.txt` says to `skip` do just that, enough of them
 *  to fill the heap, and are left out.
 */
object CorpusRun:
  def main(args: Array[String]): Unit =
    val seconds = args(0).toLong
    val threads = args(1).toInt
    val passes = args(2).toInt
    val files: List[(String, Path)] = args.lift(4).map(Path.of(_)) match
      case Some(directory) if directory.isAbsolute && Files.isDirectory(directory) =>
        val stream = Files.walk(directory)
        try
          stream.iterator().asScala.map(_.toString).filter(_.endsWith(".smt2")).toList.sorted.map(f => f -> Path.of(f))
        finally stream.close()
      case _ =>
        val regex = args.lift(4).getOrElse("").r
        val skipped = Corpus.ignored.filter(_._2.startsWith("skip")).keySet
        val selected = Corpus.files.filter(regex.findFirstIn(_).isDefined)
        System.err.println(s"${selected.count(skipped)} files left out: `skip` in corpus/ignore.txt")
        selected.filterNot(skipped).map(f => f -> Corpus.root.resolve(f))
    var last = Vector.empty[String]
    for pass <- 1 to passes do
      val pool = Executors.newFixedThreadPool(threads)
      val lines = files.map((name, path) => pool.submit(() => s"$name\t${run(path, seconds)}"))
      pool.shutdown()
      last = lines.map(_.get()).toVector
      System.err.println(s"pass $pass: ${files.length} files, ${last.map(_.split("\t")(1).toLong).sum} ms")
    Files.write(Path.of(args(3)), last.asJava)
    System.exit(0) // whatever the files left behind

  /** One file in a session of its own: the milliseconds, the outcome and the
   *  answers, as the tsv has them. The thread has the stack a deep script asks
   *  of the parser.
   */
  private def run(path: Path, seconds: Long): String =
    var outcome = "timeout"
    var answers = ""
    val start = System.nanoTime
    val worker = Thread(
      null,
      () =>
        try
          val script = Parser.parse(Files.readString(path))
          val responses = Session.run(script, deadline = Some(start + seconds * 1000000000L))
          answers = responses.collect { case "sat" => "s"; case "unsat" => "u"; case "unknown" => "?" }.mkString
          outcome = "ok"
        catch
          case _: StackOverflowError => outcome = "stackoverflow"
          case _: OutOfMemoryError => outcome = "oom"
          case e: Throwable =>
            outcome = s"${e.getClass.getSimpleName}: ${e.getMessage}".replaceAll("\\s+", " ").take(160)
      ,
      "corpusrun",
      1L << 29
    )
    worker.setDaemon(true)
    worker.start()
    worker.join(seconds * 1000 + 2000)
    s"${(System.nanoTime - start) / 1000000}\t$outcome\t$answers"
