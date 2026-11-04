package io.bovel.bench.stainless

import java.nio.file.{Files, Path}
import java.util.concurrent.{Executors, TimeUnit}
import scala.jdk.CollectionConverters.*

import io.bovel.smtlib as s2
import io.bovel.vik.Session

/** Replays dumped solver sessions, warm, in one JVM: `StainlessReplay <list file> <seconds per file> <threads>
 *  <passes> <output tsv>`. The tsv has a line per file of the last pass: milliseconds, then `ok`, `timeout` or
 *  the error, then the answers.
 */
object StainlessReplay:
  private def one(path: Path, seconds: Long): (Long, String, String) =
    var outcome = "timeout"
    var answers = ""
    val start = System.nanoTime
    val worker = new Thread(null, () => {
      try
        val script = s2.Parser().parse(Files.readString(path))
        val session = Session(deadline = Some(System.nanoTime + seconds * 1000000000L))
        val out = session.run(script)
        val checks = out.filter(a => a == "sat" || a == "unsat" || a == "unknown")
        answers = checks.map { case "sat" => "s"; case "unsat" => "u"; case _ => "?" }.mkString
        outcome = "ok"
      catch
        case e: StackOverflowError => outcome = "stackoverflow"
        case e: OutOfMemoryError => outcome = "oom"
        case e: Throwable => outcome = s"${e.getClass.getSimpleName}: ${e.getMessage}".replaceAll("\\s+", " ").take(160)
    }, "replay", 1L << 29)
    worker.setDaemon(true)
    worker.start()
    worker.join(seconds * 1000 + 2000)
    val ms = (System.nanoTime - start) / 1000000
    if worker.isAlive then worker.interrupt()
    (ms, outcome, answers)

  def main(args: Array[String]): Unit =
    val files = Files.readAllLines(Path.of(args(0))).asScala.toList.map(Path.of(_))
    val seconds = args(1).toLong
    val threads = args(2).toInt
    val passes = args(3).toInt
    var last = Vector.empty[String]
    for pass <- 1 to passes do
      val pool = Executors.newFixedThreadPool(threads)
      val futures = files.map(f => pool.submit(() => { val (ms, o, a) = one(f, seconds); s"$f\t$ms\t$o\t$a" }))
      pool.shutdown()
      last = futures.map(_.get()).toVector
      System.err.println(s"pass $pass: ${last.map(_.split("\t")(1).toLong).sum} ms")
    Files.write(Path.of(args(4)), last.asJava)
    System.exit(0)
