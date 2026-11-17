import Vik.Cert.Proof

/-!
# An example

`x ≤ 0` and `1 ≤ x`, both asserted under the level `s`: the proof derives `¬s`
from the two input clauses and a cutting-planes lemma, and the kernel checks it.
-/

namespace Vik.Low.Example

open Vik.Clausal

/-- Sorts `Bool` and `Int`; one integer constant `x`. -/
def problem : Problem where
  sig := ⟨[.bool, .int], [1]⟩
  nodes := [.tt, .const 0, .num 0, .num 1, .le 1 2, .le 3 1, .bterm 0, .ff, .bterm 7]
  sortOf := [0, 1, 1, 1, 0, 0, 0, 0, 0]
  inputs := []
  datatypes := []
  stores := []

/-- Variables: `0` is `true`, `1` is `x ≤ 0`, `2` is `1 ≤ x`, `3` is the level. -/
def answer : Answer where
  meaning := [some 0, some 4, some 5, none]
  trueLit := ⟨0, true⟩
  levels := [(3, [4, 5])]
  assumed := [3]
  ufAtoms := []
  trueClass := 6
  falseClass := 8
  ufCtors := []
  ufTesters := []
  -- `x ≤ 0` and `-x ≤ -1`
  liaAtoms := [none, some (.bound 0 0 false), some (.bound 1 (-1) false)]
  liaForms := [[(1, 1)], [(1, -1)]]
  steps := [
    .input [⟨3, false⟩, ⟨1, true⟩],
    .input [⟨3, false⟩, ⟨2, true⟩],
    .lemma [⟨1, false⟩, ⟨2, false⟩],
    .derived [⟨3, false⟩] [0, 1, 2]]
  justs := [.input (.assertion 4), .input (.assertion 5),
    .lia [.hyp ⟨2, 0, 0, false, true⟩, .hyp ⟨4, 1, -1, false, true⟩, .comb [(1, 0), (1, 1)]],
    .derived]
  conclusion := 3


theorem accepted : problem.accepts answer = true := by decide +kernel

/-- No model satisfies both assertions under the level. -/
theorem no_model {R U : Type} [Lean.Grind.Field R] [LE R] [LT R] [Std.IsLinearOrder R]
    [Std.LawfulOrderLT R] [Lean.Grind.OrderedRing R] [Lean.Grind.IsCharP R 0] (M : Model R U)
    (size : Val R U → Nat) (hL : problem.Laws M size) (h4 : holds M.toInterp problem.nodes 4)
    (h5 : holds M.toInterp problem.nodes 5) : False :=
  Problem.accepts_sound accepted M size hL (by simp [problem]) (by
    intro s hs fs hfs f hf
    simp [answer] at hs; subst hs
    simp [answer] at hfs; subst hfs
    simp at hf; rcases hf with rfl | rfl <;> assumption)

end Vik.Low.Example
