package io.bovel.vik.proofs

import java.nio.file.{Files, Path}

import scala.jdk.CollectionConverters.*

import io.bovel.smtlib.Parser
import io.bovel.vik.{Session, Solver}

/** Writes the CPC proof of each script of a corpus whose last answer is
 *  `unsat`: `proofs/runMain io.bovel.vik.proofs.CpcCorpus <corpus> <out>
 *  [seconds]`, the corpus a directory or a file listing the scripts' paths,
 *  each script solved with proofs within `seconds` (10 by default). Each
 *  proof is written as `Cnnnnn.cpc`, for `proofs/cpc/check.sh`. Prints a line
 *  per script: its path, the file written, and what came of it — `written`,
 *  why no proof was written, or the answer when it is not `unsat`.
 */
object CpcCorpus:
  def main(args: Array[String]): Unit =
    val root = Path.of(args(0))
    val out = Path.of(args(1))
    val seconds = args.lift(2).fold(10L)(_.toLong)
    Files.createDirectories(out)
    val files =
      if Files.isDirectory(root) then
        Files.walk(root).iterator.asScala.filter(_.toString.endsWith(".smt2")).map(_.toString).toList.sorted
      else Files.readAllLines(root).asScala.toList.filter(_.nonEmpty)
    for (path, i) <- files.zipWithIndex do
      val name = f"C$i%05d"
      val outcome =
        try
          val script = Parser.parse(Files.readString(Path.of(path)))
          val session = Session(
            Solver(proofs = true),
            timeoutMs = Some(seconds * 1000),
            deadline = Some(System.nanoTime + seconds * 1000000000L)
          )
          session.keepProofs = true
          val answers = session.run(script).filter(a => a == "sat" || a == "unsat" || a == "unknown")
          if answers.lastOption != Some("unsat") then answers.lastOption.getOrElse("no answer")
          else
            session.proof() match
              case Right(certificate) =>
                Files.writeString(out.resolve(s"$name.cpc"), Cpc(certificate))
                "written"
              case Left(why) => s"no proof: ${why.linesIterator.nextOption().getOrElse("").take(200)}"
        catch
          case e: Cpc.Unsupported => s"unsupported: ${String.valueOf(e.getMessage).take(200)}"
          case e: Throwable => s"error: ${e.getClass.getSimpleName}: ${String.valueOf(e.getMessage).take(200)}"
      println(s"$path\t$name\t$outcome")
