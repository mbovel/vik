# The native test run after the store fix, 2026-09-05, laraserver4

The CI job that runs every suite as a GraalVM native image went past its ten minutes from the fix of
[issue 13](https://github.com/mbovel/vik/issues/13) on, while the JVM suites passed unchanged. The native
run of the corpus, timed locally with a line per test over a second:

| Build | Native run | Tests at the 30 s budget |
|---|---:|---:|
| before the fixes (4756b10) | 4:09 | none |
| the reals fix (75fe25d) | 4:21 | none |
| the store and Boolean fix (59f990e) | 5:59 | one |
| every fix (d60d4e8) | 5:48 | three |
| with the lemma loop bounded and the file skipped | 3:51 | none |

The solver was not slower: the native binary answers the files the harness reported slow in the same time
as before (`Sydr…cjpeg__predicate_150`, 2.6 s). One file was. The `CPAchecker…dwc3-pci` script declares
its first `store` after its first `check-sat`; with the store test memoized at the first solve it got no
array axioms and failed in a second, which its `timeout` line accepted, and with the test fixed its array
lemmas run past the fuel, as its two siblings' `skip` lines say. It ran to the budget, the harness gave
up on it, and the lemma loop ran on in that thread: under the native image's stop-the-world collector
the tests that followed ran two to seven times slower until it ended, two of them past the budget in
turn, each leaving one more thread behind.

Two changes. The lemma loop of `Solver.solve` asks `keepGoing` at every round, so a check-sat past its
deadline ends within a round instead of running on; and the file is a `skip` line like its siblings. The
store test itself is a flag set at declaration, no scan. The JVM corpus run went from about 150 s to
117 s with the same change: the lingering thread was costing there too.

**Kept.**
