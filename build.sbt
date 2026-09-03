val scala3Version = "3.10.0-RC1"

// Scala Native has no compiler plugin for 3.10 yet: its rows stay on the previous release.
val nativeScalaVersion = "3.9.0"

// In sbt 2, bare settings are common settings injected into all subprojects.
organization := "net.bovel"

// Publishing to Maven Central (through the Sonatype Central Portal): the version comes from the git
// tag (sbt-dynver: pushing `v0.1.0` publishes 0.1.0), signing and upload are sbt-ci-release in the
// `publish` job of the CI — see "Releasing" in CONTRIBUTING.md.
homepage := Some(uri("https://github.com/mbovel/vik"))
licenses := List(License.Apache2)
developers := List(Developer("mbovel", "Matthieu Bovel", "matthieu@bovel.net", uri("https://github.com/mbovel")))
scmInfo := Some(ScmInfo(uri("https://github.com/mbovel/vik"), "scm:git:git@github.com:mbovel/vik.git"))
versionScheme := Some("early-semver")

// Every JVM test suite runs forked with the same fixed heap on every machine, so the sbt server
// only builds: without this, all modules' tests share the server heap and its classloader caches,
// which exhausted CI's three gigabytes (HoCoreTest OOMed after the earlier suites filled it).
val forkedTests = Seq(
  Test / fork := true,
  Test / javaOptions += "-Xmx2g",
  // The corpus test's wall-clock guard, when the environment raises it (CI: a hard limit, not the budget).
  Test / javaOptions ++= sys.env.get("VIK_CORPUS_BUDGET_MS").map(ms => s"-Dvik.corpus.budgetMs=$ms").toSeq
)
scalaVersion := scala3Version
scalacOptions += "-Yexplicit-nulls"

// Generates the subprojects `egraph` (JVM) and `egraphJS` from the same sources.
lazy val egraph = projectMatrix
  .in(file("egraph"))
  .settings(
    name := "egraph",
    description := "A congruence closure engine with explanations (an e-graph)",
    libraryDependencies += "org.scalameta" %% "munit" % "1.3.5" % Test
  )
  .jvmPlatform(scalaVersions = Seq(scala3Version), settings = forkedTests)
  .jsPlatform(scalaVersions = Seq(scala3Version))
  .nativePlatform(scalaVersions = Seq(nativeScalaVersion))

// Generates the subprojects `cdclt` (JVM) and `cdcltJS` from the same sources.
lazy val cdclt = projectMatrix
  .in(file("cdclt"))
  .settings(
    name := "cdclt",
    description := "A CDCL(T) SAT solver: watched literals, first-UIP learning, theories",
    libraryDependencies += "org.scalameta" %% "munit" % "1.3.5" % Test
  )
  .jvmPlatform(scalaVersions = Seq(scala3Version), settings = forkedTests)
  .jsPlatform(scalaVersions = Seq(scala3Version))
  .nativePlatform(scalaVersions = Seq(nativeScalaVersion))

// S-expression parser: subprojects `sexpr` (JVM) and `sexprJS`.
lazy val sexpr = projectMatrix
  .in(file("sexpr"))
  .settings(
    name := "sexpr",
    description := "An S-expression parser and printer",
    libraryDependencies += "org.scalameta" %% "munit" % "1.3.5" % Test
  )
  .jvmPlatform(scalaVersions = Seq(scala3Version), settings = forkedTests)
  .jsPlatform(scalaVersions = Seq(scala3Version))
  .nativePlatform(scalaVersions = Seq(nativeScalaVersion))

// Linear integer arithmetic (simplex and branch and bound): subprojects `lia` (JVM) and `liaJS`.
lazy val lia = projectMatrix
  .in(file("lia"))
  .settings(
    name := "lia",
    description := "Linear integer arithmetic: simplex, branch and bound in a small-model box",
    libraryDependencies += "org.scalameta" %% "munit" % "1.3.5" % Test
  )
  .jvmPlatform(scalaVersions = Seq(scala3Version), settings = forkedTests)
  .jsPlatform(scalaVersions = Seq(scala3Version))
  .nativePlatform(scalaVersions = Seq(nativeScalaVersion))

