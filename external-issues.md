# External issues

Sharp edges, probable bugs and surprising behaviours met in other tools while working on VIK — sbt 2, and
the solvers the benchmarks compare against — kept here to be reported upstream (none has been reported yet).

## sbt 2

Versions: sbt 2.0.8 (`project/build.properties`), sbt runner 2.0.2, thin client `sbtn 2.0.0-b4d628dd`,
sbt-jmh 0.4.8, Scala 3.9.0, OpenJDK 17.0.20 on Ubuntu 24.04 (Linux 6.8).

### 1. `Jmh/run; shutdown` in one command deletes the forked process's classpath

- **Observed** (2026-08-28): `sbt "egraphBench/Jmh/run; shutdown"` fails with
  `NoSuchFileException ... target/bg-jobs/.../META-INF/BenchmarkList`: the server starts shutting down, and deletes its
  `target/bg-jobs/` classpath copy, while the forked JMH process is still starting and reads its classpath from there.
- **Reproduce**: any `Jmh/run` (or presumably any forked `run` executed as a background job) chained with `shutdown` in
  the same command string.
- **Workaround**: run `shutdown` as a separate `sbt shutdown` invocation, after the run has finished.

### 2. A thin client blocks forever on a server that stopped responding

- **Observed** (2026-08-29): after a long forked run (a JMH run of about 10 minutes in a git worktree), the next client
  command (`sbt "print smtBench/runtimeClasspath"`) blocked for more than 3 hours with no output and no error; `sbt
  shutdown` blocked as well. Killing the server JVM (`java ... -Dsbt.script=/usr/bin/sbt` with that working
  directory), removing `project/target/active.json` and running the client again started a fresh server that answered
  in seconds.
- **Reproduce**: not reliably; it happened once out of several long runs in worktrees, and once more a client hung on
  a server left behind by an earlier killed client (issue 3).
- **Workaround**: wrap client calls in `timeout`; `sbt shutdown` between long runs; if a client hangs, kill the server
  JVM (check its `/proc/<pid>/cwd`) and delete `project/target/active.json`.
- **Related**: killed or stuck clients leave servers behind. At one point three server JVMs had the repository as
  working directory, plus one for a worktree that had been removed (its `cwd` shown as `(deleted)`).

### 3. Killing the client does not stop the command: forked processes keep running

- **Observed** (2026-08-29): after killing an `sbtn` client during `smtBench/Jmh/run`, the forked JMH host
  (`org.openjdk.jmh.Main`) and its measurement forks kept running on the server side, unattended; the JMH host still
  held JMH's machine-wide `/tmp/jmh.lock`, so the next `Jmh/run` failed with `Unable to acquire the JMH lock` until the
  orphaned JVMs were killed by hand.
- **Reproduce**: `sbt "smtBench/Jmh/run"`, kill the `sbtn` process (Ctrl-C from a terminal behaves the same).
- **Workaround**: `pkill` the `org.openjdk.jmh.Main` and `ForkedMain` JVMs of the user before the next run.

### 4. Classpath tasks print virtual references

- **Observed** (2026-08-28): `sbt "export smtBench/Runtime/fullClasspath"` and `print` of classpath keys give entries
  such as `${OUT}/jvm/scala-3.9.0/smt-bench/...` and `${CSR_CACHE}/...` instead of paths, which scripts cannot use
  (`java -cp`). The `${CSR_CACHE}` entries also vary between machines.
- **Workaround**: a task that converts through `fileConverter` (`build.sbt`, `runtimeClasspath`):
  `(Runtime / fullClasspath).value.map(entry => fileConverter.value.toPath(entry.data).toString)`.
- **Also**: `sbt -error "print key"` prints nothing; `print` seems to go through the logger at info level, so the
  script reads `sbt "print key" 2>/dev/null | grep -E "^/"` instead.

### 5. Cached test runs report `Passed: Total 0`

- **Observed** (2026-08-28): with nothing changed since the last run, `sbt test` reports `Passed: Total 0, Failed 0`
  and runs nothing, because task results are cached by content; `clean` does not reset that cache. Reading a green
  `Total 0` as "tests ran" is an easy mistake, and CI logs show it too.
- **Workaround**: `sbt "project/testOnly *"` forces a run. Documented in the README; surprising rather than wrong.
- **Also** (2026-08-31): the cache keys on the classpath and the sources, not on data a test reads from disk, so
  a run after a change to the corpus alone could in principle be replayed. Not observed here in the end — what
  looked like a stale replay was this summary's `Total` excluding the ignored tests — but the corpus is stamped
  into a managed test resource anyway, so the classpath changes with it and the question does not arise.

### 6. `target/` holds symlinks into the user's cache

