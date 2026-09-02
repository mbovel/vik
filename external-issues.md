# External issues

Sharp edges, probable bugs and surprising behaviours met in other tools while working on VIK — sbt 2, and
the solvers the benchmarks compare against. None has been reported upstream yet; entries marked as
behaviours rather than bugs are kept for understanding, not reporting.

## sbt 2

Versions: sbt 2.0.8 (`project/build.properties`), sbt runner 2.0.2, thin client `sbtn 2.0.0-b4d628dd`,
sbt-jmh 0.4.8, Scala 3.9.0, OpenJDK 17.0.20 on Ubuntu 24.04 (Linux 6.8).

### 1. `Jmh/run; shutdown` in one command deletes the forked process's classpath (2026-08-28)

- **Observed**: `sbt "egraphBench/Jmh/run; shutdown"` fails with
  `NoSuchFileException ... target/bg-jobs/.../META-INF/BenchmarkList`: the server starts shutting down, and deletes its
  `target/bg-jobs/` classpath copy, while the forked JMH process is still starting and reads its classpath from there.
- **Reproduce**: any `Jmh/run` (or presumably any forked `run` executed as a background job) chained with `shutdown` in
  the same command string.
- **Workaround**: run `shutdown` as a separate `sbt shutdown` invocation, after the run has finished.

### 2. A thin client blocks forever on a server that stopped responding (2026-08-29)

- **Observed**: after a long forked run (a JMH run of about 10 minutes in a git worktree), the next client
  command (`sbt "print smtBench/runtimeClasspath"`) blocked for more than 3 hours with no output and no error; `sbt
  shutdown` blocked as well. Killing the server JVM, removing `project/target/active.json` and running the client again
  started a fresh server that answered in seconds.
- **Reproduce**: not reliably; once out of several long runs in worktrees, and once more on a server left behind by an
  earlier killed client (issue 3).
- **Workaround**: wrap client calls in `timeout`; `sbt shutdown` between long runs; if a client hangs, kill the server
  JVM (check its `/proc/<pid>/cwd`) and delete `project/target/active.json`. See issue 11 for the full cleanup.

### 3. Killing the client does not stop the command: forked processes keep running (2026-08-29)

- **Observed**: after killing an `sbtn` client during `smtBench/Jmh/run`, the forked JMH host
  (`org.openjdk.jmh.Main`) and its measurement forks kept running on the server side, unattended; the JMH host still
  held JMH's machine-wide `/tmp/jmh.lock`, so the next `Jmh/run` failed with `Unable to acquire the JMH lock` until the
  orphaned JVMs were killed by hand.
- **Reproduce**: `sbt "smtBench/Jmh/run"`, kill the `sbtn` process (Ctrl-C from a terminal behaves the same).
- **Workaround**: `pkill` the `org.openjdk.jmh.Main` and `ForkedMain` JVMs of the user before the next run.

### 4. Classpath tasks print virtual references (2026-08-28)

- **Observed**: `sbt "export smtBench/Runtime/fullClasspath"` and `print` of classpath keys give entries
  such as `${OUT}/jvm/scala-3.9.0/smt-bench/...` and `${CSR_CACHE}/...` instead of paths, which scripts cannot use
  (`java -cp`). The `${CSR_CACHE}` entries also vary between machines.
- **Workaround**: a task that converts through `fileConverter` (`build.sbt`, `runtimeClasspath`):
  `(Runtime / fullClasspath).value.map(entry => fileConverter.value.toPath(entry.data).toString)`.
- **Also**: `sbt -error "print key"` prints nothing; `print` seems to go through the logger at info level, so scripts
  read `sbt "print key" 2>/dev/null | grep -E "^/"` — and on a CI runner the value line arrives prefixed with ANSI
  clear-line escapes, so the escapes must be stripped before the grep (2026-09-01).

### 5. Cached test runs report `Passed: Total 0` (2026-08-28, 2026-08-31)

- **Observed**: with nothing changed since the last run, `sbt test` reports `Passed: Total 0, Failed 0`
  and runs nothing, because task results are cached by content; `clean` does not reset that cache. Reading a green
  `Total 0` as "tests ran" is an easy mistake, and CI logs show it too.
