import Vik.Lower.Corr

/-!
# Lowered nodes take the values of their surface nodes

`expect_sound`: the shape `expect` gives a surface node describes, in the
lowered model, the value of the node; so with `fits_sound`, each lowered node
of the trace takes the value of its surface node (`corr`).
-/

set_option linter.unusedSectionVars false

namespace Vik.Lower

open Lean.Grind Vik.Real Vik.Low Vik.Surf

variable {R U : Type} [Field R] [LE R] [LT R] [Std.IsLinearOrder R] [Std.LawfulOrderLT R]
  [OrderedRing R] [IsCharP R 0]

attribute [local instance low] Lean.Grind.Ring.intCast

@[simp] theorem surf_get_values (I : Surf.Interp R U) (nodes : List SNode) (a : Nat) :
    Low.get (Surf.values I nodes) a = Surf.val I nodes a := rfl

section
variable {SP : SProblem} {P : Problem} {tr : Trace} {Ms : Surf.Model R U}
  (hM : Ms.Sound SP.sig) (hw : Surf.wellFormed SP.nodes = true) (ht : Surf.typed SP.sig SP.nodes SP.sortOf = true)

set_option quotPrecheck false in
local notation "ρ" => Low.val (lowModel SP P tr Ms).toInterp P.nodes
local notation "vs" => Surf.val Ms.toInterp SP.nodes
set_option quotPrecheck false in
local notation "MI" => (lowModel SP P tr Ms).toInterp

include hM hw ht

theorem dom_at {a : Nat} (ha : a < SP.nodes.length) : Ms.dom (SP.sort a) (vs a) :=
  Surf.val_dom hM hw ht a ha

theorem v_bool {a : Nat} (ha : a < SP.nodes.length) (hb : SP.isBool a = true) : ∃ b, vs a = .bool b := by
  simp only [SProblem.isBool, SSig.is, beq_iff_eq] at hb
  exact (hM.bool _ _ hb).mp (dom_at hM hw ht ha)

theorem v_num {a : Nat} (ha : a < SP.nodes.length) (hb : SP.isNum a = true) : ∃ r, vs a = .num r :=
  Surf.dom_num_of_numeric hM hb (dom_at hM hw ht ha)

theorem v_int {a : Nat} (ha : a < SP.nodes.length) (hb : SP.isInt a = true) : ∃ k : Int, vs a = .num (k : R) := by
  simp only [SProblem.isInt, SSig.is, beq_iff_eq] at hb
  exact (hM.int _ _ hb).mp (dom_at hM hw ht ha)

