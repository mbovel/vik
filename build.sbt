import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

val scala3Version = "3.8.2"
scalaVersion := scala3Version

lazy val core = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
  .settings(
    name := "scala-egraph-core",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies += "org.scalameta" %%% "munit" % "1.0.4" % Test,
  )

lazy val coreJVM = core.jvm
lazy val coreJS = core.js

lazy val playground = project
  .in(file("playground"))
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(coreJS)
  .settings(
    name := "scala-egraph-playground",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.8.0",
  )
