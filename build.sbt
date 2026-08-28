val scala3Version = "3.9.0"

// In sbt 2, bare settings are common settings injected into all subprojects.
organization := "net.bovel"
version := "0.1.0-SNAPSHOT"
scalaVersion := scala3Version

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
