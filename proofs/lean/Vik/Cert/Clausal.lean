import Vik.Tab

/-!
# Clausal proofs

A checker for the clausal proofs of `cdclt` (`cdclt/Proof.scala`), and its
soundness: every clause a checked proof derives holds under every assignment
that satisfies its input and lemma clauses. The inputs and lemmas are checked
apart, by the checkers of their origins and of the theories.

A step is an input clause, a lemma, or a clause derived by reverse unit
propagation over hints: with every literal of the clause false, each hint in
turn has all its literals false but one, which becomes true, until a hint has
them all false.
-/

namespace Vik.Clausal

/-- A literal: a variable, positive or negated. -/
structure Lit where
  var : Nat
  pos : Bool
  deriving DecidableEq, Repr

def Lit.neg (l : Lit) : Lit := ⟨l.var, !l.pos⟩

abbrev Clause := List Lit

/-- An assignment of the variables. -/
abbrev Assignment := Nat → Bool

def Lit.holds (ρ : Assignment) (l : Lit) : Prop := ρ l.var = l.pos

def Clause.holds (ρ : Assignment) (c : Clause) : Prop := ∃ l ∈ c, l.holds ρ

theorem Lit.neg_neg (l : Lit) : l.neg.neg = l := by
  cases l; simp [Lit.neg]

theorem Lit.holds_neg (ρ : Assignment) (l : Lit) : l.neg.holds ρ ↔ ¬l.holds ρ := by
  cases l with
  | mk v p => cases h : ρ v <;> cases p <;> simp [Lit.holds, Lit.neg, h]

/-! ## Reverse unit propagation -/

/-- One hint under the literals `τ` taken as true: `none` if the hint is
    satisfied or has two open literals (the check fails), `some none` if all its
    literals are false, `some (some u)` if `u` is its one open literal. -/
def unit (τ : List Lit) (h : Clause) : Option (Option Lit) :=
  if h.any (fun l => τ.contains l) then none
  else
    match h.filter (fun l => !τ.contains l.neg) with
    | [] => some none
    | u :: rest => if rest.all (· == u) then some (some u) else none

/-- Follows the hints from the literals `τ`: whether they end in a hint whose
    literals are all false. -/
def propagate (db : List Clause) (τ : List Lit) : List Nat → Bool
  | [] => false
  | i :: hints =>
    match db[i]? with
    | none => false
    | some h =>
      match unit τ h with
      | some none => true
      | some (some u) => propagate db (u :: τ) hints
      | none => false

/-- Whether `c` follows from the clauses `db` by unit propagation over `hints`:
    a tautology does, and otherwise the negations of its literals must lead to a
    hint with all literals false. -/
def rup (db : List Clause) (c : Clause) (hints : List Nat) : Bool :=
  c.any (fun l => c.contains l.neg) || propagate db (c.map Lit.neg) hints

