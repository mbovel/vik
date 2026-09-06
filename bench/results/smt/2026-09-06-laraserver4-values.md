# Values computed from the model, 2026-09-06, laraserver4

Six findings of the second audit on `get-value` and `get-model` ([issues 45 to 50](https://github.com/mbovel/vik/issues/45)):
`is_int` false and `to_int` printed as `floor!8`, a macro named `ite!1` valued as the lowering's constant,
bit-vectors inside datatypes and arrays printed as integers, minted names and raw integers as values, a
function's row on a store missed, a function symbol echoed as its own value. One cause behind most of
them: a value was read through the *lowered* term under the model, and the lowering's fresh constants
have no value in a model. The values are now computed from the model's values of the declared symbols by
an evaluator of the surface term — arithmetic on rationals, the bit-vector operations as SMT-LIB defines
them, arrays and datatypes as the model's closed terms — and written at the script's sorts, nested ones
included. `ModelCheck` judges every model of the corpus with z3: 42, none wrong.

Cold, VIK on the JVM, the 44 evaluation instances, the median of three runs: 29.03 s before, 29.05 s
after. None of them asks for a value.

**Kept.**
