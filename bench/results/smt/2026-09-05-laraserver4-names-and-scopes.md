# Names and scopes in the front end, 2026-09-05, laraserver4

Two soundness bugs in the front end ([issue 10](https://github.com/mbovel/vik/issues/10),
[issue 11](https://github.com/mbovel/vik/issues/11)): a `define-fun` body was expanded in the caller's
environment, so a global it read was captured by a caller's parameter of the same name; and a sort name
holding a space was cut in two inside the function-sort strings, so a real-valued function over it was
given an integer variable. Both answered unsat on satisfiable scripts.

The fixes: a macro body is expanded from its parameters alone; a named sort prints with the bars it
needs (`|a b|`), the function-sort splitter respects them, and an abstract element of such a sort is
named from the bare text so that it prints as one legal symbol (`|@a b!0|`). An instance of a parametric
sort keeps its parenthesised name, `(List Int)`, which is how the parser names it.

Every declaration now prints its sort through the symbol check, once per declared symbol. Cold, the
median of three, laraserver4, against the committed run of 2026-09-04:

| Instance | Committed | With the fixes |
|---|---:|---:|
| QG-classification__qg5__iso_icl662 | 0.42 s | 0.44 s |
| 20170829-Rodin__smt3534838651727683253 | 0.26 s | 0.26 s |
| TypeSafe__z3.1184131 | 0.26 s | 0.26 s |
| bmc__stack_k20 | 0.77 s | 0.76 s |
| kind__car_6_e2_893 | 2.05 s | 2.05 s |

Nothing moves. **Kept.**
