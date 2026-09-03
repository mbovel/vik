
# Addendum: the bit-vector corpus (`../QF_UFBVDT/dotty/`)

The compiler now lowers `Int`/`Long` to `(_ BitVec 32)`/`(_ BitVec 64)` by default (JVM semantics;
`-Yqualified-types-smt-ideal-ints` keeps the unbounded integers): `bvadd`/`bvsub`, `bvmul` by a
literal, signed comparisons, two's-complement literals, and `/`, `%` as `bvsdiv`/`bvsrem` — which
VIK refuses today ("no bit-blasting"); please add them, at least by a literal divisor (the
`(lo + hi) / 2` of `binary_search`). 1,774 queries; same headers and `:status` convention.

## What happens today

- z3 (bit-blasting): all 1,774 in 42 s wall *including a process per query*, 0 disagreements
  with the e-graph, 5 `bvsdiv`/4 `bvsrem` queries answered.
- VIK offline (fuel 200k, one run each): median 0.20 ms (10× the integer corpus), p90 1.7 ms,
  p99 85 ms, max 3.8 s, 21.5 s total; 9 queries out of fuel (the `fib_*` shapes, 1–4 s each).
- VIK inside the compiler (fuel 1M): solving goes from 10 s to **441 s** CPU over the suite, one
  leaf 54 s, 19 fuel-outs, 129 leaves proved by the e-graph but not by VIK.
- **Wrong answers**: 11 queries VIK answers `sat` that z3 proves `unsat` (and the e-graph proves).
  All have a datatype with a bit-vector-sorted field; the same queries with `Int` fields are
  proved. Smallest instance, `purity_cc-36fafbe07bd4.smt2`:

```smt2
(declare-datatypes ((Box 0)) (((new_Box (Box.value@Box (_ BitVec 32))))))
(declare-fun x0 () Box)
(assert (= (new_Box (_ bv1 32)) x0))
(assert (not (= (_ bv1 32) (Box.value@Box x0))))
(check-sat)   ; VIK: sat, z3: unsat
```

  Others: `intarray-c35dd95930c9`, `range-54cec765e157`, `matrices3-ed51cfc736b5`,
  `matrices4-{06558b5c1e60,509f3cf21279,57da5555e3f7,7a871379d291,dcb8320d07a9}`,
  `matrices5-c91119ea1063`. The selector equation of a constructor application seems not to be
  applied when the field's sort is a bit-vector (lowered to a bounded integer).

## Reading

The bit-vectors-as-bounded-integers lowering turns each `bvadd` into an integer with an overflow
count and a `[0, 2^32)` window, and the queries — which are trivial by congruence plus a little
linear arithmetic in the integer corpus — become branch-and-bound problems over 2^32-sized
constants. For a type checker that needs these answers in microseconds, the integer corpus is
the realistic target for now and the bit-vector one is the goal; besides `bvsdiv`/`bvsrem` and
the datatype bug, a cheaper treatment of 32-bit arithmetic (no overflow variables while no bound
can reach 2^31, or bit-blasting small widths) would be needed before the bit-vector default is
usable with VIK.