- **Workaround**: `sbt "project/testOnly *"` forces a run. Documented in the README; surprising rather than wrong.
- **Also**: the cache keys on the classpath and the sources, not on data a test reads from disk, so
  a run after a change to the corpus alone could in principle be replayed. Not observed here in the end — what
  looked like a stale replay was this summary's `Total` excluding the ignored tests — but the corpus is stamped
  into a managed test resource anyway, so the classpath changes with it and the question does not arise.

### 6. `target/` holds symlinks into the user's cache (2026-08-29)

- **Observed**: the incremental compilation state under `target/out/.../zinc/inc_compile_3.zip` is a
  symlink into the content-addressed store `~/.cache/sbt/v2/cas/...`. After running sbt as root in a container with
  the repository bind-mounted, the host's `target/` held root-owned symlinks into `/root/.cache/...`: every later
  build on the host failed with `AccessDeniedException` on those (dangling) links until they were deleted.
- **Reproduce**: run sbt in a container as root on a bind-mounted checkout, then on the host.
- **Workaround**: delete the links (`find target -type l -lname '/root/*' -delete`) and the `zinc` directories; run
  containers as the host user.

### 7. A malformed `build.sbt` is reported as a `MatchError` on the whole tree (2026-08-29)

- **Observed**: a stray `  )` line left in `build.sbt` by an edit made the server fail to load the
  build with `scala.MatchError: PackageDef(Ident(<empty>),List(ModuleDef(...` followed by the full tree of the file
  (thousands of characters), then `failed to connect to server`; no line number, no "expected ... but found".
- **Reproduce**: add a line containing only `)` between two top-level definitions of `build.sbt`.
- **Workaround**: none needed once found; `git diff build.sbt` is the fastest way to find it.

### 8. Several command arguments are joined into one command (2026-08-30)

