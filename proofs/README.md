# Proofs

With proofs on, Vik backs every `unsat` answer with a certificate that an
independent checker can replay from the SMT-LIB script, with no trusted steps.
A checker for a proof assistant needs, once and for all, the lemmas that make
those certificates sound. This folder holds them, one subfolder per checker:

- [`rules.md`](rules.md) lists the lemmas without reference to any checker:
  for each rule of the lowering, the facts it asserts and the values that
  satisfy them, and for each operation, how its lowered term computes the
  value. A checker for another system proves the same list.
- [`lean/`](lean) proves them in Lean 4, with nothing beyond Lean's core
  library and its standard axioms.

## What the lemmas cover

`LoweringCheck` replays the lowering of each command. It checks that every
symbol the lowering introduces comes from a rule, and that every assertion it
adds is that rule's definition of the symbol. A refutation of the lowered script
is then a refutation of the surface script, provided two things hold:

1. **Each rule's facts are satisfiable.** Given values for the rule's
   arguments, within the bounds the replay guarantees, there are values for the
   rule's symbols that satisfy its facts. A model of the surface script then
   extends to a model of the lowered script.
2. **Each lowered term has the right value.** When the symbols take those
   values, each lowered term evaluates to the value of its surface term. For a
   bit-vector, that value is the unsigned one, in `[0, 2^w - 1]`.

The lemmas here are exactly those two kinds. Not yet covered are the formats of
the certificates themselves: clausal proofs, congruence explanations and
cutting-planes steps. A checker either replays those as it reads them, or
proves the checker functions it runs.