theorem leaf_ev {a : Nat} (ha : a < SP.nodes.length) (e : ρ (tr.at a) = enc (vs a)) :
    (leaf SP tr a).ev MI ρ = enc (vs a) := by
  unfold leaf
  split
  · rename_i hb
    obtain ⟨b, hb'⟩ := v_bool hM hw ht ha hb
    simp only [Shape.ev, e, hb', enc_bool, truth_bool]
  · exact e

omit hM hw ht in
theorem form_ev {a : Nat} (e : ρ (tr.at a) = enc (vs a)) : (form tr a).ev MI ρ = .bool (vs a).truth := by
  simp only [form, Shape.ev, e, truth_enc]

omit hM hw ht in
theorem lin_ev (hwP : Low.wellFormed P.nodes = true) {a : Nat} (e : ρ (tr.at a) = enc (vs a)) :
    Lia.eval (fun u => (ρ u).toR) (linOf P tr a).1 + ((linOf P tr a).2 : R) = (vs a).toR := by
  have := lin_sound (I := MI) hwP (tr.at a)
  rw [e, toR_enc] at this
  rw [this]; rfl

theorem typed_at {i : Nat} {n : SNode} (hn : SP.nodes[i]? = some n) :
    Surf.nodeTyped SP.sig SP.sortOf n (SP.sort i) = true := by
  have hi := lt_of_get hn
  simp only [Surf.typed, Bool.and_eq_true, beq_iff_eq, List.all_eq_true, List.mem_range] at ht
  have := ht.2 i hi
  rw [hn] at this
  exact this

omit hM ht in
theorem children_lt {i : Nat} {n : SNode} (hn : SP.nodes[i]? = some n) : ∀ c ∈ n.children, c < i := by
  have := List.all_eq_true.mp hw i (List.mem_range.mpr (lt_of_get hn))
  rw [hn] at this
  simpa using this

open Classical in
omit hM hw ht in
theorem ltInt_ev (hwP : Low.wellFormed P.nodes = true) {x y : Nat} (ex : ρ (tr.at x) = enc (vs x))
    (ey : ρ (tr.at y) = enc (vs y)) :
    (ltInt P tr x y).ev MI ρ = .bool (decide ((vs x).toR + 1 ≤ (vs y).toR)) := by
  have e : Lia.eval (fun u => (ρ u).toR) (linOf P tr x).1 + (((linOf P tr x).2 + 1 : Int) : R) = (vs x).toR + 1 := by
    rw [Ring.intCast_add, Ring.intCast_one, ← lin_ev hwP ex]; grind
  simp only [ltInt, Shape.ev, toR_num, e, ey, toR_enc]

open Classical in
omit hM hw ht in
theorem le_ev {x y : Nat} (ex : ρ (tr.at x) = enc (vs x)) (ey : ρ (tr.at y) = enc (vs y)) :
    (Shape.le (.same (tr.at x)) (.same (tr.at y))).ev MI ρ = .bool (decide ((vs x).toR ≤ (vs y).toR)) := by
  simp only [Shape.ev, ex, ey, toR_enc]

open Classical in
omit hM hw ht in
theorem lt_ev {x y : Nat} (ex : ρ (tr.at x) = enc (vs x)) (ey : ρ (tr.at y) = enc (vs y)) :
    (Shape.lt (.same (tr.at x)) (.same (tr.at y))).ev MI ρ = .bool (decide ((vs x).toR < (vs y).toR)) := by
  simp only [Shape.ev, ex, ey, toR_enc]

theorem expect_sound (hwP : Low.wellFormed P.nodes = true) {i : Nat} {n : SNode} {s : Shape}
    (hn : SP.nodes[i]? = some n) (h : expect SP P tr i = some s)
    (ih : ∀ c < i, ρ (tr.at c) = enc (vs c)) :
    s.ev MI ρ = enc (vs i) := by
  have hi := lt_of_get hn
  have hc := children_lt hw hn
  have hv := Surf.val_node (I := Ms.toInterp) hw hn
  have hty := typed_at hM hw ht hn
  have ihc : ∀ c ∈ n.children, ρ (tr.at c) = enc (vs c) := fun c m => ih c (hc c m)
  have lenc : ∀ c ∈ n.children, c < SP.nodes.length := fun c m => by have := hc c m; omega
  have leafs : ∀ as : List Nat, (∀ a ∈ as, a ∈ n.children) →
      as.map (fun a => (leaf SP tr a).ev MI ρ) = as.map (fun a => enc (vs a)) :=
    fun as hs => List.map_congr_left (fun a m => leaf_ev hM hw ht (lenc a (hs a m)) (ihc a (hs a m)))
  have forms : ∀ as : List Nat, (∀ a ∈ as, a ∈ n.children) →
      as.map (fun a => (form tr a).ev MI ρ) = as.map (fun a => Val.bool (vs a).truth) :=
    fun as hs => List.map_congr_left (fun a m => form_ev (ihc a (hs a m)))
  cases n with
  | const k =>
    simp only [expect, hn] at h
    split at h
    · rename_i hk; cases h
      rw [hv]; simp only [Surf.evalNode, Shape.ev]
      exact const_base SP P tr Ms hk
    · cases h
  | app f args =>
    simp only [expect, hn, Option.some.injEq] at h; subst h
    rw [hv]; simp only [Surf.evalNode, surf_get_values, Shape.ev, Shape.evs_eq, List.map_map]
    rw [ihc f (by simp [SNode.children])]
    have := leafs args (fun a m => by simp [SNode.children, m])
    simp only [Function.comp_def]
    rw [this, show args.map (fun a => enc (vs a)) = (args.map (Low.get (Surf.values Ms.toInterp SP.nodes))).map enc
      from by rw [List.map_map]; rfl]
    exact app_enc_enc SP P tr Ms _ _
  | num v =>
    simp only [expect, hn, Option.some.injEq] at h; subst h
    rw [hv]; simp only [Surf.evalNode, Shape.ev, Lia.eval, enc_num]
    congr 1; grind
  | tt =>
    simp only [expect, hn, Option.some.injEq] at h; subst h
    rw [hv]; rfl
  | ff =>
    simp only [expect, hn, Option.some.injEq] at h; subst h
    rw [hv]; rfl
  | not a =>
    simp only [expect, hn, Option.some.injEq] at h; subst h
    rw [hv]; simp only [Surf.evalNode, surf_get_values, Shape.ev, form_ev (ihc a (by simp [SNode.children])),
      truth_bool, enc_bool]
  | and as =>
    simp only [expect, hn, Option.some.injEq] at h; subst h
    rw [hv]; simp only [Surf.evalNode, surf_get_values, Shape.ev, Shape.allTrue_eq, enc_bool]
    congr 1
    rw [all_as_map, List.map_map, all_as_map as]
    have := forms as (fun a m => by simp [SNode.children, m])
    have := congrArg (List.map Val.truth) this
    simp only [List.map_map, Function.comp_def, truth_bool] at this
    simp only [Function.comp_def]
    rw [this]
  | or as =>
    simp only [expect, hn, Option.some.injEq] at h; subst h
    rw [hv]; simp only [Surf.evalNode, surf_get_values, Shape.ev, Shape.anyTrue_eq, enc_bool]
    congr 1
    rw [any_as_map, List.map_map, any_as_map as]
    have := forms as (fun a m => by simp [SNode.children, m])
    have := congrArg (List.map Val.truth) this
    simp only [List.map_map, Function.comp_def, truth_bool] at this
    simp only [Function.comp_def]
    rw [this]
  | select a x =>
    simp only [expect, hn, Option.some.injEq] at h; subst h
    rw [hv]; simp only [Surf.evalNode, surf_get_values, Shape.ev, Shape.evs]
    rw [ihc a (by simp [SNode.children]), leaf_ev hM hw ht (lenc x (by simp [SNode.children]))
      (ihc x (by simp [SNode.children])), ← List.map_singleton (f := enc)]
    exact app_enc_enc SP P tr Ms _ _
  | store a x v =>
    simp only [expect, hn] at h
    split at h
    · rename_i g _ _
      split at h
      · rename_i k _
        split at h
        · rename_i hst
          cases h
          simp only [Trace.storeOf, Bool.and_eq_true, decide_eq_true_eq, beq_iff_eq] at hst
          rw [hv]; simp only [Surf.evalNode, surf_get_values, Shape.ev, Shape.evs]
          rw [const_store SP P tr Ms hst.1 hst.2,
            leaf_ev hM hw ht (lenc a (by simp [SNode.children])) (ihc a (by simp [SNode.children])),
            leaf_ev hM hw ht (lenc x (by simp [SNode.children])) (ihc x (by simp [SNode.children])),
            leaf_ev hM hw ht (lenc v (by simp [SNode.children])) (ihc v (by simp [SNode.children])),
            app_store]
        · cases h
      · cases h
    · cases h
  | implies as =>
    simp only [expect, hn] at h
    rw [hv]; simp only [Surf.evalNode, surf_get_values, enc_bool]
    apply impliesS_ev _ _ _ (as.map fun a => (vs a).truth) _ h
    rw [List.map_map, List.map_map]
    exact forms as (fun a m => by simp [SNode.children, m])
  | xor as =>
    simp only [expect, hn] at h
    rw [hv]; simp only [Surf.evalNode, surf_get_values, enc_bool]
    apply xorS_ev _ _ _ (as.map fun a => (vs a).truth) _ h
    rw [List.map_map, List.map_map]
    exact forms as (fun a m => by simp [SNode.children, m])
  | iff as =>
    simp only [expect, hn, Option.some.injEq] at h; subst h
    obtain ⟨b, hb, hr⟩ := chain_ev MI ρ as (fun x y => .iff (form tr x) (form tr y))
      (fun x y => (vs x).truth = (vs y).truth) (fun x mx y my => by
        refine ⟨_, rfl, ?_⟩
        simp only [form_ev (ihc x (by simp [SNode.children, mx])),
          form_ev (ihc y (by simp [SNode.children, my])), truth_bool, beq_iff_eq])
    rw [hb, hv]; simp only [Surf.evalNode, surf_get_values, enc_bool]
    congr 1
    apply bool_eq_decide
    rw [hr, chainP_map]
  | ite c a b =>
    simp only [expect, hn] at h
    split at h
    · rename_i hb
      simp only [Option.some.injEq] at h; subst h
      simp only [Surf.nodeTyped, Bool.and_eq_true, beq_iff_eq] at hty
      obtain ⟨⟨_, hsa⟩, hsb⟩ := hty
      have ba : SP.isBool a = true := by simp only [SProblem.isBool, SProblem.sort] at hb ⊢; rw [hsa]; exact hb
      have bb : SP.isBool b = true := by simp only [SProblem.isBool, SProblem.sort] at hb ⊢; rw [hsb]; exact hb
      obtain ⟨xa, hxa⟩ := v_bool hM hw ht (lenc a (by simp [SNode.children])) ba
      obtain ⟨xb, hxb⟩ := v_bool hM hw ht (lenc b (by simp [SNode.children])) bb
      rw [hv]; simp only [Surf.evalNode, surf_get_values, Shape.ev]
      rw [form_ev (ihc c (by simp [SNode.children])), form_ev (ihc a (by simp [SNode.children])),
        form_ev (ihc b (by simp [SNode.children])), hxa, hxb, truth_bool]
      split <;> simp [truth_bool]
    · split at h
      · rename_i k hk
        split at h
        · rename_i hite
          simp only [Option.some.injEq] at h; subst h
          simp only [Trace.iteOf, Bool.and_eq_true, decide_eq_true_eq] at hite
          obtain ⟨hk', hm⟩ := hite
          split at hm
          · rename_i i' c0 a0 b0 hmi hni
            simp only [hn, Option.some.injEq, SNode.ite.injEq] at hni
            obtain ⟨rfl, rfl, rfl⟩ := hni
            simp only [Bool.and_eq_true, decide_eq_true_eq] at hm
            obtain ⟨hle, hm⟩ := hm
            split at hm
            · rename_i c' a' b' hni'
              simp only [Bool.and_eq_true, beq_iff_eq] at hm
              obtain ⟨⟨e1, e2⟩, e3⟩ := hm
              have hc' := children_lt hw hni'
              have same : ∀ x y, x ∈ (SNode.ite c' a' b').children → y ∈ (SNode.ite c a b).children →
                  tr.at x = tr.at y → vs x = vs y := by
                intro x y mx my e
                apply enc_inj
                rw [← ih x (by have := hc' x mx; omega), ← ihc y my, e]
              have ec := same c' c (by simp [SNode.children]) (by simp [SNode.children]) e1
              have ea := same a' a (by simp [SNode.children]) (by simp [SNode.children]) e2
              have eb := same b' b (by simp [SNode.children]) (by simp [SNode.children]) e3
              simp only [Shape.ev]
              rw [const_ite SP P tr Ms hk' hmi, Surf.val_node hw hni', hv]
              simp only [Surf.evalNode, surf_get_values, ec, ea, eb]
            · cases hm
          · cases hm
        · cases h
      · cases h
  | add as =>
    simp only [expect, hn, Option.some.injEq] at h; subst h
    rw [hv]; simp only [Surf.evalNode, Shape.ev, enc_num]
    congr 1
    rw [sumForms_eval _ (linOf P tr) (fun a => (vs a).toR) as
      (fun a m => lin_ev hwP (ihc a (by simp [SNode.children, m]))), sumR_eq, List.map_map]
    rfl
  | sub as =>
    simp only [expect, hn] at h
    cases as with
    | nil =>
      simp only [Option.some.injEq] at h; subst h
      rw [hv]; simp only [Surf.evalNode, Shape.ev, Lia.eval, enc_num]
      congr 1; grind
    | cons a rest =>
      simp only [Option.some.injEq] at h; subst h
      rw [hv]; simp only [Surf.evalNode, surf_get_values, Shape.ev, enc_num]
      congr 1
      have hr := sumForms_eval (fun u => (ρ u).toR) (linOf P tr) (fun a => (vs a).toR) rest
        (fun b m => lin_ev hwP (ihc b (by simp [SNode.children, m])))
      have ha := lin_ev hwP (ihc a (by simp [SNode.children]))
      rw [eval_append, Lia.eval_neg, Ring.intCast_sub, sumR_eq, List.map_map, ← ha]
      have : ((rest.map (Low.get (Surf.values Ms.toInterp SP.nodes))).map Val.toR) = rest.map (fun a => (vs a).toR) := by
        rw [List.map_map]; rfl
      simp only [List.map_map] at this hr ⊢
      rw [this, ← hr]
      grind
  | neg a =>
    simp only [expect, hn, Option.some.injEq] at h; subst h
    rw [hv]; simp only [Surf.evalNode, surf_get_values, Shape.ev, enc_num]
    congr 1
    have ha := lin_ev hwP (ihc a (by simp [SNode.children]))
    rw [Lia.eval_neg, Ring.intCast_neg, ← ha]
    grind
  | mul as =>
    simp only [expect, hn, Option.map_eq_some_iff] at h
    obtain ⟨p, hp, rfl⟩ := h
    rw [hv]; simp only [Surf.evalNode, Shape.ev, enc_num]
    congr 1
    rw [mulForms_eval _ (linOf P tr) (fun a => (vs a).toR) as
      (fun a m => lin_ev hwP (ihc a (by simp [SNode.children, m]))) p hp, prodR_eq, List.map_map]
    rfl
  | eq as =>
    simp only [expect, hn] at h
    cases as with
    | nil => cases h
    | cons a rest =>
      simp only at h
      split at h
      · cases h
      · rename_i hnb
        simp only [Option.some.injEq] at h; subst h
        simp only [Surf.nodeTyped, Bool.and_eq_true, List.all_eq_true, beq_iff_eq] at hty
        have sorts : ∀ x ∈ a :: rest, SP.sort x = SP.sort a := by
          intro x m; simp only [List.mem_cons] at m
          rcases m with rfl | m
          · rfl
          · exact hty.2 x m
        have mem : ∀ x ∈ a :: rest, x ∈ (SNode.eq (a :: rest)).children := fun x m => m
        obtain ⟨b, hb, hr⟩ := chain_ev MI ρ (a :: rest)
          (fun x y => eqS (SP.isNum a) (.same (tr.at x)) (.same (tr.at y))) (fun x y => vs x = vs y)
          (fun x mx y my => by
            have ex := ihc x (mem x mx)
            have ey := ihc y (mem y my)
            unfold eqS
            split
            · rename_i hnum
              have nx : SP.isNum x = true := by unfold SProblem.isNum at hnum ⊢; rw [sorts x mx]; exact hnum
              have ny : SP.isNum y = true := by unfold SProblem.isNum at hnum ⊢; rw [sorts y my]; exact hnum
              obtain ⟨rx, hrx⟩ := v_num hM hw ht (lenc x (mem x mx)) nx
              obtain ⟨ry, hry⟩ := v_num hM hw ht (lenc y (mem y my)) ny
              refine ⟨_, rfl, ?_⟩
              simp only [Shape.allTrue, Shape.ev, ex, ey, toR_enc, hrx, hry, toR_num, truth_bool, Bool.and_true,
                Bool.and_eq_true, decide_eq_true_eq, Val.num.injEq]
              constructor
              · rintro ⟨h1, h2⟩; exact Std.le_antisymm h1 h2
              · rintro rfl; exact ⟨Std.le_refl _, Std.le_refl _⟩
            · refine ⟨_, rfl, ?_⟩
              simp only [Shape.ev, ex, ey, decide_eq_true_eq]
              exact ⟨enc_inj, fun e => by rw [e]⟩)
        rw [hb, hv]; simp only [Surf.evalNode, enc_bool]
        congr 1
        apply bool_eq_decide
        exact hr.trans (chainP_map _ _ _).symm
  | distinct as =>
    simp only [expect, hn] at h
    cases as with
    | nil => cases h
    | cons a rest =>
      simp only at h
      simp only [Surf.nodeTyped, Bool.and_eq_true, List.all_eq_true, beq_iff_eq] at hty
      have sorts : ∀ x ∈ a :: rest, SP.sort x = SP.sort a := by
        intro x m; simp only [List.mem_cons] at m
        rcases m with rfl | m
        · rfl
        · exact hty.2 x m
      have mem : ∀ x ∈ a :: rest, x ∈ (SNode.distinct (a :: rest)).children := fun x m => m
      have finish : ∀ (mk : Nat → Nat → Shape),
          (∀ x ∈ a :: rest, ∀ y ∈ a :: rest, ∃ b, (mk x y).ev MI ρ = .bool b ∧ (b = true ↔ vs x ≠ vs y)) →
          (Shape.and ((allPairs (a :: rest)).map fun p => mk p.1 p.2)).ev MI ρ = enc (vs i) := by
        intro mk hmk
        obtain ⟨b, hb, hr⟩ := pairs_ev MI ρ (a :: rest) mk (fun x y => vs x ≠ vs y) hmk
        rw [hb, hv]; simp only [Surf.evalNode, enc_bool]
        congr 1
        apply bool_eq_decide
        exact hr.trans (pairwiseP_map _ _ _).symm
      split at h
      · cases h
      · split at h
        · rename_i hint
          simp only [Option.some.injEq] at h; subst h
          apply finish (fun x y => Shape.or [ltInt P tr x y, ltInt P tr y x])
          intro x mx y my
          have ix : SP.isInt x = true := by unfold SProblem.isInt at hint ⊢; rw [sorts x mx]; exact hint
          have iy : SP.isInt y = true := by unfold SProblem.isInt at hint ⊢; rw [sorts y my]; exact hint
          obtain ⟨kx, hkx⟩ := v_int hM hw ht (lenc x (mem x mx)) ix
          obtain ⟨ky, hky⟩ := v_int hM hw ht (lenc y (mem y my)) iy
          refine ⟨_, rfl, ?_⟩
          simp only [Shape.anyTrue, ltInt_ev hwP (ihc x (mem x mx)) (ihc y (mem y my)),
            ltInt_ev hwP (ihc y (mem y my)) (ihc x (mem x mx)), truth_bool, Bool.or_false, Bool.or_eq_true,
            decide_eq_true_eq, hkx, hky, toR_num, cast_succ_le_iff, ne_eq, num_int_eq_iff]
          omega
        · split at h
          · rename_i hnum
            simp only [Option.some.injEq] at h; subst h
            apply finish (fun x y => Shape.or [.lt (.same (tr.at x)) (.same (tr.at y)), .lt (.same (tr.at y)) (.same (tr.at x))])
            intro x mx y my
            have nx : SP.isNum x = true := by unfold SProblem.isNum at hnum ⊢; rw [sorts x mx]; exact hnum
            have ny : SP.isNum y = true := by unfold SProblem.isNum at hnum ⊢; rw [sorts y my]; exact hnum
            obtain ⟨rx, hrx⟩ := v_num hM hw ht (lenc x (mem x mx)) nx
            obtain ⟨ry, hry⟩ := v_num hM hw ht (lenc y (mem y my)) ny
            refine ⟨_, rfl, ?_⟩
            simp only [Shape.ev, Shape.anyTrue, ihc x (mem x mx), ihc y (mem y my), toR_enc,
              truth_bool, Bool.or_false, Bool.or_eq_true, decide_eq_true_eq, hrx, hry, toR_num, ne_eq, Val.num.injEq]
            exact (ne_iff_lt_or_gt (R := R)).symm
          · simp only [Option.some.injEq] at h; subst h
            apply finish (fun x y => Shape.not (.eq (.same (tr.at x)) (.same (tr.at y))))
            intro x mx y my
            refine ⟨_, rfl, ?_⟩
            simp only [Shape.ev, ihc x (mem x mx), ihc y (mem y my), truth_bool, Bool.not_eq_true',
              decide_eq_false_iff_not, ne_eq]
            exact ⟨fun h1 e => h1 (by rw [e]), fun h1 e => h1 (enc_inj e)⟩
  | le as =>
    simp only [expect, hn, Option.some.injEq] at h; subst h
    obtain ⟨b, hb, hr⟩ := chain_ev MI ρ as (fun x y => .le (.same (tr.at x)) (.same (tr.at y)))
      (fun x y => (vs x).toR ≤ (vs y).toR) (fun x mx y my =>
        ⟨_, le_ev (ihc x mx) (ihc y my), by simp only [decide_eq_true_eq]⟩)
    rw [hb, hv]; simp only [Surf.evalNode, surf_get_values, enc_bool]
    congr 1
    apply bool_eq_decide
    exact hr.trans (chainP_map _ _ _).symm
  | ge as =>
    simp only [expect, hn, Option.some.injEq] at h; subst h
    obtain ⟨b, hb, hr⟩ := chain_ev MI ρ as (fun x y => .le (.same (tr.at y)) (.same (tr.at x)))
      (fun x y => (vs y).toR ≤ (vs x).toR) (fun x mx y my =>
        ⟨_, le_ev (ihc y my) (ihc x mx), by simp only [decide_eq_true_eq]⟩)
    rw [hb, hv]; simp only [Surf.evalNode, surf_get_values, enc_bool]
    congr 1
    apply bool_eq_decide
    exact hr.trans (chainP_map (fun x y => y ≤ x) (fun a => (vs a).toR) as).symm
  | lt as =>
    simp only [expect, hn] at h
    split at h
    · rename_i hint
      simp only [Option.some.injEq] at h; subst h
      simp only [List.all_eq_true] at hint
      obtain ⟨b, hb, hr⟩ := chain_ev MI ρ as (fun x y => ltInt P tr x y)
        (fun x y => (vs x).toR < (vs y).toR) (fun x mx y my => by
          obtain ⟨kx, hkx⟩ := v_int hM hw ht (lenc x mx) (hint x mx)
          obtain ⟨ky, hky⟩ := v_int hM hw ht (lenc y my) (hint y my)
          refine ⟨_, ltInt_ev hwP (ihc x mx) (ihc y my), ?_⟩
          simp only [decide_eq_true_eq, hkx, hky, toR_num, cast_succ_le_iff, cast_lt_iff])
      rw [hb, hv]; simp only [Surf.evalNode, surf_get_values, enc_bool]
      congr 1
      apply bool_eq_decide
      exact hr.trans (chainP_map _ _ _).symm
    · simp only [Option.some.injEq] at h; subst h
      obtain ⟨b, hb, hr⟩ := chain_ev MI ρ as (fun x y => .lt (.same (tr.at x)) (.same (tr.at y)))
        (fun x y => (vs x).toR < (vs y).toR) (fun x mx y my =>
          ⟨_, lt_ev (ihc x mx) (ihc y my), by simp only [decide_eq_true_eq]⟩)
      rw [hb, hv]; simp only [Surf.evalNode, surf_get_values, enc_bool]
      congr 1
      apply bool_eq_decide
      exact hr.trans (chainP_map _ _ _).symm
  | gt as =>
    simp only [expect, hn] at h
    split at h
    · rename_i hint
      simp only [Option.some.injEq] at h; subst h
      simp only [List.all_eq_true] at hint
      obtain ⟨b, hb, hr⟩ := chain_ev MI ρ as (fun x y => ltInt P tr y x)
        (fun x y => (vs y).toR < (vs x).toR) (fun x mx y my => by
          obtain ⟨kx, hkx⟩ := v_int hM hw ht (lenc x mx) (hint x mx)
          obtain ⟨ky, hky⟩ := v_int hM hw ht (lenc y my) (hint y my)
          refine ⟨_, ltInt_ev hwP (ihc y my) (ihc x mx), ?_⟩
          simp only [decide_eq_true_eq, hkx, hky, toR_num, cast_succ_le_iff, cast_lt_iff])
      rw [hb, hv]; simp only [Surf.evalNode, surf_get_values, enc_bool]
      congr 1
      apply bool_eq_decide
      exact hr.trans (chainP_map (fun x y => y < x) (fun a => (vs a).toR) as).symm
    · simp only [Option.some.injEq] at h; subst h
      obtain ⟨b, hb, hr⟩ := chain_ev MI ρ as (fun x y => .lt (.same (tr.at y)) (.same (tr.at x)))
        (fun x y => (vs y).toR < (vs x).toR) (fun x mx y my =>
          ⟨_, lt_ev (ihc y my) (ihc x mx), by simp only [decide_eq_true_eq]⟩)
      rw [hb, hv]; simp only [Surf.evalNode, surf_get_values, enc_bool]
      congr 1
      apply bool_eq_decide
      exact hr.trans (chainP_map (fun x y => y < x) (fun a => (vs a).toR) as).symm

end

/-! ## The checks, and the theorem -/

/-- What `Trace.check` says. -/
structure CheckSpec (SP : SProblem) (P : Problem) (tr : Trace) (A : Answer) : Prop where
  wf : Surf.wellFormed SP.nodes = true
  typed : Surf.typed SP.sig SP.nodes SP.sortOf = true
  sorts : SortsSpec SP
  sig : SigSpec SP P tr
  nodes : ∀ i < SP.nodes.length, tr.at i < P.nodes.length ∧
    ∃ s, expect SP P tr i = some s ∧ fits P.sig P.nodes P.sortOf (tr.at i) s = true
  inputs : ∀ f ∈ P.inputs, f < P.nodes.length ∧ ∃ w, tr.inputOk SP P f w = true
  assumed : A.assumed = []

theorem check_spec {SP : SProblem} {P : Problem} {tr : Trace} {A : Answer} (h : tr.check SP P A = true) :
    CheckSpec SP P tr A := by
  simp only [Trace.check, Bool.and_eq_true, beq_iff_eq, List.all_eq_true, List.mem_range] at h
  obtain ⟨⟨⟨⟨⟨⟨⟨⟨hw, ht⟩, hs⟩, hg⟩, _⟩, hn⟩, hlen⟩, hi⟩, ha⟩ := h
  refine ⟨hw, ht, sortsOk_spec hs, sigOk_spec hg, ?_, ?_, ?_⟩
  · intro i hi'
    have := hn i hi'
    simp only [decide_eq_true_eq] at this
    refine ⟨this.1, ?_⟩
    have h2 := this.2
    split at h2
    · rename_i s hs'; exact ⟨s, hs', h2⟩
    · cases h2
  · intro f hf
    obtain ⟨j, hj, rfl⟩ := List.mem_iff_getElem.mp hf
    have hj' : j < tr.inputs.length := by omega
    have hm : (P.inputs[j], tr.inputs[j]) ∈ P.inputs.zip tr.inputs := by
      have e : (P.inputs.zip tr.inputs)[j]'(by simp; omega) = (P.inputs[j], tr.inputs[j]) := List.getElem_zip
      rw [← e]
      exact List.getElem_mem _
    have := hi _ hm
    simp only [decide_eq_true_eq] at this
    exact ⟨this.1, _, this.2⟩
  · exact ha

section
variable {SP : SProblem} {P : Problem} {tr : Trace} {A : Answer} {Ms : Surf.Model R U} {size : Val R U → Nat}
  (hc : CheckSpec SP P tr A) (hA : Accepted P A) (hL : SP.Laws Ms size)

set_option quotPrecheck false in
local notation "ρ" => Low.val (lowModel SP P tr Ms).toInterp P.nodes
local notation "vs" => Surf.val Ms.toInterp SP.nodes
set_option quotPrecheck false in
local notation "MI" => (lowModel SP P tr Ms).toInterp

include hc hA hL

/-- Each lowered node of the trace takes the value of its surface node. -/
theorem corr : ∀ i < SP.nodes.length, ρ (tr.at i) = enc (vs i) := by
  have hM := lowModel_sound hc.sorts hc.sig hL hc.wf hc.typed
  have hrngP : ∀ i < P.nodes.length, inRange P.bvIndex (P.sortOf[i]?.getD 0) (ρ i) := by
    intro i _ w hw; rw [hc.sig.bvIndex] at hw; cases hw
  intro i
  induction i using Nat.strongRecOn with
  | ind i ih =>
  intro hi
  obtain ⟨hlt, s, hs, hf⟩ := hc.nodes i hi
  rw [fits_sound hM hA.wf hA.typed hrngP _ hlt s hf]
  obtain ⟨n, hn⟩ : ∃ n, SP.nodes[i]? = some n := ⟨_, List.getElem?_eq_getElem hi⟩
  exact expect_sound hL.sound hc.wf hc.typed hA.wf hn hs (fun c hci => ih c hci (by omega))

theorem lt_of_holds {a : Nat} (h : Surf.holds Ms.toInterp SP.nodes a) : a < SP.nodes.length := by
  rcases Nat.lt_or_ge a SP.nodes.length with h' | h'
  · exact h'
  · exfalso
    unfold Surf.holds Surf.val Low.get at h
    rw [List.getElem?_eq_none (by rw [Surf.values_length]; exact h')] at h
    cases h

/-- The lowered inputs hold when the surface assertions do. -/
theorem inputs_hold (hin : ∀ a ∈ SP.asserts, Surf.holds Ms.toInterp SP.nodes a) :
    ∀ f ∈ P.inputs, Low.holds MI P.nodes f := by
  have hM := lowModel_sound hc.sorts hc.sig hL hc.wf hc.typed
  have hrngP : ∀ i < P.nodes.length, inRange P.bvIndex (P.sortOf[i]?.getD 0) (ρ i) := by
    intro i _ w hw; rw [hc.sig.bvIndex] at hw; cases hw
  have hco := corr hc hA hL
  intro f hf
  obtain ⟨hfl, w, hw⟩ := hc.inputs f hf
  unfold Low.holds
  cases w with
  | assert a =>
    simp only [Trace.inputOk, Bool.and_eq_true, List.contains_iff_mem] at hw
    have ha := hin a hw.1
    have hal := lt_of_holds hc hA hL ha
    rw [fits_sound hM hA.wf hA.typed hrngP f hfl _ hw.2, form_ev (hco a hal)]
    unfold Surf.holds at ha
    rw [ha, truth_bool]
  | iteThen k | iteElse k =>
    simp only [Trace.inputOk] at hw
    split at hw
    · rename_i s hs
      rw [fits_sound hM hA.wf hA.typed hrngP f hfl s hw]
      unfold iteFact at hs
      split at hs
      · cases hs
      · rename_i hk
        split at hs
        · rename_i i hm
          split at hs
          · rename_i c a b hn
            have hil := lt_of_get hn
            have hch := children_lt hc.wf hn
            have hci := hco c (by have := hch c (by simp [SNode.children]); omega)
            have hai := hco a (by have := hch a (by simp [SNode.children]); omega)
            have hbi := hco b (by have := hch b (by simp [SNode.children]); omega)
            have hk' : SP.sig.decls.length ≤ k := by omega
            have hv := Surf.val_node (I := Ms.toInterp) hc.wf hn
            simp only [Surf.evalNode, surf_get_values] at hv
            have hK : (lowModel SP P tr Ms).const k = enc (vs i) := const_ite SP P tr Ms hk' hm
            -- `k = t` holds when `k` is `t`
            have eqT : ∀ t, t ∈ (SNode.ite c a b).children → vs i = vs t → ρ (tr.at t) = enc (vs t) →
                (eqS (SP.isNum i) (.const k) (.same (tr.at t))).ev MI ρ = .bool true := by
              intro t _ e ht'
              unfold eqS
              split
              · simp only [Shape.ev, Shape.allTrue, truth_bool, Bool.and_true]
                have hK' : (lowModel SP P tr Ms).toInterp.const k = enc (vs t) := by rw [← e]; exact hK
                simp only [hK', ht', Std.le_refl, decide_true, Bool.and_self]
              · simp only [Shape.ev]
                have hK' : (lowModel SP P tr Ms).toInterp.const k = enc (vs t) := by rw [← e]; exact hK
                simp only [hK', ht', decide_true]
            split at hs
            · simp only [Option.some.injEq] at hs; subst hs
              simp only [Shape.ev, form_ev hci, truth_bool]
              by_cases e : (vs c).truth = true
              · rw [eqT a (by simp [SNode.children]) (by rw [hv, if_pos e]) hai]; simp [e, truth_bool]
              · simp [e]
            · simp only [Option.some.injEq] at hs; subst hs
              simp only [Shape.ev, form_ev hci, truth_bool]
              by_cases e : (vs c).truth = true
              · simp [e]
              · rw [eqT b (by simp [SNode.children]) (by rw [hv, if_neg e]) hbi]; simp [e, truth_bool]
          · cases hs
        · cases hs
    · cases hw

end

/-- A lowering checked by `Trace.check`, whose lowered problem has an answer
    that `Problem.accepts` accepts, has no model: no model of the surface
    script satisfies its assertions. -/
theorem Trace.unsat {SP : SProblem} {P : Problem} {tr : Trace} {A : Answer}
    (hc : tr.check SP P A = true) (hA : P.accepts A = true) (Ms : Surf.Model R U) (size : Val R U → Nat)
    (hL : SP.Laws Ms size) (hin : ∀ a ∈ SP.asserts, Surf.holds Ms.toInterp SP.nodes a) : False := by
  have hcs := check_spec hc
  have hAc := accepted hA
  apply Problem.accepts_sound hA (lowModel SP P tr Ms) (fun v => size (dec v))
    (lowModel_laws hcs.sorts hcs.sig hL hcs.wf hcs.typed) (inputs_hold hcs hAc hL hin)
  · intro s hs; rw [hcs.assumed] at hs; cases hs
  · intro i _ w hw
    rw [hcs.sig.bvIndex] at hw; cases hw

end Vik.Lower
