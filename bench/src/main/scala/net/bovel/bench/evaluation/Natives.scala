package net.bovel.bench.evaluation

import java.io.File

/** Loads the native libraries of the z3 bindings: `libz3java.so` depends on
 *  `libz3.so`, which the dynamic linker does not find on `java.library.path`,
 *  so `libz3.so` is loaded explicitly first (as JavaSMT's loader does); the
 *  bindings then load `libz3java` themselves. The libraries come from
 *  `bench/fetch-native.sh`.
 */
object Natives:
  lazy val z3: Unit =
    val dirs = System.getProperty("java.library.path", "").split(File.pathSeparator)
    val lib = dirs.map(File(_, "libz3.so")).find(_.exists).getOrElse(
      throw IllegalStateException("libz3.so not found on java.library.path: run bench/fetch-native.sh")
    )
    System.load(lib.getAbsolutePath)
