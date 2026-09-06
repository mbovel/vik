# The core's availability and `exit`, 2026-09-06, laraserver4

Two findings of the second audit ([issue 51](https://github.com/mbovel/vik/issues/51),
[issue 52](https://github.com/mbovel/vik/issues/52)): `get-unsat-core` printed a stale core after a
`reset-assertions`, a `pop`, a new assertion; the commands after `(exit)` were played. The session now
leaves unsat mode at any change of the problem, as the standard says, and the parser stops reading at
`exit`. Every core of the corpus judged by z3 (`CoreCheck`): 19, none wrong.

Cold, VIK on the JVM, the 44 evaluation instances, the median of three runs: 29.05 s before, 29.06 s
after.

**Kept.**