// Terms, formulas and the solver over the theories: subprojects `vik` (JVM), `vikJS` and `vikNative`.
lazy val vik = projectMatrix
  .in(file("vik"))
  .dependsOn(cdclt, egraph, lia, smtlib)
  .settings(
    name := "vik",
    description := "A minimal, readable SMT solver",
    Compile / mainClass := Some("net.bovel.vik.smt2"), // the command line, also as a native binary
    libraryDependencies += "org.scalameta" %% "munit" % "1.3.5" % Test
  )
  .jvmPlatform(scalaVersions = Seq(scala3Version), settings = forkedTests)
  .jsPlatform(scalaVersions = Seq(scala3Version))
  .nativePlatform(scalaVersions = Seq(nativeScalaVersion))

// SMT-LIB 2 scripts: subprojects `smtlib` (JVM) and `smt2JS`.
lazy val smtlib = projectMatrix
  .in(file("smtlib"))
  .dependsOn(sexpr)
  .settings(
    name := "smtlib",
    description := "SMT-LIB 2 as written: trees, parser, printer",
    libraryDependencies += "org.scalameta" %% "munit" % "1.3.5" % Test
  )
  .jvmPlatform(scalaVersions = Seq(scala3Version), settings = forkedTests)
  .jsPlatform(scalaVersions = Seq(scala3Version))
  .nativePlatform(scalaVersions = Seq(nativeScalaVersion))

lazy val vikJVM = vik.jvm(scala3Version)

// The corpus on disk and its importer, and the tests that play it (JVM only): forked with a fixed
// heap, the same memory budget on every machine.
lazy val smtlibutils = (project in file("smtlibutils"))
  .dependsOn(vikJVM, vikJVM % "test->test")
  .settings(
    name := "smtlibutils",
    publish / skip := true, // corpus tooling and tests, not a library
    libraryDependencies += "org.scalameta" %% "munit" % "1.3.5" % Test
  )
  .settings(forkedTests *)

// The corpus and the documentation pages are data the JVM tests read from disk. The test cache is keyed on
// the classpath, not on them (external-issues.md), so their stamp becomes a managed resource: a changed file
// changes the classpath, and the cache lets the tests run.
lazy val corpusStamp = taskKey[Seq[File]]("The corpus and docs listing, as a test resource (cache input)")
smtlibutils / Test / corpusStamp := Def.uncached {
  import scala.jdk.CollectionConverters.*
  val listing = new StringBuilder
  Seq("corpus", "docs/_docs").foreach { root =>
    val stream = java.nio.file.Files.walk(java.nio.file.Path.of(root))
    try {
      stream.iterator().asScala.filter(java.nio.file.Files.isRegularFile(_)).toList.sortBy(_.toString).foreach {
        p =>
          listing.append(p.toString).append('\t').append(java.nio.file.Files.size(p))
            .append('\t').append(java.nio.file.Files.getLastModifiedTime(p).toMillis).append('\n')
      }
    } finally stream.close()
  }
  val out = (smtlibutils / Test / resourceManaged).value / "corpus-stamp.tsv"
  IO.write(out, listing.toString)
  Seq(out)
}
smtlibutils / Test / resourceGenerators += (smtlibutils / Test / corpusStamp).taskValue

// The browser playgrounds (Scala.js): the e-graph and the SMT-LIB front end.
import org.scalajs.linker.interface.ESVersion

// The solver as a WebAssembly module for the runnable snippets of the documentation (see the `site` task).
lazy val docsPlayground = project
  .in(file("docsPlayground"))
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(vik.js(scala3Version))
  .settings(
    name := "docsPlayground",
    publish / skip := true,
    scalaJSLinkerConfig ~= {
      _.withESFeatures(_.withUseWebAssembly(true).withESVersion(ESVersion.ES2022)).withModuleKind(ModuleKind.ESModule)
    }
  )

// The same module, on the JavaScript backend, for the engines that have no Wasm 3.0 (Safari before 26).
lazy val docsPlaygroundJs = project
  .in(file("target/docsPlayground-js"))
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(vik.js(scala3Version))
  .settings(
    name := "docsPlaygroundJs",
    publish / skip := true,
    Compile / scalaSource := (docsPlayground / Compile / scalaSource).value,
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) }
  )

