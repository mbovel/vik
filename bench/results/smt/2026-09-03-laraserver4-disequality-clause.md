# A disequality of shared terms as a clause, with the relaxation's phase, 2026-09-03, laraserver4

The combination with uninterpreted functions decides an arrangement of the shared integer terms: an
equality atom per pair that congruence could relate. False, the atom was `assertDistinct` in the LIA
solver, which decides a disequality by branching in its final check, at the value the relaxation lands on,
without learning — on `wisas`, hundreds of them. Now a false pair atom asserts nothing in the simplex: the
clause `e ∨ (a − b ≤ −1) ∨ (a − b ≥ 1)` over two bound atoms makes the SAT solver take a side, and learn.

That alone was worse (the SAT solver picks sides blindly): the theory now suggests the phase of a bound
atom's *first* decision as the relaxation stands to it (`Theory.phase`, `cdclt` asks before deciding),
on problems that have shared pairs, so the side taken is the one already true and nothing moves.
Guiding every decision lost phase saving's locality (`hash` 3× slower); guiding on problems without
shared pairs made an unsat search meet its conflicts only at the final check (a bit-vector file 200×
slower); guiding only the clauses' own sides gave `wisas` nothing. The variants, cold, laraserver4:

| Variant | `xs_24_44` | `xs_38_38` | `hash_sat_09_08` | Certora `65782_6` | `bvrewrite-extract` (unsat) |
|---|---:|---:|---:|---:|---:|
| branching (before) | 10.3 s | 7.9 s | 3.0 s | 11.5 s | 0.34 s |
| clause alone | 19.9 | 52 | 2.0 | 6.3 | — |
| clause, phase on every decision | 3.6 | 4.1 | 8.5 | 5.1 | > 60 |
| clause, phase at first decision, every problem | 4.1 | 4.5 | 0.94 | 4.8 | > 60 |
| clause, phase on the clauses' sides only | 11.1 | 7.3 | 2.0 | 5.6 | 0.35 |
| **clause, phase at first decision, problems with shared pairs (kept)** | **4.5** | **4.3** | **0.96** | **4.7** | **0.36** |
| branching with that phase | 23.7 | 11.5 | 0.87 | 8.7 | 0.36 |

Warm, `VikBench.api` (JMH `-f 2 -wi 3 -i 3`, ms/op, [before](2026-09-03-laraserver4-disequality-before.json),
[after](2026-09-03-laraserver4-disequality-after.json)):

| Instance | Before | After | Ratio |
|---|---:|---:|---:|
| wisas__xs_8_8 | 26.896 ± 2.826 | 8.734 ± 0.255 | 3.08× |
| mathsat__Wisa__xs-09-05-2-4-4-5 | 29.994 ± 0.932 | 30.170 ± 0.672 | 0.99× |
| dillig__20-4 | 8.646 ± 0.761 | 8.706 ± 0.719 | 0.99× |
| CAV_2009_benchmarks__20-vars__problem__001 | 2.093 ± 0.036 | 2.053 ± 0.036 | 1.02× |
| kind__MESI_i4.ec | 311.232 ± 21.480 | 275.478 ± 5.714 | 1.13× |
| 20190906-CLEARSY__0020__00009 | 9.332 ± 0.252 | 9.507 ± 0.331 | 0.98× |
| alpha-093dddceea58 | 0.180 ± 0.002 | 0.204 ± 0.019 | 0.89× |
| 20230720-blocksworld__from_0_1_3_to_1_1_2_bmc_0 | 0.188 ± 0.033 | 0.187 ± 0.017 | 1.00× |

**The compiler's queries, warm, each best of 20 (2 031 queries):** 156 ms before, 185 ms after. The ten
slowest gain (34.2 to 25.2 ms — the two slowest `fib` queries 2.3× and 1.9×, the `mergeSortLength` pair
2.1×), the 1 831 quick ones barely move (36.0 to 39.3 ms), and a family of medium `fib_memoized2` queries
loses three times (0.8 to 2.7 ms each): many pairs, none of which they need, each now a bound the simplex
carries on every path, where the branching paid only for the pairs the relaxation hit. 323 queries are
more than 20% slower, 191 more than 20% faster.

**Kept, with that trade, on the strength of the other benchmarks.**

## The lazy side, tried after (branch `lazy-sides`, not kept)

The fix expected to remove the compiler's regression: a false pair asserts nothing and mints nothing;
when the relaxation lands on the forbidden value — checked after every relaxation in `propagate` and
around branch and bound in `finalCheck` — the theory returns the clause with its two side atoms created
then (`Theory.Result.Lemmas`; the SAT solver adds the clauses during the search, watches them, bumps
their fresh variables to the front of the decision order). Every split of a round at once: one per round
was too slow, and a first cut that only looked in `finalCheck` never answered `xs_8_8`, branch and bound
wandering a relaxation with no disequality in it. Cold, laraserver4, the compiler's 2 031 queries warm
as above:

| Variant | `xs_24_44` | `xs_38_38` | `hash_sat_09_08` | Certora `65782_6` | `bvrewrite-extract` | compiler set |
|---|---:|---:|---:|---:|---:|---:|
| branching (before) | 10.3 s | 7.9 s | 3.0 s | 11.5 s | 0.34 s | 156 ms |
| eager sides, phase at first decision (kept) | 4.5 | 4.3 | 0.96 | 4.7 | 0.36 | 185 |
| lazy sides, phase at first decision | 9.9 | 7.7 | 0.88 | 9.3 | 0.36 | 178 |
| lazy sides, no phase | 22.6 | > 60 | 1.9 | 12.9 | — | 152 |
| lazy sides, phase on the sides only | 10.1 | 11.6 | 3.0 | 13.6 | — | 158 |

Two things the eager variant's numbers hid. The compiler's regression is the phase guidance, not the
bounds: the lazy sides with the same guidance still cost 178 ms (the `fib_memoized2` family 2.7 ms as
before), and without it the set is back at 152 — but `wisas` then needs the guidance on every bound, the
sides alone give it nothing, as the eager experiment had found. And the laziness itself costs `wisas`
2× against the eager sides: an eager side is decided at its first decision on the side the relaxation
already satisfies, and nothing moves; a lazy side is born when the relaxation is on the forbidden value,
so taking either side moves it. The eager variant stays. What would give the compiler its 30 ms back is
a guidance that leaves the unsat search's conflicts early, and no variant of "which atoms" does that.
