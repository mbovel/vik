val scala3Version = "3.9.0"

// In sbt 2, bare settings are common settings injected into all subprojects.
organization := "net.bovel"
version := "0.1.0-SNAPSHOT"
scalaVersion := scala3Version
scalacOptions += "-Yexplicit-nulls"

// Generates the subprojects `egraph` (JVM) and `egraphJS` from the same sources.
lazy val egraph = projectMatrix
  .in(file("egraph"))
  .settings(
    name := "egraph",
    libraryDependencies += "org.scalameta" %% "munit" % "1.3.5" % Test
  )
  .jvmPlatform(scalaVersions = Seq(scala3Version))
  .jsPlatform(scalaVersions = Seq(scala3Version))

// Generates the subprojects `cdclt` (JVM) and `cdcltJS` from the same sources.
lazy val cdclt = projectMatrix
  .in(file("cdclt"))
  .settings(
    name := "cdclt",
    libraryDependencies += "org.scalameta" %% "munit" % "1.3.5" % Test
  )
  .jvmPlatform(scalaVersions = Seq(scala3Version))
  .jsPlatform(scalaVersions = Seq(scala3Version))

// S-expression parser: subprojects `sexpr` (JVM) and `sexprJS`.
lazy val sexpr = projectMatrix
  .in(file("sexpr"))
  .settings(
    name := "sexpr",
    libraryDependencies += "org.scalameta" %% "munit" % "1.3.5" % Test
  )
  .jvmPlatform(scalaVersions = Seq(scala3Version))
  .jsPlatform(scalaVersions = Seq(scala3Version))

// Linear integer arithmetic (simplex and branch and bound): subprojects `lia` (JVM) and `liaJS`.
lazy val lia = projectMatrix
  .in(file("lia"))
  .settings(
    name := "lia",
    libraryDependencies += "org.scalameta" %% "munit" % "1.3.5" % Test
  )
  .jvmPlatform(scalaVersions = Seq(scala3Version))
  .jsPlatform(scalaVersions = Seq(scala3Version))

// SMT expressions and solver over UF: subprojects `smt` (JVM) and `smtJS`.
lazy val smt = projectMatrix
  .in(file("smt"))
  .dependsOn(cdclt, egraph, lia)
  .settings(
    name := "smt",
    libraryDependencies += "org.scalameta" %% "munit" % "1.3.5" % Test
  )
  .jvmPlatform(scalaVersions = Seq(scala3Version))
  .jsPlatform(scalaVersions = Seq(scala3Version))

// SMT-LIB 2 scripts: subprojects `smt2` (JVM) and `smt2JS`.
lazy val smt2 = projectMatrix
  .in(file("smt2"))
  .dependsOn(sexpr, smt)
  .settings(
    name := "smt2",
    libraryDependencies += "org.scalameta" %% "munit" % "1.3.5" % Test
  )
  .jvmPlatform(scalaVersions = Seq(scala3Version)) // JVM-only sources in `src/main/scalajvm`: the corpus on disk
  .jsPlatform(scalaVersions = Seq(scala3Version))

// The JVM tests of `smt2` (the corpus) fork with a fixed heap: the same memory budget on every machine.
lazy val smt2JVM = smt2.jvm(scala3Version)
smt2JVM / Test / fork := true
smt2JVM / Test / javaOptions += "-Xmx2g"

// The browser playgrounds (Scala.js): the e-graph and the SMT-LIB front end.
lazy val playground = project
  .in(file("playground"))
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(egraph.js(scala3Version), smt2.js(scala3Version))
  .settings(
    name := "playground",
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies += "org.scala-js" %% "scalajs-dom" % "2.8.1"
  )

// The runtime classpath of a project as real paths, for scripts (sbt 2 exports virtual file references).
lazy val runtimeClasspath = taskKey[String]("The runtime classpath, colon-separated, as real paths")

// JMH benchmarks of every module (JVM only), and the comparison with other solvers: `sbt "bench/Jmh/run"`.
lazy val bench = project
  .in(file("bench"))
  .enablePlugins(JmhPlugin)
  .dependsOn(
    egraph.jvm(scala3Version),
    cdclt.jvm(scala3Version),
    lia.jvm(scala3Version),
    sexpr.jvm(scala3Version),
    smt.jvm(scala3Version),
    smt2.jvm(scala3Version)
  )
  .settings(
    name := "bench",
    // Other solvers, run in-process by the benchmarks and as separate processes by compare.sh.
    libraryDependencies ++= Seq(
      ("io.github.uuverifiers" %% "princess" % "2026-05-20").cross(CrossVersion.for3Use2_13),
      "de.uni-freiburg.informatik.ultimate" % "smtinterpol" % "2.5-916-ga5843d8b",
      // The Java bindings of z3 and cvc5 as published by SoSy-Lab (JavaSMT); their native libraries come from
      // bench/fetch-native.sh into bench/native, found through java.library.path below.
      "org.sosy-lab" % "javasmt-solver-z3" % "4.14.0",
      "org.sosy-lab" % "javasmt-solver-cvc5" % "1.2.1-g8594a8e4dc"
    ),
    fork := true, // runs and benchmarks in their own JVM, with the native libraries of z3 and cvc5 on its path
    javaOptions += s"-Djava.library.path=${baseDirectory.value / "native"}",
    Jmh / run / javaOptions += s"-Djava.library.path=${baseDirectory.value / "native"}",
    runtimeClasspath := {
      val converter = fileConverter.value
      (Runtime / fullClasspath).value.map(entry => converter.toPath(entry.data).toString).mkString(":")
    }
  )

// Scala.js on the WebAssembly backend: ES modules and ES 2022 are required; the browser playground stays on the
// JavaScript backend, which every browser runs.
import org.scalajs.linker.interface.ESVersion
lazy val egraphJS = egraph.js(scala3Version)
lazy val cdcltJS = cdclt.js(scala3Version)
lazy val sexprJS = sexpr.js(scala3Version)
lazy val liaJS = lia.js(scala3Version)
lazy val smtJS = smt.js(scala3Version)
lazy val smt2JS = smt2.js(scala3Version)
egraphJS / scalaJSLinkerConfig ~=
  { _.withESFeatures(_.withUseWebAssembly(true).withESVersion(ESVersion.ES2022)).withModuleKind(ModuleKind.ESModule) }
cdcltJS / scalaJSLinkerConfig ~=
  { _.withESFeatures(_.withUseWebAssembly(true).withESVersion(ESVersion.ES2022)).withModuleKind(ModuleKind.ESModule) }
sexprJS / scalaJSLinkerConfig ~=
  { _.withESFeatures(_.withUseWebAssembly(true).withESVersion(ESVersion.ES2022)).withModuleKind(ModuleKind.ESModule) }
liaJS / scalaJSLinkerConfig ~=
  { _.withESFeatures(_.withUseWebAssembly(true).withESVersion(ESVersion.ES2022)).withModuleKind(ModuleKind.ESModule) }
smtJS / scalaJSLinkerConfig ~=
  { _.withESFeatures(_.withUseWebAssembly(true).withESVersion(ESVersion.ES2022)).withModuleKind(ModuleKind.ESModule) }
smt2JS / scalaJSLinkerConfig ~=
  { _.withESFeatures(_.withUseWebAssembly(true).withESVersion(ESVersion.ES2022)).withModuleKind(ModuleKind.ESModule) }
