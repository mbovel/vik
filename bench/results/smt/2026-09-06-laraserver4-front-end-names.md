# The front end's names: operators, match binders, instances, indices, 2026-09-06, laraserver4

Four findings of the second audit in the parser ([issue 41](https://github.com/mbovel/vik/issues/41),
[issue 42](https://github.com/mbovel/vik/issues/42), [issue 43](https://github.com/mbovel/vik/issues/43),
[issue 44](https://github.com/mbovel/vik/issues/44)): a script's own function named `+` or `to_real` was
read as the operator, the `match` rewrite's binder captured a `let`-bound name, a polymorphic instance's
name merged with a script symbol, an index past 2^31 was truncated. The fixes add a hash lookup at the
head of every application (is it the script's symbol?), a wider skip for the binder's name, two
refusals.

Cold, VIK on the JVM, the 44 evaluation instances, the median of three runs: 28.99 s before, 29.01 s
after, no instance past the noise. The lookup is a map hit per application, and the parse is a small
part of a cold run.

**Kept.**