- **Observed**: `sbt "bench/Jmh/run -f 1 -e .*A.*" "bench/Jmh/run -f 1 -p instance=x .*B.*"` ran *one* command,
  `bench/Jmh/run -f 1 -e .*A.* bench/Jmh/run -f 1 -p instance=x .*B.*` (JMH: "Found multiple arguments for
  option i"); sbt 1 ran the two commands in sequence.
- **Workaround**: one argument with `;` between the commands, `sbt "cmd1; cmd2"`.

### 9. The client fails to connect on a fresh CI runner (2026-08-30)

- **Observed**: once on GitHub Actions, the first `sbt` invocation of a job died with `sbt connection file
  project/target/active.json is corrupt or unreadable: ... IncompleteParseException: exhausted input` before running
  any command: the thin client read the server's connection file while it was being written. A re-run passed.

### 10. The cached test task replays although a generated resource changed (2026-08-31, sbt 2.0.8)

- **Observed**: `smtlibutils / Test` is cache-keyed through a managed resource, `corpus-stamp.tsv`, an
  uncached listing (path, size, mtime) of every file under `corpus/` and `docs/_docs` —
  the documented workaround for the classpath-only cache key. Adding 80
  files under `corpus/` (nothing else changed) still replays `smtlibutils/test` from the
  cache: `Passed: Total 0`, twice, including right after `smtlibutils/clean`. The stamp
  regenerates with the new content, so the resource's hash should differ; the replay
  suggests the cache key does not see it. `testOnly` is not cached and runs the tests. Not minimized yet.

### 11. Killed servers and clients leave state that silently wedges every later invocation (2026-08-29 – 2026-09-01)

One robustness gap with five faces, all observed after `kill`/`kill -9` of servers, clients or test
forks — each face hangs later commands with no error and a different amount of log:

- **Stale `project/target/active.json`.** A killed server never removes its descriptor; the next
  client connects to the dead socket URI and hangs *before booting anything* — a zero-byte log is
  the signature. The client never falls back to starting a fresh server. Remedy: delete the file.
- **Stale sockets in `/run/user/$UID/.sbt-fork-ipc/`.** After killing a forked test JVM, later
  forked test runs hang right after "set current project": no compile output, no fork spawned,
  `timeout` expires on an empty run. Unforked and cache-replayed tasks still work, which disguises
  it. Remedy: delete the `*.sock` files.
- **An orphaned command keeps the queue.** When a client is killed mid-command (a shell timeout, a
  tool cap), the server keeps executing the abandoned command; every later client prints
  `[info] waiting for: <that command>` and produces nothing. If the abandoned command's fork is
  then killed, the server waits forever on the half-closed worker socket. Remedy: restart the
  server.
- **The client outlives TERM.** `timeout <n> sbt ...` can fail to end the invocation: the client
  ignores TERM while blocked on the server, so `timeout` without `--kill-after` waits forever.
  Remedy: `timeout -k 30 <n> sbt ...`.
- **The unkilled server itself** (2026-09-01). The server is a plain `java -Divy.home=…
  -Dsbt.io.virtual=true …` process, which process patterns like `xsbt.boot.Boot` or `sbt-server`
  do not match: a cleanup that kills clients, forks, sockets and `active.json` but misses the
  server leaves the orphaned queue alive, and every "fresh" client reattaches to the wedge. Two
  such servers (48 and 31 minutes old) were found alive side by side after two wedged benchmark
  runs. Remedy: kill by `sbt.io.virtual`, then remove the sockets and `active.json`.

The reliable cleanup after any kill is all of it at once: kill the forks and the server (by
`sbt.io.virtual`), remove the fork sockets, remove `active.json`. A client that timed out
connecting to a dead descriptor and booted a fresh server would remove the worst face outright.

### 12. The server is a shared heap for unforked tests (2026-09-01; runs 33470652739, 33473080564, 33477637073)

A mechanism, not a bug. With `Test / fork := false` (the default), every module's suites execute
inside the sbt server JVM, whose heap also carries the build state, the Scala.js linker and the
layered-classloader cache. On a CI runner with the launcher's default `-Xmx1G` (and still at 3G),
our seven-module `sbt test` exhausted the server heap before the forked corpus suite even started:
over 100% of CPU seconds went to GC pauses, and whichever thread allocated next threw — once
`classloader-cache-cleanup-0`, once a network-channel thread, once a 15-second suite that then
reported as "4555 s, OutOfMemoryError". Not an sbt 2 regression: the classloader cache is inherited
from sbt 1 nearly verbatim — same `SoftReference` values, same `classloader-cache-cleanup-$id`
thread (`main-command/src/main/scala/sbt/internal/classpath/ClassLoaderCache.scala`, branches
`develop` and `1.13.x`). SoftReferences shrink before an OOM, so the heap was dominated by
everything else the server hosts; the cleanup thread was just holding the bag. The fix that
matters: fork every JVM test suite with a fixed heap (`Test / fork := true`, `-Xmx2g` here),
leaving the server to build only.

### 13. Small things (2026-08-28 – 2026-08-30)

- `sbt "cmd1; cmd2"` stops at the first failing command, as in sbt 1, but with the thin client the exit code of the
  whole call is that of the last command started; a script has to check each command separately.
- Long `-p file=a,b,c` arguments to `Jmh/run` work, but a benchmark that throws during setup (a missing resource) is
  reported only inside the JMH log as `<failure>`; the command itself succeeds with exit code 0.

### 14. After a completed invocation, the next client can exit at once with empty stdout (2026-09-01, runs 33516155117, 33519232381)

- **Observed**: on GitHub Actions, a job step ran a long `sbt` command chain to completion; the next step's
  `sbt "print bench/runtimeClasspath"` exited within about 1.5 seconds with empty stdout, on three of four
  runs (the fourth passed unchanged). No process was killed anywhere — this is the healthy path. The
  client's stderr went to `/dev/null` in the script, so the underlying error was never captured; the
  symptom is indistinguishable from a successful print of nothing.
- **Reading**: the thin client of the new step races the previous invocation's background server — its
  teardown, or a descriptor in transition (issue 9 is the same family on first boot, issue 11 the
  kill-induced one) — and gives up silently instead of starting a fresh server.
- **Remedy**: no thin client in scripts and CI — `sbt --server` runs the build in the foreground process,
  which exits with the step: nothing to race, nothing left behind. (A foreground run coexists with a live
  background server; interactive use keeps the client.)

## Scaladoc

Version: the Scaladoc of Scala 3.10.0-RC1, through sbt-unidoc.

### 1. An enum case's parameter accessor is not in its members list (2026-08-31)

- **Observed**: for `enum SExpr { case Atom(text: String) ... }`, the page of `Atom` shows
  `text` only inside the signature — the members list has no entry for the public accessor `text`, so its
  type is unclickable and it cannot carry a doc comment the reader can find. The same for every case of a
  documented enum (`Command.Assert#formula`, ...). The parameter names in the signature are also emitted
  with `data-unresolved-link`, so they do not link to anything.
- **Reproduce**: any Scala 3 enum with parameterized cases; open the generated page of a case.

### 2. The content table style pads every element inside the table (2026-09-01)

- **Observed**: the theme's `bundle.css` has
  `#content table * { padding: calc(2 * var(--base-spacing)) var(--base-spacing) 17px; }` — every
  descendant of a table gets cell padding: rows, links, emphasis, spans, all inflating the cells.
  Surely meant for `td` and `th` only.
- **Reproduce**: any documentation page with a markdown table whose cells hold links or emphasis;
  inspect an element inside a cell.
- **Workaround**: `docs/_assets/css/site.css` zeroes it with a higher-specificity selector
  (`#content table :not(td, th)`) and pads only the cells — the page's own stylesheets load before the
  theme's, so an override must win by specificity, not order.

## SMTInterpol

Version: `de.uni-freiburg.informatik.ultimate:smtinterpol:2.5-916-ga5843d8b` (Maven).

### 1. `sat` after failing to read the assertions (2026-08-29, 2026-08-31)

- **Observed**: on the QF_BV files of the corpus, every
  `declare-fun` and `assert` that mentions a bit-vector sort fails with `(error "Unsupported internal sort:
  (_ BitVec 8)")` — SMTInterpol has no bit-vector theory — and the `(check-sat)` that follows answers `sat`
  on files whose status is `unsat`: the assertions were never made, so the answer is about an empty problem.
  With `-q` the errors are not printed and the output is just `sat`. Recorded on six corpus files
  (`crafted__bv8`, `2018-Goel-hwbench__QF_BV_cav14_example_cc_reg_max`,
  `20190311-bv-term-small-rw-Noetzli__bv-term-small-rw_17`, `bruttomesso__core__ext_con_004_001_0016`,
  `fft__Sz128_2823`, `pspace__ndist.a.20000`, see `bench/results/smt/compare-raw-bv.tsv`); the in-process
  `ParseEnvironment` behaves the same.
- **Consequence here**: the bold `sat` in the cold table of the evaluation page; the warm table leaves the
  instance out.
- Arguably permitted by SMT-LIB (a command that fails is skipped, and `check-sat` then speaks of what was
  asserted), but as a default it turns "unsupported" into what reads as a wrong answer.

## princess

Version: `io.github.uuverifiers:princess:2026-05-20` (Maven, for3Use2_13).

### 1. `unknown` with "Stack overflow, giving up" on an unsat QF_LIA instance (2026-08-29, 2026-09-01)

A behaviour, not a bug: an incomplete answer honestly labeled. `ap.CmdlMain
corpus/QF_LIA/smt-lib/cut_lemmas__20-vars__cut_lemma_01_001.smt2` prints `Stack overflow, giving up` and
answers `unknown` after a few seconds on the default JVM stack; the file's status is `unsat`, and z3, cvc5
and VIK answer it. With `-Xss512m` (2026-09-01) the overflow disappears but princess still does not answer:
it sits in "Constructing countermodel ..." past 2 minutes of wall time (9.5 CPU-minutes) before being
killed. The overflow is the messenger, not the illness — the instance is simply outside what princess
decides in reasonable time, and `unknown` is a legitimate answer. At most a usability nit: the default
stack turns "slow" into "gives up in seconds".

### 2. The script executor rejects re-declarations after pop (2026-08, master)

`SimpleAPI.execSMTLIB` (and the CLI, which runs the same `SMTParser2InputAbsy`) fails on
incremental scripts that declare a symbol, pop the scope, and declare it again in a sibling
scope — `20190906-CLEARSY__0020__00009.smt2` of SMT-LIB incremental stops with
`EnvironmentException: Symbol lh_1 is already declared`. The parser's `push` saves function
definitions, datatypes and partition state and `prover.push` scopes the prover's symbols, but
the parser's own symbol environment is not part of the saved tuple, so declarations survive
`pop` there.

### 3. The API replay pays 39x the CLI on an incremental QF_UFLIA instance (2026-09-01)

On `kind__MESI_i4.ec.smt2` (SMT-LIB incremental, 5 check-sats), `ap.CmdlMain +incremental` answers the
whole file in 2.5 s, while the same script replayed through `SimpleAPI` (our benchmark harness, a
compile-once instruction list) costs about 96 s per replay — 39x, reproducibly, and far out of line
with the same replay on other instances. Not diagnosed further; the benchmark leaves the combination
out rather than print a number that measures the replay path instead of the solver.
