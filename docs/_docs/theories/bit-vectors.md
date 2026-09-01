---
title: Bit-vectors
extraJS:
  - js/snippets.js
extraCSS:
  - css/site.css
  - css/snippets.css
---

# Bit-vectors

The arithmetic fragment of `QF_BV`, without bit-blasting: a bit-vector is an integer in `[0, 2ⁿ)`, and the
operations that are arithmetic modulo `2ⁿ` are lowered to linear integer arithmetic. The width costs nothing.

**Decision procedure: yes, for the fragment it accepts.** Anything outside the fragment is refused up
front, and the lowered problem has bounded variables, which the integer solver decides.

## What you can write

```smt2
(set-logic QF_BV)
(declare-const a (_ BitVec 64))
(declare-const b (_ BitVec 64))
(assert (bvult a b))
(assert (bvult b a))
(check-sat)
```

Supported: `bvadd`, `bvsub`, `bvneg`, `bvnot`, `bvmul` and `bvudiv`, `bvurem`, `bvshl`, `bvlshr` by
constants, `bvand`/`bvor` with a mask that is a power of two minus one, `concat`, `extract`, `zero_extend`,
`sign_extend`, the unsigned and the signed comparisons, equality, and the literals `#b…`, `#x…`, `(_ bvN w)`.

```smt2
(set-logic QF_BV)
(declare-const x (_ BitVec 8))
(assert (= ((_ extract 3 0) x) #x5))
(assert (bvuge x #xf0))
(assert (distinct x #xf5))
(check-sat)
```

Refused, because they are not arithmetic: `bvand`, `bvor`, `bvxor` of two unknowns, `bvmul` of two unknowns,
shifts and divisions by an unknown. A script that uses them is rejected rather than answered — the whole
point of this fragment is that what it answers, it answers by arithmetic.

Of the small files of `QF_BV` in the library, a bit less than a third are in the fragment. Where the fragment
applies, the width is free: `pspace/ndist.a.20000` is decided in milliseconds here and times out for a
bit-blasting solver ([evaluation](../evaluation.md)).

## Internals

Every bit-vector term becomes an integer with its range asserted, and every operation becomes its
arithmetic image (`net.bovel.vik.Lowering`), with fresh bounded constants where the modulus or a
division needs them — each fresh constant is declared and defined by one linear equation before the
assertion that uses it. Constant arguments fold in the lowering. For an `n`-bit term, `2ⁿ` is written
`m` below:

| Operation | Integer image | Fresh constants and their defining equation |
|---|---|---|
| a declared `(_ BitVec n)` constant `a` | `a` | none; `0 ≤ a ≤ m − 1` asserted |
| `bvadd a b` | `r` | `r ∈ [0, m)`, `k ∈ {0, 1}`: `a + b = r + m·k` |
| `bvsub a b` | `r` | `r ∈ [0, m)`, `k ∈ {−1, 0}`: `a − b = r + m·k` |
| `bvneg a` | `r` | `r ∈ [0, m)`, `k ∈ {−1, 0}`: `−a = r + m·k` |
| `bvnot a` | `m − 1 − a` | none |
| `bvmul a c`, `c` a constant | `r` | `r ∈ [0, m)`, `k ∈ [0, c − 1]`: `c·a = r + m·k` |
| `bvshl a c` | as `bvmul a 2ᶜ`; `0` when `c ≥ n` | as `bvmul` |
| `bvlshr a c` | `q` | `q ∈ [0, m/2ᶜ)`, `r ∈ [0, 2ᶜ)`: `a = 2ᶜ·q + r`; `0` when `c ≥ n` |
| `bvudiv a c` | `q` | `q ∈ [0, (m−1)/c]`, `r ∈ [0, c)`: `a = c·q + r`; `m − 1` when `c = 0` |
| `bvurem a c` | `r` | the same division; `a` when `c = 0` |
| `bvand a c`, `c = 2ᵏ − 1` | `r` | `q, r` of `a = 2ᵏ·q + r`: the low bits are the remainder |
| `bvand a c`, `c = m − 2ᵏ` | `a − r` | the same division: the high bits are what remains |
| `extract i j a` | `r₂` | `a = 2ʲ·q₁ + r₁`, then `q₁ = 2^(i−j+1)·q₂ + r₂` |
| `concat a b`, `b` of width `k` | `2ᵏ·a + b` | none |
| `zero_extend k a` | `a` | none |
| `sign_extend k a` | `a + (2^(n+k) − m)·s` | `s ∈ {0, 1}`, `u ∈ [0, m/2)`: `a = u + (m/2)·s` |
| `bvult`, `bvule`, `bvugt`, `bvuge` | `<`, `≤`, `>`, `≥` directly | none |
| `bvslt`, `bvsle`, `bvsgt`, `bvsge` | the same on `a − m·s` per side | per side `s, u` as for the sign bit |

The parser reads bit-vector terms as they are written — the tree has them (`Term.BvApp`, `Term.Bits`) —
and the lowering is one function of the solver, `net.bovel.vik.Lowering`: it takes a script and returns
the lowered script, declaring the constants each assertion needs and asserting their definitions before
it, so the script that VIK plays is a `QF_LIA` script. The player applies it before playing. Two
ways to see it: `(get-assertions)` in a script answers with the lowered assertion stack, and
`smt2lowered file.smt2` on the command line prints a whole file as VIK will play it.

```smt2
(set-logic QF_BV)
(declare-const x (_ BitVec 8))
(declare-const y (_ BitVec 8))
(assert (bvult x y))
(check-sat)
(get-assertions)
```

The answer is the ranges of `x` and `y` and the comparison, lowered — `bvult` is `<` on the values,
written `x + 1 ≤ y`:

```
sat
((and (<= 0 x) (<= x 255)) (and (<= 0 y) (<= y 255)) (<= (+ x 1) y))
```
