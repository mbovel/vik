import Vik.Lower.Trace

/-!
# The lowered model of a surface model

From a model of a surface script, a model of its lowering: the same values,
with one more object for each store symbol, so that applying it is the store
of the surface model. Its minted `ite` symbols take the values of their
`ite`s.
-/

set_option linter.unusedSectionVars false

namespace Vik.Lower

open Lean.Grind Vik.Real Vik.Low Vik.Surf

variable {R U : Type} [Field R] [LE R] [LT R] [Std.IsLinearOrder R] [Std.LawfulOrderLT R]
  [OrderedRing R] [IsCharP R 0]

attribute [local instance low] Lean.Grind.Ring.intCast

/-! ## Values -/

/-- A surface value as a lowered one. -/
def enc : Val R U → Val R (U ⊕ Nat)
  | .num r => .num r
  | .bool b => .bool b
  | .obj u => .obj (.inl u)

/-- A lowered value as a surface one; a store symbol is not one. -/
def dec : Val R (U ⊕ Nat) → Val R U
  | .num r => .num r
  | .bool b => .bool b
  | .obj (.inl u) => .obj u
  | .obj (.inr _) => .bool false

@[simp] theorem dec_enc (v : Val R U) : dec (enc v) = v := by cases v <;> rfl

theorem enc_inj {v w : Val R U} (h : enc v = enc w) : v = w := by
  have := congrArg dec h; simpa using this

theorem map_dec_enc (vs : List (Val R U)) : (vs.map enc).map dec = vs := by
  simp [List.map_map, Function.comp_def]

@[simp] theorem truth_enc (v : Val R U) : (enc v).truth = v.truth := by
  cases v <;> simp [enc, Val.truth]

@[simp] theorem toR_enc (v : Val R U) : (enc v).toR = v.toR := by
  cases v <;> rfl

@[simp] theorem enc_bool (b : Bool) : enc (.bool b : Val R U) = .bool b := rfl
@[simp] theorem enc_num (r : R) : enc (.num r : Val R U) = .num r := rfl

/-! ## Surface laws -/

/-- The argument sorts of a surface symbol. -/
def SProblem.argSorts (SP : SProblem) (C : Nat) : List Nat :=
  match SP.sig.decls[C]? with
  | some s =>
    match (SP.sig.sorts[s]?).map lowerSort with
    | some (.fn as _) => as
    | _ => []
  | none => []

/-- A surface model as a lowered one over the same values, for its datatype
    laws. -/
def SModel.low (Ms : Surf.Model R U) : Low.Model R U := ⟨⟨Ms.const, Ms.app⟩, Ms.dom⟩

/-- What a model of a surface script satisfies: its sorts and symbols, and the
    laws of datatypes, as for lowered problems (`Problem.Laws`). -/
structure SProblem.Laws (SP : SProblem) (Ms : Surf.Model R U) (size : Val R U → Nat) : Prop where
  sound : Ms.Sound SP.sig
  sel : ∀ d cs C T Ss, (d, cs) ∈ SP.datatypes → (C, T, Ss) ∈ cs →
    ∀ xs (i : Nat) S x, Forall2 Ms.dom (SP.argSorts C) xs → Ss[i]? = some S → xs[i]? = some x →
      Ms.app (Ms.const S) [ctorVal (SModel.low Ms) C xs] = x
  test : ∀ d cs C T Ss, (d, cs) ∈ SP.datatypes → (C, T, Ss) ∈ cs →
    ∀ xs, Forall2 Ms.dom (SP.argSorts C) xs → Ms.app (Ms.const T) [ctorVal (SModel.low Ms) C xs] = .bool true
  testOther : ∀ d cs C T Ss C' T' Ss', (d, cs) ∈ SP.datatypes → (C, T, Ss) ∈ cs → (C', T', Ss') ∈ cs →
    C ≠ C' → ∀ ys, Forall2 Ms.dom (SP.argSorts C') ys →
      Ms.app (Ms.const T) [ctorVal (SModel.low Ms) C' ys] ≠ .bool true
  inj : ∀ d cs C T Ss, (d, cs) ∈ SP.datatypes → (C, T, Ss) ∈ cs →
    ∀ xs ys, Forall2 Ms.dom (SP.argSorts C) xs → Forall2 Ms.dom (SP.argSorts C) ys →
      ctorVal (SModel.low Ms) C xs = ctorVal (SModel.low Ms) C ys → xs = ys
  distinct : ∀ d cs C T Ss C' T' Ss', (d, cs) ∈ SP.datatypes → (C, T, Ss) ∈ cs → (C', T', Ss') ∈ cs →
    C ≠ C' → ∀ xs ys, Forall2 Ms.dom (SP.argSorts C) xs → Forall2 Ms.dom (SP.argSorts C') ys →
      ctorVal (SModel.low Ms) C xs ≠ ctorVal (SModel.low Ms) C' ys
  expand : ∀ d cs C T Ss, (d, cs) ∈ SP.datatypes → (C, T, Ss) ∈ cs →
    ∀ v, Ms.dom d v → Ms.app (Ms.const T) [v] = .bool true →
      v = ctorVal (SModel.low Ms) C (Ss.map fun S => Ms.app (Ms.const S) [v])
  smaller : ∀ d cs C T Ss, (d, cs) ∈ SP.datatypes → (C, T, Ss) ∈ cs →
    ∀ xs x, Forall2 Ms.dom (SP.argSorts C) xs → x ∈ xs → size x < size (ctorVal (SModel.low Ms) C xs)
  exhaust : ∀ d cs, (d, cs) ∈ SP.datatypes → ∀ v, Ms.dom d v →
    ∃ ct ∈ cs, Ms.app (Ms.const ct.2.1) [v] = .bool true

