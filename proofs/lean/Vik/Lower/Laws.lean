import Vik.Lower.Model

/-!
# The lowered model satisfies the laws

What the checks of the signature say, and that the model of
`Vik/Lower/Model.lean` satisfies the laws of the lowered problem
(`Problem.Laws`) when the surface model satisfies those of the script.
-/

set_option linter.unusedSectionVars false

namespace Vik.Lower

open Lean.Grind Vik.Real Vik.Low Vik.Surf

/-! ## What the checks say -/

section
variable {SP : SProblem} {P : Problem} {tr : Trace}

structure SortsSpec (SP : SProblem) : Prop where
  noBv : ∀ (s w : Nat), SP.sig.sorts[s]? ≠ some (SSort.bv w)
  fn : ∀ (s : Nat) (as : List Nat) (r : Nat), SP.sig.sorts[s]? = some (SSort.fn as r) → (∀ a ∈ as, a < SP.sig.sorts.length) ∧ r < SP.sig.sorts.length
  array : ∀ (s i e : Nat), SP.sig.sorts[s]? = some (SSort.array i e) → i < SP.sig.sorts.length ∧ e < SP.sig.sorts.length
  decls : ∀ (k s : Nat), SP.sig.decls[k]? = some s → s < SP.sig.sorts.length
  sortOf : ∀ (i s : Nat), SP.sortOf[i]? = some s → s < SP.sig.sorts.length
  dt : ∀ d cs, (d, cs) ∈ SP.datatypes → d < SP.sig.sorts.length ∧
    ∀ C T Ss, (C, T, Ss) ∈ cs → C < SP.sig.decls.length ∧ T < SP.sig.decls.length ∧
      ∀ S ∈ Ss, S < SP.sig.decls.length

theorem sortsOk_spec (h : SP.sortsOk = true) : SortsSpec SP := by
  simp only [SProblem.sortsOk, Bool.and_eq_true, List.all_eq_true, decide_eq_true_eq] at h
  obtain ⟨⟨⟨hs, hd⟩, ho⟩, hdt⟩ := h
  have get_mem : ∀ {α : Type} (l : List α) (i : Nat) (x : α), l[i]? = some x → x ∈ l :=
    fun l i x h => List.mem_of_getElem? h
  refine ⟨?_, ?_, ?_, ?_, ?_, ?_⟩
  · intro s w e; have := hs _ (get_mem _ _ _ e); simp at this
  · intro s as r e; have := hs _ (get_mem _ _ _ e); simpa using this
  · intro s i e' e; have := hs _ (get_mem _ _ _ e); simpa using this
  · intro k s e; exact hd _ (get_mem _ _ _ e)
  · intro i s e; exact ho _ (get_mem _ _ _ e)
  · intro d cs m
    have := hdt _ m
    exact ⟨this.1, fun C T Ss mc => by
      have := this.2 _ mc
      exact ⟨this.1.1, this.1.2, this.2⟩⟩

structure SigSpec (SP : SProblem) (P : Problem) (tr : Trace) : Prop where
  sorts : ∀ s < SP.sig.sorts.length, P.sig.sorts[s]? = (SP.sig.sorts[s]?).map lowerSort
  decls : ∀ k < SP.sig.decls.length, P.sig.decls[k]? = SP.sig.decls[k]?
  declsLen : P.sig.decls.length = SP.sig.decls.length + tr.mints.length
  extra : ∀ s d, SP.sig.sorts.length ≤ s → P.sig.sorts[s]? = some d →
    ∃ arr idx el, d = .fn [arr, idx, el] arr ∧ SP.sig.sorts[arr]? = some (.array idx el)
  ite : ∀ m i, tr.mints[m]? = some (.ite i) →
    (∃ c a b, SP.nodes[i]? = some (.ite c a b)) ∧ SP.isBool i = false ∧
      P.sig.decls[SP.sig.decls.length + m]? = some (SP.sort i)
  store : ∀ m s, tr.mints[m]? = some (.store s) →
    ∃ idx el ds, SP.sig.sorts[s]? = some (.array idx el) ∧ P.sig.decls[SP.sig.decls.length + m]? = some ds ∧
      SP.sig.sorts.length ≤ ds ∧ P.sig.sorts[ds]? = some (.fn [s, idx, el] s) ∧
      ∀ m' s', m' ≠ m → tr.mints[m']? = some (.store s') → P.sig.decls[SP.sig.decls.length + m']? ≠ some ds
  stores : ∀ n ∈ P.stores, SP.sig.decls.length ≤ n ∧ ∃ s, tr.mints[n - SP.sig.decls.length]? = some (.store s)
  datatypes : P.datatypes = SP.datatypes
  bvIndex : P.bvIndex = []

