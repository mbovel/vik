# Lean proofs

The lemmas of [`../rules.md`](../rules.md), proved in Lean 4 with only Lean's
core library: no Mathlib, no `sorry`, and no axioms beyond Lean's standard
three (`propext`, `Classical.choice`, `Quot.sound`).

```sh
lake build                    # checks every proof
lake env lean Axioms.lean     # lists the theorems and fails on any other axiom
```

`lean-toolchain` pins the Lean version; [elan](https://github.com/leanprover/elan)
installs it on first use.

## Files

- `Vik/Rules.lean`: the rules, each with the values of its symbols and a proof
  that they satisfy its facts.
- `Vik/Values.lean`: the value of a bit-vector (`val`), its sign and its signed
  value.
- `Vik/Arith.lean`: addition, subtraction, negation, complement, products and
  shifts by constants, unsigned and signed division.
- `Vik/Signed.lean`: signed division through magnitudes, by a constant and by
  an unknown divisor.
- `Vik/Bits.lean`: bit decomposition, bitwise operations and masks, products
  of unknowns, `extract`, `concat`, `repeat`, rotations, extensions,
  comparisons and equality.
- `Vik/Shifts.lean`: arithmetic shifts by constants, and the barrel shifter for
  shifts by unknown amounts.
- `Vik/Real.lean`: `floor`, `to_int`, `is_int` and rational constants, over any
  ordered field of characteristic zero, as `grind` describes them (Mathlib's
  `ℝ` is one).

Bit-vector semantics are Lean's `BitVec`, whose SMT-LIB operations are
`smtUDiv`, `smtSDiv`, `srem`, `smod`, `sshiftRight'` and the rest under their
usual names.
