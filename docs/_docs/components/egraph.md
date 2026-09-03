---
title: The e-graph
extraJS:
  - js/snippets.js
extraCSS:
  - css/site.css
---

# The e-graph

Congruence closure with backtracking, explanations and disequalities: the data structure under the
[theory of uninterpreted functions](../theories/uninterpreted-functions.md), and the one the
[datatype rules](../theories/sorts.md) hang on. It is a module of its own — `net.bovel.egraph` — with no
dependency on the rest.

## Using it

```scala
import net.bovel.egraph.{App, EGraph}
import scala.collection.immutable.ArraySeq

val eg = EGraph()
val f = eg.newClass()
val a = eg.newClass()
val b = eg.newClass()
val fa = eg.add(App(f, ArraySeq(a)))
val fb = eg.add(App(f, ArraySeq(b)))

eg.merge(a, b, reason = 1) // the reason is yours: a literal, an index, anything
eg.rebuild()
eg.find(fa) == eg.find(fb) // true, by congruence
eg.explain(fa, fb)         // ArrayBuffer(1): why they are equal
```

`disequate(a, b, reason)` records a disequality, `conflict` reports the merge that violated one with the
reasons that led to it, and `push()`/`pop()` frame everything so a search can undo it. `Rules` is the hook
the datatype theory uses: callbacks when a node is added and when two classes are merged, `derive` for an
equality with its premises, `contradiction`, and a final check.

## What is in it

| Feature | | Notes |
|---|---|---|
| Dense `Int` ids, eager union-find (union by size, circular member lists) | ✓ | as Z3 and cvc5 |
| Deferred congruence rebuild with a pending list (egg) | ✓ | |
| `java.util.HashMap` as the congruence table | ✓ | 5% faster than Scala's, [measured](https://github.com/mbovel/vik/blob/main/bench/results/egraph/2026-08-28-laraserver4-scala-hashmap.md) |
| Backtracking: `push`/`pop` over an undo trail | ✓ | |
| Disequalities (forbid lists) and conflicts | ✓ | |
| Explanations: union tree, most recent edge below the LCA (LISA's scheme), linear LCA | ✓ | [`f3d89a4`](https://github.com/mbovel/vik/commit/f3d89a4) |
| Event log and per-class atom lists for theory propagation | ✗ | 1.8× on one instance, nothing elsewhere, 85 subtle lines: removed for readability ([`6badd0f`](https://github.com/mbovel/vik/commit/6badd0f) → [`bf015d6`](https://github.com/mbovel/vik/commit/bf015d6), [measurements](https://github.com/mbovel/vik/blob/main/bench/results/smt/2026-08-29-laraserver4-scan.md)) |
| Cached node hashes | ✗ | [no gain](https://github.com/mbovel/vik/blob/main/bench/results/egraph/2026-08-28-laraserver4-cached-hash.md) |
| Union-tree edges as flat arrays | ✗ | [no gain](https://github.com/mbovel/vik/blob/main/bench/results/egraph/2026-08-28-laraserver4-flat-edges.md) |
| Rules: callbacks on added nodes and merges, derived equalities with premises as justification, contradictions, a final check | ✓ | [docs/_docs/adt-plan.md](../adt-plan.md); the datatype theory is such a rule set |

The explanations follow LISA's scheme — the most recent edge below the lowest common ancestor of the union
tree, no re-rooting — and the closure itself is the eager union-find that z3 and cvc5 use, because undoing it
is symmetric and cheap.

Results: [egraph benchmarks](https://github.com/mbovel/vik/tree/main/bench/results/egraph).