lazy val playground = project
  .in(file("playground"))
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(egraph.js(scala3Version), vik.js(scala3Version))
  .settings(
    name := "playground",
    publish / skip := true,
    scalaJSUseMainModuleInitializer := true,
    // WebAssembly too (see the JS rows below): the pages load `main.js` as an ES module, which fetches `main.wasm`.
    scalaJSLinkerConfig ~= {
      _.withESFeatures(_.withUseWebAssembly(true).withESVersion(ESVersion.ES2022)).withModuleKind(ModuleKind.ESModule)
    },
    libraryDependencies += "org.scala-js" %% "scalajs-dom" % "2.8.1"
  )

// The runtime classpath of a project as real paths, for scripts (sbt 2 exports virtual file references).
lazy val runtimeClasspath = taskKey[String]("The runtime classpath, colon-separated, as real paths")

// JMH benchmarks of every module (JVM only), and the comparison with other solvers: `sbt "bench/Jmh/run"`.
lazy val bench = project
  .in(file("bench"))
  .enablePlugins(JmhPlugin)
  .dependsOn(
    smtlibutils,
    egraph.jvm(scala3Version),
    cdclt.jvm(scala3Version),
    lia.jvm(scala3Version),
    sexpr.jvm(scala3Version),
    vik.jvm(scala3Version),
    smtlib.jvm(scala3Version)
  )
  .settings(
    name := "bench",
    publish / skip := true,
    // Other solvers, run in-process by the benchmarks and as separate processes by compare.sh.
    libraryDependencies ++= Seq(
      ("io.github.uuverifiers" %% "princess" % "2026-05-20").cross(CrossVersion.for3Use2_13),
      "de.uni-freiburg.informatik.ultimate" % "smtinterpol" % "2.5-1388-ga5a4ab0c",
      // The Java bindings of z3 and cvc5 as published by SoSy-Lab (JavaSMT); their native libraries come from
      // bench/fetch-native.sh into bench/native, found through java.library.path below. The cvc5 one is a
      // snapshot of cvc5's main taken the day after the 1.3.3 tag (four housekeeping commits past it): SoSy-Lab
      // stopped publishing release builds after 1.2.1, whose binding scans a list of every object created for
      // each new term (external-issues.md).
      "org.sosy-lab" % "javasmt-solver-z3" % "4.14.0",
      "org.sosy-lab" % "javasmt-solver-cvc5" % "2026-02-26-d22638a"
    ),
    fork := true, // runs and benchmarks in their own JVM, with the native libraries of z3 and cvc5 on its path
    // The Java that sbt-jmh generates is compiled for 17 whatever JDK runs the build: a class compiled by a
    // newer one stays in target/ and a build on 17 then cannot load it (UnsupportedClassVersionError).
    javacOptions ++= Seq("--release", "17"),
    javaOptions += s"-Djava.library.path=${baseDirectory.value / "native"}",
    Jmh / run / javaOptions += s"-Djava.library.path=${baseDirectory.value / "native"}",
    runtimeClasspath := {
      val converter = fileConverter.value
      (Runtime / fullClasspath).value.map(entry => converter.toPath(entry.data).toString).mkString(":")
    }
  )

// Scala.js on the WebAssembly backend: ES modules and ES 2022 are required.
lazy val egraphJS = egraph.js(scala3Version)
lazy val cdcltJS = cdclt.js(scala3Version)
lazy val sexprJS = sexpr.js(scala3Version)
lazy val liaJS = lia.js(scala3Version)
lazy val vikJS = vik.js(scala3Version)
lazy val smt2JS = smtlib.js(scala3Version)
egraphJS / scalaJSLinkerConfig ~=
  { _.withESFeatures(_.withUseWebAssembly(true).withESVersion(ESVersion.ES2022)).withModuleKind(ModuleKind.ESModule) }
cdcltJS / scalaJSLinkerConfig ~=
  { _.withESFeatures(_.withUseWebAssembly(true).withESVersion(ESVersion.ES2022)).withModuleKind(ModuleKind.ESModule) }
sexprJS / scalaJSLinkerConfig ~=
  { _.withESFeatures(_.withUseWebAssembly(true).withESVersion(ESVersion.ES2022)).withModuleKind(ModuleKind.ESModule) }
