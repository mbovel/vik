package io.bovel.vik.graal

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/** The solver's own test suites, run by the native image the tests are built
 *  into (`graal/nativeImageTestRun`): the same munit suites the JVM runs, so
 *  that what the binary answers is checked, not only that it starts. munit's
 *  runner is a test framework the image does not carry, so the suites are
 *  listed here and their tests called directly — `munitTests()` is the same
 *  list sbt runs, in the same order.
 */
object TestMain:
  private def suites: List[munit.FunSuite] =
    List(
      io.bovel.vik.AdtTest(),
      io.bovel.vik.BitVectorsTest(),
      io.bovel.vik.CoresTest(),
      io.bovel.vik.DatatypesTest(),
      io.bovel.vik.IncrementalConsistencyTest(),
      io.bovel.vik.IntsTest(),
      io.bovel.vik.LiaSolverTest(),
      io.bovel.vik.LoweringTest(),
      io.bovel.vik.OptionsTest(),
      io.bovel.vik.ReadmeExamplesTest(),
      io.bovel.vik.SessionTest(),
      io.bovel.vik.Smt2Test(),
      io.bovel.vik.SolverTest(),
      io.bovel.vik.UfliaTest()
    )

  def main(args: Array[String]): Unit =
    var run = 0
    val failures = List.newBuilder[String]
    for suite <- suites do
      val name = suite.getClass.getSimpleName.stripSuffix("$")
      suite.beforeAll()
      for test <- suite.munitTests() do
        run += 1
        try
          Await.result(test.body(), Duration.Inf)
          ()
        catch
          case error: Throwable =>
            failures += s"$name.${test.name}: ${error.getMessage}"
      suite.afterAll()
    val failed = failures.result()
    for failure <- failed do println(s"X $failure")
    println(s"native image: $run tests, ${failed.length} failed")
    if failed.nonEmpty then sys.exit(1)
