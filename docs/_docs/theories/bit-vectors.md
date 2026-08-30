---
title: Bit-vectors
extraJS:
  - js/snippets.js
extraCSS:
  - css/snippets.css
---

# Bit-vectors

The arithmetic fragment of `QF_BV`, without bit-blasting: a bit-vector is an integer in `[0, 2ⁿ)`, and the
operations that are arithmetic modulo `2ⁿ` are lowered to linear integer arithmetic. The width costs nothing.

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

Every bit-vector term becomes an integer with its range asserted, and every operation becomes its arithmetic
image, with an overflow count where the modulus needs one: `bvadd a b` is `a + b - 2ⁿ·k` with `k` a fresh
constant in `{0, 1}`, `extract` is a division and a remainder by powers of two, `concat` a weighted sum, the
signed comparisons are unsigned ones on the value shifted by the sign bit. The lowering happens in the
[front end](../components/front-end.md) (`BitVectors.scala`), before the solver sees anything: the
`set-logic` is rewritten, so the script that VIK plays is a `QF_LIA` script, which `(get-info)`-style
inspection or the [playground](https://github.com/mbovel/vik) will show you.
