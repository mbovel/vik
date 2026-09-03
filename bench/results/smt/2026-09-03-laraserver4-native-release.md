# Scala Native in release mode, 2026-09-03, laraserver4

Scala Native links in `debug` mode unless `nativeConfig` says otherwise — no optimizer, no link-time
optimization — and the build said nothing, so every Scala Native number of the evaluation up to this day was a
debug binary. The `vik` row's main binary now links with `Mode.releaseFull` and `LTO.thin` (build.sbt,
`nativeRelease`); the test binaries stay in debug mode. Linking takes 28 s here (10 s of optimizer, 11 s of
thin LTO) against 3 s for debug.

The two binaries on six instances, three runs each, wall clock, laraserver4 (AMD EPYC 9254, shared),
Scala Native 0.5.12, LLVM 18.1.8 (`-stdlib=libc++ -fuse-ld=lld`, the user-space toolchain's needs):

| Instance | Debug (s) | Release (s) | Debug / release |
|---|---:|---:|---:|
| `NEQ__NEQ004_size4` | 1.70 | 0.33 | 5.2× |
| `dillig__20-4` | 0.072 | 0.025 | 2.9× |
| `wisas__xs_8_8` | 0.31 | 0.071 | 4.3× |
| `sage__app2__bench_512` | 1.27 | 0.40 | 3.2× |
| `alpha-093dddceea58` | 0.014 | 0.007 | 2.0× |
| `20190906-CLEARSY__0016__00004` | 0.018 | 0.009 | 2.0× |

Where the search takes time, three to five times faster; where start-up is the time, twice — the release
binary starts in about 7 ms. That puts Scala Native level with the GraalVM native image
([its record](2026-09-03-laraserver4-graal.md): `NEQ004` 0.36 s, 6–30 ms elsewhere) and a step ahead of it on
`sage` (0.40 s). The cold table of the evaluation page is remeasured with the release binary from this day on.
