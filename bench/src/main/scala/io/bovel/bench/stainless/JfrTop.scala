package io.bovel.bench.stainless

import java.nio.file.Path

import scala.collection.mutable
import scala.jdk.CollectionConverters.*

import jdk.jfr.consumer.RecordingFile

/** Self and inclusive time per method of a JFR recording:
 *  `JfrTop <file> [lines]`.
 */
object JfrTop:
  def main(args: Array[String]): Unit =
    val self = mutable.Map.empty[String, Int].withDefaultValue(0)
    val incl = mutable.Map.empty[String, Int].withDefaultValue(0)
    val attributed = mutable.Map.empty[String, Int].withDefaultValue(0)
    var n = 0
    val f = RecordingFile(Path.of(args(0)))
    while f.hasMoreEvents do
      val e = f.readEvent()
      if e.getEventType.getName == "jdk.ExecutionSample" && e.getStackTrace != null then
        val frames = e.getStackTrace.getFrames.asScala.map(fr =>
          fr.getMethod.getType.getName.replace("io.bovel.", "") + "." + fr.getMethod.getName
        )
        if frames.nonEmpty then
          n += 1; self(frames.head) += 1
          val firstOurs = frames.find(f =>
            f.startsWith("vik.") || f.startsWith("lia.") || f.startsWith("egraph.") || f.startsWith("cdclt.")
          ).getOrElse("?")
          attributed(firstOurs) += 1
          frames.distinct.foreach(incl(_) += 1)
    println(s"samples $n")
    val lines = if args.length > 1 then args(1).toInt else 25
    println("self%  incl%")
    for (k, v) <- self.toSeq.sortBy(-_._2).take(lines) do
      println(f"${100.0 * v / n}%5.1f ${100.0 * incl(k) / n}%5.1f $k")
    println("-- self, charged to the nearest frame of ours")
    for (k, v) <- attributed.toSeq.sortBy(-_._2).take(lines) do println(f"${100.0 * v / n}%5.1f $k")
    println("-- inclusive")
    for (k, v) <- incl.toSeq.filter(_._1.startsWith("vik")).sortBy(-_._2).take(lines) do
      println(f"${100.0 * v / n}%5.1f $k")
