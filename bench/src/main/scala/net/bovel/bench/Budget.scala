package net.bovel.bench

import java.nio.file.{Files, Path, StandardOpenOption}

import scala.jdk.CollectionConverters.*

import net.bovel.smtlib.Corpus

/** The five-second budget of the warm benchmarks: a solver that needs more per
 *  operation has nothing to say in a table of milliseconds, and nobody waits
 *  minutes on it again. Every solver operation runs under [[run]]; a watchdog
 *  thread halts the forked VM when the running operation passes the budget —
 *  the solvers run in-process and cannot be interrupted, only outlived — after
 *  noting `solver:instance` on `bench/timeouts.txt`. A noted combination
 *  refuses to start on later runs (a fresh fork reads the ledger, so even the
 *  second fork of the run that discovered it refuses), and its cell in the
 *  table reads `timeout` (`bench/warm2md.py`). The JMH runs pass `-foe false`:
 *  a halted or refusing fork is a recorded fact, not the end of the evaluation.
 */
object Budget:
  /** Five seconds, unless `-Dvik.bench.budgetMs=…` says otherwise: the CI dry
   *  run raises it — a JIT-cold single invocation on a slow runner is a smoke
   *  test, not a measurement.
   */
  private val budgetNanos =
    java.lang.Long.getLong("vik.bench.budgetMs", 5000L).nn.longValue * 1_000_000L
  private val ledger: Path = Corpus.root.getParent.resolve("bench").resolve("timeouts.txt")
  private val noted: Set[String] =
    if Files.exists(ledger) then Files.readAllLines(ledger).asScala.filter(_.nonEmpty).toSet
    else Set.empty

  @volatile private var running: String = ""
  @volatile private var deadline: Long = Long.MaxValue

  private val watchdog = Thread(
    () =>
      while true do
        Thread.sleep(250)
        if System.nanoTime() > deadline then
          Files.writeString(ledger, running + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND)
          System.err.println(s"Budget: $running passed the five-second budget, noted on $ledger; halting this fork")
          Runtime.getRuntime.halt(66)
    ,
    "benchmark-budget"
  )
  watchdog.setDaemon(true)
  watchdog.start()

  /** One solver operation under the budget. `solver` must be the benchmark
   *  method's name: the ledger and the table key cells by it.
   */
  def run[A](solver: String, file: String)(body: => A): A =
    val combo = s"$solver:${file.stripSuffix(".smt2")}"
    if noted(combo) then
      throw IllegalStateException(s"$combo timed out on an earlier run (bench/timeouts.txt)")
    running = combo
    deadline = System.nanoTime() + budgetNanos
    try body
    finally deadline = Long.MaxValue