theorem sigOk_spec (h : tr.sigOk SP P = true) : SigSpec SP P tr := by
  simp only [Trace.sigOk, Bool.and_eq_true, beq_iff_eq, List.all_eq_true, List.mem_range] at h
  obtain ⟨⟨⟨⟨⟨⟨⟨hs, hd⟩, hl⟩, hx⟩, hm⟩, hst⟩, hdt⟩, hbv⟩ := h
  refine ⟨?_, ?_, hl, ?_, ?_, ?_, ?_, hdt, hbv⟩
  · intro s hs'
    rw [← List.getElem?_map, ← hs, List.getElem?_take_of_lt hs']
  · intro k hk
    conv => rhs; rw [← hd]
    rw [List.getElem?_take_of_lt hk]
  · intro s d hle he
    have hlt : s < P.sig.sorts.length := by
      rcases Nat.lt_or_ge s P.sig.sorts.length with h' | h'
      · exact h'
      · rw [List.getElem?_eq_none h'] at he; cases he
    have := hx s hlt
    simp only [Bool.or_eq_true, decide_eq_true_eq] at this
    rcases this with h' | h'
    · omega
    · rw [he] at h'
      split at h'
      · rename_i arr idx el arr' heq
        cases heq
        simp only [Bool.and_eq_true, beq_iff_eq] at h'
        exact ⟨arr, idx, el, by rw [h'.1], h'.2⟩
      · cases h'
  · intro m i hmi
    have hlt : m < tr.mints.length := by
      rcases Nat.lt_or_ge m tr.mints.length with h' | h'
      · exact h'
      · rw [List.getElem?_eq_none h'] at hmi; cases hmi
    have := hm m hlt
    rw [hmi] at this
    simp only [Bool.and_eq_true, Bool.not_eq_true', beq_iff_eq] at this
    obtain ⟨⟨hn, hb⟩, hdc⟩ := this
    refine ⟨?_, hb, hdc⟩
    split at hn
    · rename_i c a b he; exact ⟨c, a, b, he⟩
    · cases hn
  · intro m s hms
    have hlt : m < tr.mints.length := by
      rcases Nat.lt_or_ge m tr.mints.length with h' | h'
      · exact h'
      · rw [List.getElem?_eq_none h'] at hms; cases hms
    have := hm m hlt
    rw [hms] at this
    simp only at this
    split at this
    · rename_i idx el ds hs1 hd1
      simp only [Bool.and_eq_true, decide_eq_true_eq, beq_iff_eq, List.all_eq_true, List.mem_range,
        Bool.or_eq_true] at this
      obtain ⟨⟨hle, hds⟩, honly⟩ := this
      refine ⟨idx, el, ds, hs1, hd1, hle, hds, fun m' s' hne hm' => ?_⟩
      have hlt' : m' < tr.mints.length := by
        rcases Nat.lt_or_ge m' tr.mints.length with h' | h'
        · exact h'
        · rw [List.getElem?_eq_none h'] at hm'; cases hm'
      rcases honly m' hlt' with h' | h'
      · exact absurd h' hne
      · rw [hm'] at h'; simpa using h'
    · cases this
  · intro n hn
    have := hst n hn
    simp only [decide_eq_true_eq] at this
    refine ⟨this.1, ?_⟩
    split at this
    · rename_i s he; exact ⟨s, he⟩
    · exact absurd this.2 (by simp)

end


/-! ## The laws -/

variable {R U : Type} [Field R] [LE R] [LT R] [Std.IsLinearOrder R] [Std.LawfulOrderLT R]
  [OrderedRing R] [IsCharP R 0]

attribute [local instance low] Lean.Grind.Ring.intCast

theorem lt_of_get {α : Type} {l : List α} {i : Nat} {x : α} (h : l[i]? = some x) : i < l.length := by
  rcases Nat.lt_or_ge i l.length with h' | h'
  · exact h'
  · rw [List.getElem?_eq_none h'] at h; cases h

theorem forall2_imp {α β : Type} {p q : α → β → Prop} (h : ∀ a b, p a b → q a b) :
    ∀ (as : List α) (bs : List β), Forall2 p as bs → Forall2 q as bs
  | [], [], _ => trivial
  | a :: as, b :: bs, hp => ⟨h a b hp.1, forall2_imp h as bs hp.2⟩
  | [], _ :: _, hp => hp.elim
  | _ :: _, [], hp => hp.elim

section
variable {SP : SProblem} {P : Problem} {tr : Trace} {Ms : Surf.Model R U}
  (hS : SortsSpec SP) (hG : SigSpec SP P tr)

include hG in
/-- A lowered sort that is not a function is one of the script. -/
theorem base_of_not_fn {s : Nat} {d : SortDef} (h : P.sig.sorts[s]? = some d) (hd : ∀ as r, d ≠ .fn as r) :
    s < SP.sig.sorts.length := by
  rcases Nat.lt_or_ge s SP.sig.sorts.length with h' | h'
  · exact h'
  · obtain ⟨arr, idx, el, rfl, _⟩ := hG.extra s d h' h
    exact absurd rfl (hd _ _)

include hG in
theorem surf_sort_of {s : Nat} {d : SortDef} (hs : s < SP.sig.sorts.length) (h : P.sig.sorts[s]? = some d) :
    ∃ x, SP.sig.sorts[s]? = some x ∧ lowerSort x = d := by
  rw [hG.sorts s hs] at h
  cases e : SP.sig.sorts[s]? with
  | none => rw [e] at h; cases h
  | some x => rw [e] at h; cases h; exact ⟨x, rfl, rfl⟩

include hS hG in
theorem argSorts_eq {C : Nat} (hC : C < SP.sig.decls.length) : P.argSorts C = SP.argSorts C := by
  unfold Problem.argSorts SProblem.argSorts
  rw [hG.decls C hC]
  cases e : SP.sig.decls[C]? with
  | none => rfl
  | some s =>
    simp only
    rw [hG.sorts s (hS.decls C s e)]
    generalize Option.map lowerSort SP.sig.sorts[s]? = o
    rcases o with _ | d
    · rfl
    · cases d <;> rfl

include hS in
theorem argSorts_lt (C : Nat) : ∀ a ∈ SP.argSorts C, a < SP.sig.sorts.length := by
  unfold SProblem.argSorts
  cases e : SP.sig.decls[C]? with
  | none => simp
  | some s =>
    simp only
    cases e' : SP.sig.sorts[s]? with
    | none => simp
    | some x =>
      cases x with
      | fn as r => exact (hS.fn s as r e').1
      | array i el => simp [lowerSort]; exact (hS.array s i el e').1
      | _ => simp [lowerSort]

theorem ctorVal_enc {C : Nat} (hC : C < SP.sig.decls.length) (xs : List (Val R U)) :
    ctorVal (lowModel SP P tr Ms) C (xs.map enc) = enc (ctorVal (SModel.low Ms) C xs) := by
  unfold ctorVal
  simp only [List.isEmpty_map]
  split
  · exact const_base SP P tr Ms hC
  · rw [const_base SP P tr Ms hC, app_enc_enc]; rfl

include hS hG in
/-- The arguments of a surface symbol, as lowered values. -/
theorem forall2_args {C : Nat} (hC : C < SP.sig.decls.length) (xs : List (Val R (U ⊕ Nat)))
    (h : Forall2 (lowModel SP P tr Ms).dom (P.argSorts C) xs) :
    ∃ ys, xs = ys.map enc ∧ Forall2 Ms.dom (SP.argSorts C) ys := by
  rw [argSorts_eq hS hG hC] at h
  exact (forall2_base SP P tr Ms _ xs (argSorts_lt hS C)).mp h

end


section
variable {SP : SProblem} {P : Problem} {tr : Trace} {Ms : Surf.Model R U} {size : Val R U → Nat}
  (hS : SortsSpec SP) (hG : SigSpec SP P tr) (hL : SP.Laws Ms size)
  (hw : Surf.wellFormed SP.nodes = true) (ht : Surf.typed SP.sig SP.nodes SP.sortOf = true)
include hS hG hL

/-- A minted symbol is a store or the value of an `ite`. -/
theorem mint_of {k : Nat} (hk : SP.sig.decls.length ≤ k) (s : Nat) (h : P.sig.decls[k]? = some s) :
    ∃ m, k = SP.sig.decls.length + m ∧ ∃ x, tr.mints[m]? = some x := by
  have := lt_of_get h
  rw [hG.declsLen] at this
  refine ⟨k - SP.sig.decls.length, by omega, ?_⟩
  exact ⟨_, List.getElem?_eq_getElem (by omega)⟩

/-- A lowered value of a sort of the script is a surface one. -/
theorem dom_enc {s : Nat} (hs : s < SP.sig.sorts.length) {v : Val R (U ⊕ Nat)}
    (h : (lowModel SP P tr Ms).dom s v) : ∃ w, Ms.dom s w ∧ v = enc w :=
  (dom_base SP P tr Ms hs v).mp h

include hw ht in
theorem lowModel_sound : (lowModel SP P tr Ms).Sound P.sig P.bvIndex := by
  have hM := hL.sound
  refine ⟨?_, ?_, ?_, ?_, ?_⟩
  · intro s v h
    have hs := base_of_not_fn hG h (by intro _ _ e; cases e)
    obtain ⟨x, hx, ex⟩ := surf_sort_of hG hs h
    cases x <;> simp [lowerSort] at ex
    rw [dom_base SP P tr Ms hs]
    constructor
    · rintro ⟨w, hw', rfl⟩
      obtain ⟨b, rfl⟩ := (hM.bool s w hx).mp hw'
      exact ⟨b, rfl⟩
    · rintro ⟨b, rfl⟩
      exact ⟨.bool b, (hM.bool s _ hx).mpr ⟨b, rfl⟩, rfl⟩
  · intro s v h
    have hs := base_of_not_fn hG h (by intro _ _ e; cases e)
    obtain ⟨x, hx, ex⟩ := surf_sort_of hG hs h
    cases x with
    | bv w => exact absurd hx (hS.noBv s w)
    | int =>
      rw [dom_base SP P tr Ms hs]
      constructor
      · rintro ⟨w, hw', rfl⟩
        obtain ⟨k, rfl⟩ := (hM.int s w hx).mp hw'
        exact ⟨k, rfl⟩
      · rintro ⟨k, rfl⟩
        exact ⟨.num (k : R), (hM.int s _ hx).mpr ⟨k, rfl⟩, rfl⟩
    | _ => simp [lowerSort] at ex
  · intro s v h
    have hs := base_of_not_fn hG h (by intro _ _ e; cases e)
    obtain ⟨x, hx, ex⟩ := surf_sort_of hG hs h
    cases x <;> simp [lowerSort] at ex
    rw [dom_base SP P tr Ms hs]
    constructor
    · rintro ⟨w, hw', rfl⟩
      obtain ⟨r, rfl⟩ := (hM.real s w hx).mp hw'
      exact ⟨r, rfl⟩
    · rintro ⟨r, rfl⟩
      exact ⟨.num r, (hM.real s _ hx).mpr ⟨r, rfl⟩, rfl⟩
  · intro s as r f xs h hf hxs'
    have hxs := forall2_imp (fun _ _ h => h.1) _ _ hxs'
    rcases Nat.lt_or_ge s SP.sig.sorts.length with hs | hs
    · obtain ⟨x, hx, ex⟩ := surf_sort_of hG hs h
      obtain ⟨f0, hf0, rfl⟩ := dom_enc hS hG hL hs hf
      cases x with
      | fn as' r' =>
        simp only [lowerSort, SortDef.fn.injEq] at ex
        obtain ⟨h1, h2⟩ := ex
        subst h1; subst h2
        obtain ⟨has, hr⟩ := hS.fn s as' r' hx
        obtain ⟨ys, rfl, hys⟩ := (forall2_base SP P tr Ms as' xs has).mp hxs
        rw [app_enc_enc, dom_base SP P tr Ms hr]
        exact ⟨_, hM.fn s as' r' f0 ys hx hf0 hys, rfl⟩
      | array i e =>
        simp only [lowerSort, SortDef.fn.injEq] at ex
        obtain ⟨rfl, rfl⟩ := ex
        obtain ⟨hi, he⟩ := hS.array s i e hx
        obtain ⟨ys, rfl, hys⟩ := (forall2_base SP P tr Ms [i] xs (by simpa using hi)).mp hxs
        cases ys with
        | nil => exact hys.elim
        | cons y ys =>
          cases ys with
          | cons _ _ => exact hys.2.elim
          | nil =>
            rw [app_enc_enc, dom_base SP P tr Ms he]
            exact ⟨_, hM.select s i e f0 y hx hf0 hys.1, rfl⟩
      | _ => simp [lowerSort] at ex
    · obtain ⟨arr, idx, el, hd, harr⟩ := hG.extra s _ hs h
      simp only [SortDef.fn.injEq] at hd
      obtain ⟨h1, h2⟩ := hd
      subst h1; subst h2
      have hf' := hf
      simp only [lowModel, Nat.not_lt.mpr hs, if_false] at hf'
      obtain ⟨k, rfl, _, _⟩ := hf'
      obtain ⟨hi, he⟩ := hS.array r idx el harr
      have ha := lt_of_get harr
      obtain ⟨ys, rfl, hys⟩ := (forall2_base SP P tr Ms [r, idx, el] xs (by simp; omega)).mp hxs
      match ys, hys with
      | [a, i, v], hys =>
        rw [List.map_cons, List.map_cons, List.map_cons, List.map_nil, app_store, dom_base SP P tr Ms ha]
        exact ⟨_, (hM.store r idx el a i v harr hys.1 hys.2.1 hys.2.2.1).1, rfl⟩
  · intro k s h
    rcases Nat.lt_or_ge k SP.sig.decls.length with hk | hk
    · rw [hG.decls k hk] at h
      have hs := hS.decls k s h
      rw [const_base SP P tr Ms hk, dom_base SP P tr Ms hs]
      exact ⟨_, hM.const k s h, rfl⟩
    · obtain ⟨m, rfl, x, hx⟩ := mint_of hS hG hL hk s h
      have hm : SP.sig.decls.length + m - SP.sig.decls.length = m := by omega
      cases x with
      | ite i =>
        obtain ⟨⟨c, a, b, hi⟩, _, hd⟩ := hG.ite m i hx
        rw [h] at hd; cases hd
        rw [const_ite SP P tr Ms hk (by rw [hm]; exact hx)]
        have hil := lt_of_get hi
        have hso : SP.sortOf.length = SP.nodes.length := by
          simp only [Surf.typed, Bool.and_eq_true, beq_iff_eq] at ht; exact ht.1
        have hsl : SP.sort i < SP.sig.sorts.length := by
          unfold SProblem.sort
          rw [List.getElem?_eq_getElem (by omega)]
          exact hS.sortOf i _ (List.getElem?_eq_getElem (by omega))
        rw [dom_base SP P tr Ms hsl]
        exact ⟨_, Surf.val_dom hM hw ht i hil, rfl⟩
      | store s0 =>
        obtain ⟨idx, el, ds, _, hd, hle, _, _⟩ := hG.store m s0 hx
        rw [h] at hd; cases hd
        rw [const_store SP P tr Ms hk (by rw [hm]; exact hx)]
        simp only [lowModel, Nat.not_lt.mpr hle, if_false]
        exact ⟨_, rfl, ⟨hk, s0, by rw [hm]; exact hx⟩, h⟩

include hw ht in
/-- The lowered model satisfies the laws of the lowered problem. -/
theorem lowModel_laws : P.Laws (lowModel SP P tr Ms) (fun v => size (dec v)) := by
  have hM := hL.sound
  have dtC : ∀ d cs C T Ss, (d, cs) ∈ P.datatypes → (C, T, Ss) ∈ cs →
      (d, cs) ∈ SP.datatypes ∧ d < SP.sig.sorts.length ∧ C < SP.sig.decls.length ∧ T < SP.sig.decls.length ∧
        ∀ S ∈ Ss, S < SP.sig.decls.length := by
    intro d cs C T Ss hd hc
    rw [hG.datatypes] at hd
    have := hS.dt d cs hd
    exact ⟨hd, this.1, this.2 C T Ss hc⟩
  have args : ∀ {C : Nat}, C < SP.sig.decls.length → ∀ xs,
      Forall2 (fun a x => (lowModel SP P tr Ms).dom a x ∧ inRange P.bvIndex a x) (P.argSorts C) xs →
      ∃ ys, xs = ys.map enc ∧ Forall2 Ms.dom (SP.argSorts C) ys :=
    fun hC xs h => forall2_args hS hG hC xs (forall2_imp (fun _ _ h => h.1) _ _ h)
  refine ⟨lowModel_sound hS hG hL hw ht, ?_, ?_, ?_, ?_, ?_, ?_, ?_, ?_, ?_, ?_⟩
  · -- sel
    intro d cs C T Ss hd hc xs i S x hxs hS' hx
    obtain ⟨hd', _, hC, _, hSs⟩ := dtC d cs C T Ss hd hc
    obtain ⟨ys, rfl, hys⟩ := args hC xs hxs
    have hSl := hSs S (List.mem_of_getElem? hS')
    rw [List.getElem?_map] at hx
    cases e : ys[i]? with
    | none => rw [e] at hx; cases hx
    | some y =>
      rw [e] at hx; cases hx
      rw [ctorVal_enc hC, const_base SP P tr Ms hSl, ← List.map_singleton (f := enc), app_enc_enc,
        hL.sel d cs C T Ss hd' hc ys i S y hys hS' e]
  · -- test
    intro d cs C T Ss hd hc xs hxs
    obtain ⟨hd', _, hC, hT, _⟩ := dtC d cs C T Ss hd hc
    obtain ⟨ys, rfl, hys⟩ := args hC xs hxs
    rw [ctorVal_enc hC, const_base SP P tr Ms hT, ← List.map_singleton (f := enc), app_enc_enc,
      hL.test d cs C T Ss hd' hc ys hys]; rfl
  · -- testOther
    intro d cs C T Ss C' T' Ss' hd hc hc' hne ys hys
    obtain ⟨hd', _, hC, hT, _⟩ := dtC d cs C T Ss hd hc
    obtain ⟨_, _, hC', _, _⟩ := dtC d cs C' T' Ss' hd hc'
    obtain ⟨zs, rfl, hzs⟩ := args hC' ys hys
    rw [ctorVal_enc hC', const_base SP P tr Ms hT, ← List.map_singleton (f := enc), app_enc_enc]
    intro e
    exact hL.testOther d cs C T Ss C' T' Ss' hd' hc hc' hne zs hzs (enc_inj e)
  · -- inj
    intro d cs C T Ss hd hc xs ys hxs hys e
    obtain ⟨hd', _, hC, _, _⟩ := dtC d cs C T Ss hd hc
    obtain ⟨xs0, rfl, hxs0⟩ := args hC xs hxs
    obtain ⟨ys0, rfl, hys0⟩ := args hC ys hys
    rw [ctorVal_enc hC, ctorVal_enc hC] at e
    rw [hL.inj d cs C T Ss hd' hc xs0 ys0 hxs0 hys0 (enc_inj e)]
  · -- distinct
    intro d cs C T Ss C' T' Ss' hd hc hc' hne xs ys hxs hys e
    obtain ⟨hd', _, hC, _, _⟩ := dtC d cs C T Ss hd hc
    obtain ⟨_, _, hC', _, _⟩ := dtC d cs C' T' Ss' hd hc'
    obtain ⟨xs0, rfl, hxs0⟩ := args hC xs hxs
    obtain ⟨ys0, rfl, hys0⟩ := args hC' ys hys
    rw [ctorVal_enc hC, ctorVal_enc hC'] at e
    exact hL.distinct d cs C T Ss C' T' Ss' hd' hc hc' hne xs0 ys0 hxs0 hys0 (enc_inj e)
  · -- expand
    intro d cs C T Ss hd hc v hv ht'
    obtain ⟨hd', hdl, hC, hT, hSs⟩ := dtC d cs C T Ss hd hc
    obtain ⟨v0, hv0, rfl⟩ := dom_enc hS hG hL hdl hv
    rw [const_base SP P tr Ms hT, ← List.map_singleton (f := enc), app_enc_enc] at ht'
    have e := hL.expand d cs C T Ss hd' hc v0 hv0 (enc_inj ht')
    have hmap : (Ss.map fun S => (lowModel SP P tr Ms).app ((lowModel SP P tr Ms).const S) [enc v0]) =
        (Ss.map fun S => Ms.app (Ms.const S) [v0]).map enc := by
      rw [List.map_map]
      apply List.map_congr_left
      intro S m
      simp only [Function.comp_apply]
      rw [const_base SP P tr Ms (hSs S m), ← List.map_singleton (f := enc), app_enc_enc]
    rw [hmap, ctorVal_enc hC, ← e]
  · -- smaller
    intro d cs C T Ss hd hc xs x hxs hx
    obtain ⟨hd', _, hC, _, _⟩ := dtC d cs C T Ss hd hc
    obtain ⟨xs0, rfl, hxs0⟩ := args hC xs hxs
    obtain ⟨x0, hx0, rfl⟩ := List.mem_map.mp hx
    simp only [dec_enc]
    rw [ctorVal_enc hC, dec_enc]
    exact hL.smaller d cs C T Ss hd' hc xs0 x0 hxs0 hx0
  · -- exhaust
    intro d cs hd v hv
    rw [hG.datatypes] at hd
    have hdl := (hS.dt d cs hd).1
    obtain ⟨v0, hv0, rfl⟩ := dom_enc hS hG hL hdl hv
    obtain ⟨ct, hct, e⟩ := hL.exhaust d cs hd v0 hv0
    refine ⟨ct, hct, ?_⟩
    obtain ⟨C, T, Ss⟩ := ct
    have hT := ((hS.dt d cs hd).2 C T Ss hct).2.1
    rw [const_base SP P tr Ms hT, ← List.map_singleton (f := enc), app_enc_enc, e]; rfl
  · -- store
    intro n hn s arr idx el hs hsort harr a i v j ha hi hv hj _ _
    obtain ⟨hle, s0, hm⟩ := hG.stores n hn
    obtain ⟨idx0, el0, ds, hs0, hd, hdl, hds, _⟩ := hG.store _ s0 hm
    have hk : SP.sig.decls.length + (n - SP.sig.decls.length) = n := by omega
    rw [hk, hs] at hd; cases hd
    rw [hsort] at hds
    simp only [Option.some.injEq, SortDef.fn.injEq, List.cons.injEq, and_true] at hds
    obtain ⟨⟨rfl, rfl, rfl⟩, -⟩ := hds
    have hal := lt_of_get hs0
    obtain ⟨hil, hel⟩ := hS.array _ _ _ hs0
    obtain ⟨a0, ha0, rfl⟩ := dom_enc hS hG hL hal ha
    obtain ⟨i0, hi0, rfl⟩ := dom_enc hS hG hL hil hi
    obtain ⟨v0, hv0, rfl⟩ := dom_enc hS hG hL hel hv
    obtain ⟨j0, hj0, rfl⟩ := dom_enc hS hG hL hil hj
    rw [const_store SP P tr Ms hle hm, app_store, ← List.map_singleton (f := enc), app_enc_enc, app_enc_enc]
    have := (hM.store _ _ _ a0 i0 v0 hs0 ha0 hi0 hv0).2 j0 hj0
    refine ⟨fun e => ?_, fun e => ?_⟩
    · rw [this.1 (enc_inj e)]
    · rw [this.2 (fun e' => e (by rw [e']))]
  · -- ext
    intro s as r f g hs hf hg hfg
    rcases Nat.lt_or_ge s SP.sig.sorts.length with hsl | hsl
    · obtain ⟨x, hx, ex⟩ := surf_sort_of hG hsl hs
      obtain ⟨f0, hf0, rfl⟩ := dom_enc hS hG hL hsl hf
      obtain ⟨g0, hg0, rfl⟩ := dom_enc hS hG hL hsl hg
      have has : (∀ a ∈ as, a < SP.sig.sorts.length) ∧
          (SP.sig.sorts[s]? = some (.fn as r) ∨ ∃ i, as = [i] ∧ SP.sig.sorts[s]? = some (.array i r)) := by
        cases x with
        | fn as' r' =>
          simp only [lowerSort, SortDef.fn.injEq] at ex
          obtain ⟨h1, h2⟩ := ex; subst h1; subst h2
          exact ⟨(hS.fn s _ _ hx).1, .inl hx⟩
        | array i e =>
          simp only [lowerSort, SortDef.fn.injEq] at ex
          obtain ⟨h1, h2⟩ := ex; subst h1; subst h2
          exact ⟨by simpa using (hS.array s i e hx).1, .inr ⟨i, rfl, hx⟩⟩
        | _ => simp [lowerSort] at ex
      congr 1
      apply hM.ext s as r f0 g0 has.2 hf0 hg0
      intro ys hys
      apply enc_inj
      rw [← app_enc_enc SP P tr Ms, ← app_enc_enc SP P tr Ms]
      apply hfg
      have h2 := (forall2_base SP P tr Ms as (ys.map enc) has.1).mpr ⟨ys, rfl, hys⟩
      refine forall2_imp (fun a x hx => ⟨hx, ?_⟩) _ _ h2
      intro w hw'
      rw [hG.bvIndex] at hw'; cases hw'
    · have hf' := hf
      have hg' := hg
      simp only [lowModel, Nat.not_lt.mpr hsl, if_false] at hf' hg'
      obtain ⟨k, rfl, ⟨hk, s1, hm1⟩, hd1⟩ := hf'
      obtain ⟨k', rfl, ⟨hk', s2, hm2⟩, hd2⟩ := hg'
      by_cases e : k = k'
      · rw [e]
      · exfalso
        obtain ⟨_, _, ds, _, hd, _, _, honly⟩ := hG.store _ s1 hm1
        have hk1 : SP.sig.decls.length + (k - SP.sig.decls.length) = k := by omega
        rw [hk1, hd1] at hd; cases hd
        apply honly (k' - SP.sig.decls.length) s2 (by omega) hm2
        have hk2 : SP.sig.decls.length + (k' - SP.sig.decls.length) = k' := by omega
        rw [hk2, hd2]

end

end Vik.Lower
