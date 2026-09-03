# The Scala 3.9 bytecode optimizer (`-opt`), cold, 2026-09-03, laraserver4

Scala 3.9 carries the backend optimizer ported from Scala 2 (`-opt`: the local optimizations — box
elimination, copy propagation, closure rewriting, nullness — `-Yopt-specific:help` lists them). Tried on the
JVM rows (`scalacOptions += "-opt"`), the GraalVM image and the AOT cache rebuilt from that bytecode, then
`bench/evaluate.sh cold` against the run of the same day without it (`854e859`): the evaluation's 42 instances,
three repetitions, the median, the ten-second budget, laraserver4 (shared).

**Result: no effect. Not kept.** Every column is within one to three percent of the run before, which is
the noise of the machine. Cold, the JVM's time is start-up and the JIT, which the bytecode does not change;
the image compiles the bytecode itself and saw nothing either; Scala Native does not read the flag. Warm, the
JIT does these optimizations by itself, so nothing is expected there either.

| Side | Before (median) | With `-opt` (median) | Geometric mean before / after (41 instances) |
|---|---:|---:|---:|
| VIK (JVM) | 0.322 s | 0.322 s | 1.00 |
| VIK (JVM AOT) | 0.161 s | 0.162 s | 1.00 |
| VIK (Native Image) | 0.012 s | 0.011 s | 0.98 |
| VIK (native) | 0.012 s | 0.013 s | 0.97 |
| VIK (Node) | 0.103 s | 0.099 s | 1.01 |
| z3 | 0.017 s | 0.017 s | 0.99 |
| cvc5 | 0.011 s | 0.011 s | 1.00 |

The rows where the search takes time, JVM / JVM AOT / Native Image, before and with `-opt`:

| Instance | JVM | JVM AOT | Native Image |
|---|---:|---:|---:|
| `NEQ__NEQ004_size4` | 0.627 → 0.614 | 0.518 → 0.533 | 0.365 → 0.354 |
| `sage__app2__bench_512` | 0.491 → 0.482 | 0.337 → 0.333 | 0.159 → 0.159 |
| `wisas__xs_8_8` | 0.494 → 0.487 | 0.284 → 0.303 | 0.088 → 0.089 |
| `dillig__20-4` | 0.322 → 0.325 | 0.168 → 0.162 | 0.024 → 0.025 |
| `alpha-fib_memoized-36bd61c61979` | 0.344 → 0.341 | 0.166 → 0.169 | 0.019 → 0.018 |

The raw table of the `-opt` run was not kept (it overwrote the day's cold file and the committed one was
restored); the numbers above are its per-side medians and the five rows read off it.
