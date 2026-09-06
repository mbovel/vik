# Declarations scoped by their level, 2026-09-06, laraserver4

The second audit's [issue 40](https://github.com/mbovel/vik/issues/40): a declaration, a definition or a
datatype made inside a level survived the level's `pop`, and a name declared again at another sort kept
the old symbol's sort, simplex variable and class in the encoder — wrong answers in both directions. The
parser, the lowerer and the session now undo a level's declarations at its `pop` (unless
`:global-declarations` is true), and the solver rebuilds its encoder when a name it knows comes back at
another sort; a name that comes back at the same sort costs nothing, which is what the incremental
corpus files do (the CLEARSY family re-defines a macro per level).

Cold, VIK on the JVM, the 44 evaluation instances, the median of three runs: 29.01 s before, 29.03 s
after, no instance past the noise. The undo lists are a closure per declaration inside a level, which
the evaluation's incremental instances hardly have.

**Kept.**
