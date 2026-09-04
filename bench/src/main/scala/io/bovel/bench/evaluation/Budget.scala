package io.bovel.bench.evaluation

import java.nio.file.{Files, Path, StandardOpenOption}

import scala.jdk.CollectionConverters.*

import io.bovel.smtlib.Corpus

/** The ten-second budget of the warm benchmarks: a solver that needs more per
 *  operation has nothing to say in a table of milliseconds, and nobody waits
 *  minutes on it again. Every solver operation runs under [[run]]; a watchdog
 *  thread halts the forked VM when the running operation passes the budget —
 *  the solvers run in-process and cannot be interrupted, only outlived — after
 *  noting `solver:instance` on `bench/timeouts.txt`. A noted combination does
 *  not start on later runs (a fresh fork reads the ledger, so even the second
 *  fork of the run that discovered it holds back): the operation returns
 *  `refused` at once, and its cell in the table reads 🐢 whatever that measured
 *  (`bench/warm2md.py` reads the ledger). The JMH runs pass `-foe true`: a
 *  halted fork ends the run, and the next run holds the combination back.
 */
object Budget:
  /** Ten seconds, unless `-Dvik.bench.budgetMs=…` says otherwise: the CI dry
   *  run raises it — a JIT-cold single invocation on a slow runner is a smoke
   *  test, not a measurement.
   */
  private val budgetNanos =
    java.lang.Long.getLong("vik.bench.budgetMs", 10000L).nn.longValue * 1_000_000L
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
          System.err.println(s"Budget: $running passed the ten-second budget, noted on $ledger; halting this fork")
          Runtime.getRuntime.halt(66)
    ,
    "benchmark-budget"
  )
  watchdog.setDaemon(true)
  watchdog.start()

  /** One solver operation under the budget. `solver` must be `Suite.method`:
   *  the ledger and the table key cells by it. `refused` is the value of a
   *  combination on the ledger, returned without running.
   */
  def run[A](solver: String, file: String, refused: A)(body: => A): A =
    val combo = s"$solver:${file.stripSuffix(".smt2")}"
    if noted(combo) then return refused
    running = combo
    deadline = System.nanoTime() + budgetNanos
    try body
    catch
      case undecided: Replay.Undecided =>
        // A solver that answers neither way has nothing to say in a table of milliseconds either: noted
        // like one past its budget, so later runs and forks hold it back and its cell carries the mark.
        Files.writeString(ledger, combo + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND)
        System.err.println(s"Budget: ${undecided.getMessage} on $file, noted on $ledger")
        refused
    finally deadline = Long.MaxValue