/-! ## The lowered model -/

section
variable (SP : SProblem) (P : Problem) (tr : Trace) (Ms : Surf.Model R U)

/-- Whether `k` is a minted store symbol. -/
def isStore (k : Nat) : Prop :=
  SP.sig.decls.length ≤ k ∧ ∃ s, tr.mints[k - SP.sig.decls.length]? = some (.store s)

noncomputable def lowModel : Low.Model R (U ⊕ Nat) where
  const k :=
    if k < SP.sig.decls.length then enc (Ms.const k)
    else
      match tr.mints[k - SP.sig.decls.length]? with
      | some (.ite i) => enc (Surf.val Ms.toInterp SP.nodes i)
      | some (.store _) => .obj (.inr k)
      | none => .bool false
  app f xs :=
    match f, xs with
    | .obj (.inr _), [a, i, v] => enc (Ms.store (dec a) (dec i) (dec v))
    | .obj (.inr _), _ => .bool false
    | f, xs => enc (Ms.app (dec f) (xs.map dec))
  dom s v :=
    if s < SP.sig.sorts.length then ∃ w, Ms.dom s w ∧ v = enc w
    else ∃ k, v = .obj (.inr k) ∧ isStore SP tr k ∧ P.sig.decls[k]? = some s

theorem app_enc (f : Val R U) (xs : List (Val R (U ⊕ Nat))) :
    (lowModel SP P tr Ms).app (enc f) xs = enc (Ms.app f (xs.map dec)) := by
  cases f <;> rfl

theorem app_enc_enc (f : Val R U) (xs : List (Val R U)) :
    (lowModel SP P tr Ms).app (enc f) (xs.map enc) = enc (Ms.app f xs) := by
  rw [app_enc, map_dec_enc]

theorem const_base {k : Nat} (h : k < SP.sig.decls.length) :
    (lowModel SP P tr Ms).const k = enc (Ms.const k) := by
  simp [lowModel, h]

theorem const_ite {k i : Nat} (h : SP.sig.decls.length ≤ k) (hm : tr.mints[k - SP.sig.decls.length]? = some (.ite i)) :
    (lowModel SP P tr Ms).const k = enc (Surf.val Ms.toInterp SP.nodes i) := by
  simp [lowModel, hm, Nat.not_lt.mpr h]

theorem const_store {k s : Nat} (h : SP.sig.decls.length ≤ k) (hm : tr.mints[k - SP.sig.decls.length]? = some (.store s)) :
    (lowModel SP P tr Ms).const k = .obj (.inr k) := by
  simp [lowModel, hm, Nat.not_lt.mpr h]

theorem app_store (k : Nat) (a i v : Val R U) :
    (lowModel SP P tr Ms).app (.obj (.inr k)) [enc a, enc i, enc v] = enc (Ms.store a i v) := by
  simp [lowModel]

theorem dom_base {s : Nat} (hs : s < SP.sig.sorts.length) (v : Val R (U ⊕ Nat)) :
    (lowModel SP P tr Ms).dom s v ↔ ∃ w, Ms.dom s w ∧ v = enc w := by
  simp [lowModel, hs]

theorem forall2_base : ∀ (as : List Nat) (xs : List (Val R (U ⊕ Nat))), (∀ a ∈ as, a < SP.sig.sorts.length) →
    (Forall2 (lowModel SP P tr Ms).dom as xs ↔ ∃ ys, xs = ys.map enc ∧ Forall2 Ms.dom as ys)
  | [], [], _ => by simp [Forall2]
  | [], _ :: _, _ => by
    simp only [Forall2, false_iff, _root_.not_exists, _root_.not_and]
    intro ys h; cases ys <;> simp_all [Forall2]
  | _ :: _, [], _ => by
    simp only [Forall2, false_iff, _root_.not_exists, _root_.not_and]
    intro ys h; cases ys <;> simp_all [Forall2]
  | a :: as, x :: xs, h => by
    simp only [Forall2]
    rw [dom_base SP P tr Ms (h a (by simp)), forall2_base as xs (fun b m => h b (by simp [m]))]
    constructor
    · rintro ⟨⟨w, hw, rfl⟩, ys, rfl, hys⟩
      exact ⟨w :: ys, rfl, hw, hys⟩
    · rintro ⟨ys, hx, hys⟩
      cases ys with
      | nil => cases hx
      | cons y ys =>
        simp only [List.map_cons, List.cons.injEq] at hx
        exact ⟨⟨y, hys.1, hx.1⟩, ys, hx.2, hys.2⟩

end

end Vik.Lower
