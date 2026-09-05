package io.bovel.vik.graal

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/** Every suite the JVM runs, run again by the native image they are built into
 *  (`graal/nativeImageTest`, then `graal/nativeImageTestRun`): what the binary
 *  answers, not only that it starts. The image carries no test framework, so
 *  the suites come from [[Suites]], which the build generates from what sbt
 *  discovers, and their tests are called here — `munitTests()` is the list sbt
 *  runs, in its order.
 */
object TestMain:
  def main(args: Array[String]): Unit =
    var run = 0
    val failures = List.newBuilder[String]
    for suite <- Suites.all do
      val name = suite.getClass.getName.stripSuffix("$")
      suite.beforeAll()
      // The tests munit marks ignored are the ones the JVM run skips too (corpus/ignore.txt).
      for test <- suite.munitTests() if !test.tags.contains(munit.Ignore) do
        run += 1
        val started = System.nanoTime()
        try
          Await.result(test.body(), Duration.Inf)
          ()
        catch
          case error: Throwable =>
            failures += s"$name.${test.name}: ${error.getMessage}"
        // The slow tests, named: a native run that grows tells which files, not only that it did.
        val seconds = (System.nanoTime() - started) / 1e9
        if seconds >= 1.0 then println(f"T $seconds%.2f $name.${test.name}")
      suite.afterAll()
    val failed = failures.result()
    for failure <- failed do println(s"X $failure")
    println(s"native image: $run tests, ${failed.length} failed")
    if failed.nonEmpty then sys.exit(1)