theorem unit_sound {ρ : Assignment} {τ : List Lit} {h : Clause} (hτ : ∀ l ∈ τ, l.holds ρ)
    (hh : h.holds ρ) :
    (unit τ h = some none → False) ∧ (∀ u, unit τ h = some (some u) → u.holds ρ) := by
  obtain ⟨l, hl, hlρ⟩ := hh
  unfold unit
  split
  · simp
  · rename_i hsat
    -- every literal of `h` outside the filter is false under ρ
    have hfalse : ∀ m ∈ h, (!τ.contains m.neg) = false → ¬m.holds ρ := by
      intro m _ hm hmρ
      simp only [Bool.not_eq_false', List.contains_iff_mem] at hm
      have := hτ _ hm
      exact (Lit.holds_neg ρ m).mp this hmρ
    have hl_in : l ∈ h.filter (fun l => !τ.contains l.neg) := by
      rw [List.mem_filter]
      refine ⟨hl, ?_⟩
      cases e : (!τ.contains l.neg)
      · exact absurd hlρ (hfalse l hl e)
      · rfl
    split
    · rename_i he; rw [he] at hl_in; simp at hl_in
    · rename_i u rest he
      rw [he] at hl_in
      split
      · rename_i hall
        refine ⟨by simp, ?_⟩
        intro v hv
        simp only [Option.some.injEq] at hv
        subst hv
        rcases List.mem_cons.mp hl_in with h1 | h1
        · rw [← h1]; exact hlρ
        · have := List.all_eq_true.mp hall l h1
          simp only [beq_iff_eq] at this
          rw [← this]; exact hlρ
      · simp

theorem propagate_sound {ρ : Assignment} {db : List Clause} (hdb : ∀ c ∈ db, c.holds ρ) :
    ∀ (hints : List Nat) (τ : List Lit), (∀ l ∈ τ, l.holds ρ) → propagate db τ hints = true → False := by
  intro hints
  induction hints with
  | nil => intro τ _ h; simp [propagate] at h
  | cons i hints ih =>
    intro τ hτ h
    unfold propagate at h
    split at h
    · simp at h
    · rename_i c hc
      have hcρ : c.holds ρ := hdb c (List.mem_of_getElem? hc)
      have ⟨h1, h2⟩ := unit_sound hτ hcρ
      split at h
      · rename_i he; exact h1 he
      · rename_i u he
        apply ih (u :: τ) _ h
        intro l hl
        rcases List.mem_cons.mp hl with e | e
        · rw [e]; exact h2 u he
        · exact hτ l e
      · simp at h

/-- A clause that passes `rup` holds wherever the clauses `db` hold. -/
theorem rup_sound {ρ : Assignment} {db : List Clause} {c : Clause} {hints : List Nat}
    (hdb : ∀ c ∈ db, c.holds ρ) (h : rup db c hints = true) : c.holds ρ := by
  unfold rup at h
  rcases Bool.or_eq_true_iff.mp h with h | h
  · obtain ⟨l, hl, hneg⟩ := List.any_eq_true.mp h
    rw [List.contains_iff_mem] at hneg
    by_cases hlρ : l.holds ρ
    · exact ⟨l, hl, hlρ⟩
    · exact ⟨l.neg, hneg, (Lit.holds_neg ρ l).mpr hlρ⟩
  · apply Classical.byContradiction
    intro hc
    apply propagate_sound hdb hints (c.map Lit.neg) _ h
    intro l hl
    obtain ⟨m, hm, rfl⟩ := List.mem_map.mp hl
    exact (Lit.holds_neg ρ m).mpr (fun hmρ => hc ⟨m, hm, hmρ⟩)

/-! ## Proofs -/

/-- A step of a proof: an input clause, a lemma of a theory, or a clause derived
    by `rup` from the clauses of earlier steps, by their indices. -/
inductive Step where
  | input (c : Clause)
  | lemma (c : Clause)
  | derived (c : Clause) (hints : List Nat)

def Step.clause : Step → Clause
  | .input c => c
  | .lemma c => c
  | .derived c _ => c

/-- Checks the derived steps, each against the clauses of the steps before it:
    the clauses of the steps if they all pass, `none` otherwise. -/
def check : List Clause → List Step → Option (List Clause)
  | db, [] => some db
  | db, .derived c hints :: rest => if rup db c hints then check (db ++ [c]) rest else none
  | db, s :: rest => check (db ++ [s.clause]) rest

/-- The premises of a proof: its input and lemma steps. -/
def Step.premise : Step → Bool
  | .derived _ _ => false
  | _ => true

theorem check_sound {ρ : Assignment} :
    ∀ (steps : List Step) (db out : List Clause), (∀ c ∈ db, c.holds ρ) →
      (∀ s ∈ steps, s.premise = true → s.clause.holds ρ) →
      check db steps = some out → ∀ c ∈ out, c.holds ρ := by
  intro steps
  induction steps with
  | nil => intro db out hdb _ h; simp only [check, Option.some.injEq] at h; rw [← h]; exact hdb
  | cons s rest ih =>
    intro db out hdb hprem h
    have hrest : ∀ s ∈ rest, s.premise = true → s.clause.holds ρ :=
      fun s hs => hprem s (List.mem_cons_of_mem _ hs)
    have extend : ∀ c, c.holds ρ → ∀ d ∈ db ++ [c], d.holds ρ := by
      intro c hc d hd
      rcases List.mem_append.mp hd with hd | hd
      · exact hdb d hd
      · rw [List.mem_singleton.mp hd]; exact hc
    cases s with
    | derived c hints =>
      simp only [check] at h
      split at h
      · rename_i hr
        exact ih _ out (extend c (rup_sound hdb hr)) hrest h
      · simp at h
    | input c =>
      simp only [check, Step.clause] at h
      exact ih _ out (extend c (hprem _ (List.mem_cons_self ..) rfl)) hrest h
    | lemma c =>
      simp only [check, Step.clause] at h
      exact ih _ out (extend c (hprem _ (List.mem_cons_self ..) rfl)) hrest h

/-- A proof checks with a conclusion: the steps pass, and the conclusion is the
    clause of one of them. -/
def accepts (steps : List Step) (conclusion : Nat) (goal : Clause) : Bool :=
  match check [] steps with
  | some db => db[conclusion]? == some goal
  | none => false

/-- The conclusion of an accepted proof holds under every assignment that
    satisfies its premises. -/
theorem accepts_sound {ρ : Assignment} {steps : List Step} {conclusion : Nat} {goal : Clause}
    (hprem : ∀ s ∈ steps, s.premise = true → s.clause.holds ρ)
    (h : accepts steps conclusion goal = true) : goal.holds ρ := by
  unfold accepts at h
  split at h
  · rename_i db hdb
    have hall := check_sound steps [] db (by simp) hprem hdb
    simp only [beq_iff_eq] at h
    exact hall goal (List.mem_of_getElem? h)
  · simp at h

/-! ## A faster check

`check` looks up each hint in the clauses before its step, a list it extends
at each step: the kernel takes time quadratic in the steps. `checkT` looks
hints up in one tree of all the steps' clauses, bounded by the step's index,
and gives the same answer.
-/

/-- `propagate` over the first `k` clauses of `tab`. -/
def propagateT (tab : List Clause) (k : Nat) (τ : List Lit) : List Nat → Bool
  | [] => false
  | i :: hints =>
    match (if i < k then Tab.at tab i else none) with
    | none => false
    | some h =>
      match unit τ h with
      | some none => true
      | some (some u) => propagateT tab k (u :: τ) hints
      | none => false

theorem propagateT_eq (tab : List Clause) (k : Nat) :
    ∀ (hints : List Nat) (τ : List Lit), propagateT tab k τ hints = propagate (tab.take k) τ hints
  | [], _ => rfl
  | i :: hints, τ => by
    have e : (if i < k then Tab.at tab i else none) = (tab.take k)[i]? := by
      rw [List.getElem?_take, Tab.at_eq]
    simp only [propagateT, propagate, e]
    split <;> try rfl
    split <;> try rfl
    exact propagateT_eq tab k hints _

def rupT (tab : List Clause) (k : Nat) (c : Clause) (hints : List Nat) : Bool :=
  c.any (fun l => c.contains l.neg) || propagateT tab k (c.map Lit.neg) hints

/-- Whether the steps from the `k`-th on pass, `tab` the clauses of all steps. -/
def checkT (tab : List Clause) : Nat → List Step → Bool
  | _, [] => true
  | k, .derived c hints :: rest => rupT tab k c hints && checkT tab (k + 1) rest
  | k, _ :: rest => checkT tab (k + 1) rest

theorem checkT_append (tab : List Clause) :
    ∀ (l₁ l₂ : List Step) (k : Nat), checkT tab k (l₁ ++ l₂) = (checkT tab k l₁ && checkT tab (k + l₁.length) l₂)
  | [], l₂, k => by simp [checkT]
  | .derived c hints :: l₁, l₂, k => by
    simp only [List.cons_append, checkT, checkT_append tab l₁ l₂ (k + 1), List.length_cons, Bool.and_assoc]
    rw [show k + 1 + l₁.length = k + (l₁.length + 1) by omega]
  | .input c :: l₁, l₂, k => by
    simp only [List.cons_append, checkT, checkT_append tab l₁ l₂ (k + 1), List.length_cons]
    rw [show k + 1 + l₁.length = k + (l₁.length + 1) by omega]
  | .lemma c :: l₁, l₂, k => by
    simp only [List.cons_append, checkT, checkT_append tab l₁ l₂ (k + 1), List.length_cons]
    rw [show k + 1 + l₁.length = k + (l₁.length + 1) by omega]

theorem checkT_sound (tab : List Clause) :
    ∀ (rest : List Step) (k : Nat), tab.drop k = rest.map Step.clause →
      checkT tab k rest = true → check (tab.take k) rest = some tab
  | [], k, hd, _ => by
    simp only [List.map_nil, List.drop_eq_nil_iff] at hd
    simp only [check, List.take_of_length_le hd]
  | s :: rest, k, hd, h => by
    have hk : tab[k]? = some s.clause := by
      have : (tab.drop k)[0]? = some s.clause := by rw [hd]; rfl
      rwa [List.getElem?_drop, Nat.add_zero] at this
    have ht : tab.take (k + 1) = tab.take k ++ [s.clause] := by
      rw [List.take_add_one, hk]; rfl
    have hd' : tab.drop (k + 1) = rest.map Step.clause := by
      rw [← List.drop_drop, hd]; rfl
    cases s with
    | derived c hints =>
      simp only [checkT, Bool.and_eq_true] at h
      have hr : rup (tab.take k) c hints = true := by
        unfold rup; rw [← propagateT_eq]; exact h.1
      simp only [check, hr, ↓reduceIte]
      simp only [Step.clause] at ht
      rw [← ht]; exact checkT_sound tab rest (k + 1) hd' h.2
    | input c =>
      simp only [checkT] at h
      simp only [check, Step.clause]
      simp only [Step.clause] at ht
      rw [← ht]; exact checkT_sound tab rest (k + 1) hd' h
    | lemma c =>
      simp only [checkT] at h
      simp only [check, Step.clause]
      simp only [Step.clause] at ht
      rw [← ht]; exact checkT_sound tab rest (k + 1) hd' h

theorem check_of_checkT {steps : List Step} (h : checkT (steps.map Step.clause) 0 steps = true) :
    check [] steps = some (steps.map Step.clause) := by
  have := checkT_sound (steps.map Step.clause) steps 0 (by simp) h
  simpa using this

/-- In particular, an accepted proof of the empty clause leaves no assignment
    that satisfies its premises. -/
theorem refutes {steps : List Step} {conclusion : Nat} (h : accepts steps conclusion [] = true)
    (ρ : Assignment) : ¬ ∀ s ∈ steps, s.premise = true → s.clause.holds ρ := by
  intro hprem
  obtain ⟨_, hl, _⟩ := accepts_sound hprem h
  simp at hl

end Vik.Clausal
