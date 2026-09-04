# The atoms of a popped level, out of the decisions, 2026-09-04, laraserver4

A level is retired by a selector: `pop` asserts the selector false, which satisfies the level's clauses,
and the level's atoms stay in the SAT solver — nothing decides to remove them, since a later assertion may
name one again. They are still decided, and every decision on one asserts a bound in the LIA solver, on
every path of the search. It shows on any script that pushes and pops a bit-vector query, where a level
introduces hundreds of atoms:

| Script (eight independent multiplication identities, 6 bits) | Time |
|---|---:|
| one `reset-assertions` between the checks (a fresh solver each time) | 3.5 s |
| `push`/`pop` between the checks, as the file is written today | > 120 s (no answer) |
| `push`/`pop`, the level's atoms taken out of the decisions | 3.8 s |

The last row is the experiment: `cdclt.Solver.retire(v)` leaves a variable out of `nextDecision`, `pop`
retires the atoms the level introduced (the range above the atom count at its push), and any clause that
names one puts it back. Thirty times faster, and **unsound**: four corpus files answer wrongly
(`QF_BVLRA/…/corridor_001_k3_E2`, `…_k3_A2`, `…_k2_B`, `QF_DT/princess/adt__datatype2`). A variable that no
*live* clause names is safe to leave undecided; one that a live clause names is not, and a level's atoms
are named by clauses that outlive it — the theory lemmas the search learned while the level was open carry
no selector, since a theory conflict's reasons are the atoms themselves.

Retiring only what no live clause names (one pass over the clauses at each pop) is sound and gives nothing:
those learned clauses keep every atom decidable, and the file takes its 120 s again.

**What would work**: a learned clause is a consequence, never a premise, so one that mentions an atom of a
retired level can be deleted. Delete them at the pop, then retire the atoms no surviving clause names. That
needs clause deletion in `cdclt` — the clause array is indexed by position and `reason(v)` holds an index,
so deleting means rebuilding the watches and remapping the reasons of the level-0 trail. Worth doing: the
gain is thirty times on incremental bit-vector scripts, and clause deletion is also what `reduceDB` would
need. Not done here; the experiment is reverted.

The corpus files of the multiplication and the shifts use `reset-assertions` between their checks for this
reason, which is why they answer in seconds.
