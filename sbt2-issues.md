# sbt 2 issues met in this repository

Sharp edges and probable bugs of sbt 2 met while working on VIK, kept here to be reported later (none has been
reported yet). Versions: sbt 2.0.8 (`project/build.properties`), sbt runner 2.0.2, thin client `sbtn 2.0.0-b4d628dd`,
sbt-jmh 0.4.8, Scala 3.9.0, OpenJDK 17.0.20 on Ubuntu 24.04 (Linux 6.8).

## 1. `Jmh/run; shutdown` in one command deletes the forked process's classpath

- **Observed** (2026-08-28): `sbt "egraphBench/Jmh/run; shutdown"` fails with
  `NoSuchFileException ... target/bg-jobs/.../META-INF/BenchmarkList`: the server starts shutting down, and deletes its
  `target/bg-jobs/` classpath copy, while the forked JMH process is still starting and reads its classpath from there.
- **Reproduce**: any `Jmh/run` (or presumably any forked `run` executed as a background job) chained with `shutdown` in
  the same command string.
- **Workaround**: run `shutdown` as a separate `sbt shutdown` invocation, after the run has finished.

## 2. A thin client blocks forever on a server that stopped responding

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

## 3. Killing the client does not stop the command: forked processes keep running

- **Observed** (2026-08-29): after killing an `sbtn` client during `smtBench/Jmh/run`, the forked JMH host
  (`org.openjdk.jmh.Main`) and its measurement forks kept running on the server side, unattended; the JMH host still
  held JMH's machine-wide `/tmp/jmh.lock`, so the next `Jmh/run` failed with `Unable to acquire the JMH lock` until the
  orphaned JVMs were killed by hand.
- **Reproduce**: `sbt "smtBench/Jmh/run"`, kill the `sbtn` process (Ctrl-C from a terminal behaves the same).
- **Workaround**: `pkill` the `org.openjdk.jmh.Main` and `ForkedMain` JVMs of the user before the next run.

## 4. Classpath tasks print virtual references

- **Observed** (2026-08-28): `sbt "export smtBench/Runtime/fullClasspath"` and `print` of classpath keys give entries
  such as `${OUT}/jvm/scala-3.9.0/smt-bench/...` and `${CSR_CACHE}/...` instead of paths, which scripts cannot use
  (`java -cp`). The `${CSR_CACHE}` entries also vary between machines.
- **Workaround**: a task that converts through `fileConverter` (`build.sbt`, `runtimeClasspath`):
  `(Runtime / fullClasspath).value.map(entry => fileConverter.value.toPath(entry.data).toString)`.
- **Also**: `sbt -error "print key"` prints nothing; `print` seems to go through the logger at info level, so the
  script reads `sbt "print key" 2>/dev/null | grep -E "^/"` instead.

## 5. Cached test runs report `Passed: Total 0`

- **Observed** (2026-08-28): with nothing changed since the last run, `sbt test` reports `Passed: Total 0, Failed 0`
  and runs nothing, because task results are cached by content; `clean` does not reset that cache. Reading a green
  `Total 0` as "tests ran" is an easy mistake, and CI logs show it too.
- **Workaround**: `sbt "project/testOnly *"` forces a run. Documented in the README; surprising rather than wrong.

## 6. `target/` holds symlinks into the user's cache

- **Observed** (2026-08-29): the incremental compilation state under `target/out/.../zinc/inc_compile_3.zip` is a
  symlink into the content-addressed store `~/.cache/sbt/v2/cas/...`. After running sbt as root in a container with
  the repository bind-mounted, the host's `target/` held root-owned symlinks into `/root/.cache/...`: every later
  build on the host failed with `AccessDeniedException` on those (dangling) links until they were deleted.
- **Reproduce**: run sbt in a container as root on a bind-mounted checkout, then on the host.
- **Workaround**: delete the links (`find target -type l -lname '/root/*' -delete`) and the `zinc` directories; run
  containers as the host user.

## 7. A malformed `build.sbt` is reported as a `MatchError` on the whole tree

- **Observed** (2026-08-29): a stray `  )` line left in `build.sbt` by an edit made the server fail to load the
  build with `scala.MatchError: PackageDef(Ident(<empty>),List(ModuleDef(...` followed by the full tree of the file
  (thousands of characters), then `failed to connect to server`; no line number, no "expected ... but found".
- **Reproduce**: add a line containing only `)` between two top-level definitions of `build.sbt`.
- **Workaround**: none needed once found; `git diff build.sbt` is the fastest way to find it.

## 8. Small things

- `sbt "cmd1; cmd2"` stops at the first failing command, as in sbt 1, but with the thin client the exit code of the
  whole call is that of the last command started; a script has to check each command separately.
- Long `-p file=a,b,c` arguments to `Jmh/run` work, but a benchmark that throws during setup (a missing resource) is
  reported only inside the JMH log as `<failure>`; the command itself succeeds with exit code 0.

## Several command arguments are joined into one command (2026-08-30)

`sbt "bench/Jmh/run -f 1 -e .*A.*" "bench/Jmh/run -f 1 -p instance=x .*B.*"` ran *one* command,
`bench/Jmh/run -f 1 -e .*A.* bench/Jmh/run -f 1 -p instance=x .*B.*` (JMH: "Found multiple arguments for
option i"); sbt 1 ran the two commands in sequence. Workaround: one argument with `;` between the commands,
`sbt "cmd1; cmd2"`.

## The client fails to connect on a fresh CI runner (2026-08-30)

Once on GitHub Actions, the first `sbt` invocation of a job died with `sbt connection file
project/target/active.json is corrupt or unreadable: ... IncompleteParseException: exhausted input` before running
any command: the thin client read the server's connection file while it was being written. A re-run passed.