- **Observed** (2026-08-29): the incremental compilation state under `target/out/.../zinc/inc_compile_3.zip` is a
  symlink into the content-addressed store `~/.cache/sbt/v2/cas/...`. After running sbt as root in a container with
  the repository bind-mounted, the host's `target/` held root-owned symlinks into `/root/.cache/...`: every later
  build on the host failed with `AccessDeniedException` on those (dangling) links until they were deleted.
- **Reproduce**: run sbt in a container as root on a bind-mounted checkout, then on the host.
- **Workaround**: delete the links (`find target -type l -lname '/root/*' -delete`) and the `zinc` directories; run
  containers as the host user.

### 7. A malformed `build.sbt` is reported as a `MatchError` on the whole tree

- **Observed** (2026-08-29): a stray `  )` line left in `build.sbt` by an edit made the server fail to load the
  build with `scala.MatchError: PackageDef(Ident(<empty>),List(ModuleDef(...` followed by the full tree of the file
  (thousands of characters), then `failed to connect to server`; no line number, no "expected ... but found".
- **Reproduce**: add a line containing only `)` between two top-level definitions of `build.sbt`.
- **Workaround**: none needed once found; `git diff build.sbt` is the fastest way to find it.

### 8. Small things

- `sbt "cmd1; cmd2"` stops at the first failing command, as in sbt 1, but with the thin client the exit code of the
  whole call is that of the last command started; a script has to check each command separately.
- Long `-p file=a,b,c` arguments to `Jmh/run` work, but a benchmark that throws during setup (a missing resource) is
  reported only inside the JMH log as `<failure>`; the command itself succeeds with exit code 0.

### Several command arguments are joined into one command (2026-08-30)

`sbt "bench/Jmh/run -f 1 -e .*A.*" "bench/Jmh/run -f 1 -p instance=x .*B.*"` ran *one* command,
`bench/Jmh/run -f 1 -e .*A.* bench/Jmh/run -f 1 -p instance=x .*B.*` (JMH: "Found multiple arguments for
option i"); sbt 1 ran the two commands in sequence. Workaround: one argument with `;` between the commands,
`sbt "cmd1; cmd2"`.

### The client fails to connect on a fresh CI runner (2026-08-30)

Once on GitHub Actions, the first `sbt` invocation of a job died with `sbt connection file
project/target/active.json is corrupt or unreadable: ... IncompleteParseException: exhausted input` before running
any command: the thin client read the server's connection file while it was being written. A re-run passed.

## Scaladoc

Version: the Scaladoc of Scala 3.10.0-RC1, through sbt-unidoc.

### An enum case's parameter accessor is not in its members list

- **Observed** (2026-08-31): for `enum SExpr { case Atom(text: String) ... }`, the page of `Atom` shows
  `text` only inside the signature — the members list has no entry for the public accessor `text`, so its
  type is unclickable and it cannot carry a doc comment the reader can find. The same for every case of a
  documented enum (`Command.Assert#formula`, ...). The parameter names in the signature are also emitted
  with `data-unresolved-link`, so they do not link to anything.
- **Reproduce**: any Scala 3 enum with parameterized cases; open the generated page of a case.

## SMTInterpol

Version: `de.uni-freiburg.informatik.ultimate:smtinterpol:2.5-916-ga5843d8b` (Maven).

### `sat` after failing to read the assertions

- **Observed** (2026-08-29, the compare run; reproduced 2026-08-31): on the QF_BV files of the corpus, every
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

### `unknown` with "Stack overflow, giving up" on an unsat QF_LIA instance

- **Observed** (2026-08-29, the compare run at 60 s; reproduced 2026-08-31): `ap.CmdlMain
  smtlib/QF_LIA/smt-lib/cut_lemmas__20-vars__cut_lemma_01_001.smt2` prints `Stack overflow, giving up` and
  answers `unknown` after a few seconds; the file's status is `unsat`, and z3, cvc5 and VIK answer it.
  Default JVM stack size; a larger `-Xss` was not tried.

## sbt 2: the cached test task replays although a generated resource changed

`smt2utils / Test` is cache-keyed through a managed resource, `corpus-stamp.tsv`, an
uncached listing (path, size, mtime) of every file under `smtlib/` and `docs/_docs` —
the documented workaround for the classpath-only cache key. With sbt 2.0.8, adding 80
files under `smtlib/` (nothing else changed) still replays `smt2utils/test` from the
cache: `Passed: Total 0`, twice, including right after `smt2utils/clean`. The stamp
regenerates with the new content, so the resource's hash should differ; the replay
suggests the cache key does not see it. `testOnly` is not cached and runs the tests.
Not minimized or reported yet.
