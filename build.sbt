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

lazy val egraphPlayground = project
  .in(file("egraph-playground"))
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(egraph.js(scala3Version))
  .settings(
    name := "egraph-playground",
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies += "org.scala-js" %% "scalajs-dom" % "2.8.1"
  )

// JMH benchmarks for the e-graph (JVM only): `sbt "egraphBench/Jmh/run"`.
lazy val egraphBench = project
  .in(file("egraph-bench"))
  .enablePlugins(JmhPlugin)
  .dependsOn(egraph.jvm(scala3Version))
  .settings(name := "egraph-bench")

// Generates the subprojects `cdclt` (JVM) and `cdcltJS` from the same sources.
lazy val cdclt = projectMatrix
  .in(file("cdclt"))
  .settings(
    name := "cdclt",
    libraryDependencies += "org.scalameta" %% "munit" % "1.3.5" % Test
  )
  .jvmPlatform(scalaVersions = Seq(scala3Version))
  .jsPlatform(scalaVersions = Seq(scala3Version))

// JMH benchmarks for the CDCL(T) solver (JVM only): `sbt "cdcltBench/Jmh/run"`.
lazy val cdcltBench = project
  .in(file("cdclt-bench"))
  .enablePlugins(JmhPlugin)
  .dependsOn(cdclt.jvm(scala3Version))
  .settings(name := "cdclt-bench")

// S-expression parser: subprojects `sexpr` (JVM) and `sexprJS`.
lazy val sexpr = projectMatrix
  .in(file("sexpr"))
  .settings(
    name := "sexpr",
    libraryDependencies += "org.scalameta" %% "munit" % "1.3.5" % Test
  )
  .jvmPlatform(scalaVersions = Seq(scala3Version))
  .jsPlatform(scalaVersions = Seq(scala3Version))

// SMT expressions and solver over UF: subprojects `smt` (JVM) and `smtJS`.
lazy val smt = projectMatrix
  .in(file("smt"))
  .dependsOn(cdclt, egraph)
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
  .jvmPlatform(scalaVersions = Seq(scala3Version))
  .jsPlatform(scalaVersions = Seq(scala3Version))

lazy val sexprBench = project
  .in(file("sexpr-bench"))
  .enablePlugins(JmhPlugin)
  .dependsOn(sexpr.jvm(scala3Version))
  .settings(name := "sexpr-bench")

lazy val smt2Bench = project
  .in(file("smt2-bench"))
  .enablePlugins(JmhPlugin)
  .dependsOn(smt2.jvm(scala3Version))
  .settings(name := "smt2-bench")

// The runtime classpath of a project as real paths, for scripts (sbt 2 exports virtual file references).
lazy val runtimeClasspath = taskKey[String]("The runtime classpath, colon-separated, as real paths")

// SMT-LIB benchmarks end to end (JVM only): `sbt "smtBench/Jmh/run"`.
lazy val smtBench = project
  .in(file("smt-bench"))
  .enablePlugins(JmhPlugin)
  .dependsOn(smt2.jvm(scala3Version))
  .settings(
    name := "smt-bench",
    // Other solvers, run in-process by the benchmarks and as separate processes by compare.sh.
    libraryDependencies ++= Seq(
      ("io.github.uuverifiers" %% "princess" % "2026-05-20").cross(CrossVersion.for3Use2_13),
      "de.uni-freiburg.informatik.ultimate" % "smtinterpol" % "2.5-916-ga5843d8b"
    ),
    runtimeClasspath := {
      val converter = fileConverter.value
      (Runtime / fullClasspath).value.map(entry => converter.toPath(entry.data).toString).mkString(":")
    }
  )