liaJS / scalaJSLinkerConfig ~=
  { _.withESFeatures(_.withUseWebAssembly(true).withESVersion(ESVersion.ES2022)).withModuleKind(ModuleKind.ESModule) }
vikJS / scalaJSLinkerConfig ~=
  { _.withESFeatures(_.withUseWebAssembly(true).withESVersion(ESVersion.ES2022)).withModuleKind(ModuleKind.ESModule) }
smt2JS / scalaJSLinkerConfig ~=
  { _.withESFeatures(_.withUseWebAssembly(true).withESVersion(ESVersion.ES2022)).withModuleKind(ModuleKind.ESModule) }

// Scala Native: the same sources again, with a `Native` suffix (clang: on the path, or `LLVM_BIN`).

// The API documentation of the six modules (their JVM rows) as one site: `sbt unidoc`, in `target/unidoc`.
lazy val docsAssets = taskKey[Unit]("The solver as a WebAssembly module for the runnable snippets of the site")

// The documentation site: the solver linked for the snippets, then the pages of `docs/` and the API documentation.
addCommandAlias("site", "docsAssets; unidoc")

// The test suites by platform, one CI job each: the six modules on the JVM with the corpus, then the same six on
// Scala.js (Node.js, the WebAssembly backend) and on Scala Native (clang). `sbt test` at the root is the JVM rows.
addCommandAlias("testJVM", "egraph/test; cdclt/test; sexpr/test; lia/test; vik/test; smtlib/test; smtlibutils/test")
addCommandAlias("testJS", "egraphJS/test; cdcltJS/test; sexprJS/test; liaJS/test; vikJS/test; smtlibJS/test")
addCommandAlias(
  "testNative",
  "egraphNative/test; cdcltNative/test; sexprNative/test; liaNative/test; vikNative/test; smtlibNative/test"
)

lazy val root = (project in file("."))
  .enablePlugins(ScalaUnidocPlugin)
  .aggregate(
    egraph.jvm(scala3Version),
    cdclt.jvm(scala3Version),
    sexpr.jvm(scala3Version),
    lia.jvm(scala3Version),
    vik.jvm(scala3Version),
    smtlib.jvm(scala3Version)
  )
  .settings(
    name := "vik-root", // the aggregate; the solver module is `vik`
    publish / skip := true,
    ScalaUnidoc / unidoc / unidocProjectFilter := inProjects(
      egraph.jvm(scala3Version),
      cdclt.jvm(scala3Version),
      sexpr.jvm(scala3Version),
      lia.jvm(scala3Version),
      vik.jvm(scala3Version),
      smtlib.jvm(scala3Version)
    ),
    ScalaUnidoc / unidoc / target := file("target/unidoc"),
    // The solver, linked to WebAssembly into `docs/_assets/js/vik/`, where the pages of the site load it from.
    docsAssets := Def.uncached { // the linker's report has no hash for sbt's cache; the linker caches by itself
      val linked = (docsPlayground / Compile / fullLinkJS).value.data.publicModules.head
      val _ = (docsPlaygroundJs / Compile / fullLinkJS).value
      val directory = (docsPlayground / Compile / fullLinkJS / scalaJSLinkerOutputDirectory).value
      val javascript = (docsPlaygroundJs / Compile / fullLinkJS / scalaJSLinkerOutputDirectory).value
      val destination = file("docs") / "_assets" / "js"
      IO.delete(destination / "vik")
      IO.delete(destination / "vik-js")
      IO.copyDirectory(directory, destination / "vik")
      IO.copyDirectory(javascript, destination / "vik-js")
      streams.value.log.info(
        s"the solver (${linked.jsFileName}) is linked into $destination as WebAssembly and as JavaScript"
      )
    },
    ScalaUnidoc / unidoc / scalacOptions ++= Seq(
      "-project",
      "VIK",
      "-siteroot",
      "docs",
      "-Yapi-subdirectory", // the guide at the root of the site, the API documentation under `api/`
      "-project-version",
      version.value,
      "-source-links:github://mbovel/vik/main",
      "-social-links:github::https://github.com/mbovel/vik"
    )
  )
