import Vik.Lower.BitOps
import Vik.Shifts
import Vik.Signed

/-!
# Bit-vector operations through rules

The lowering computes most bit-vector operations with the symbols of rules:
`bvadd` is the remainder of a `modulo`, `bvudiv` by a constant the quotient of
a `divide`, and so on. `Trace.bvOk` checks those combinations, and
`bvOk_sound` proves that, with the rule symbols at their values, they take the
value of the operation.
-/

set_option linter.unusedSectionVars false

namespace Vik.Lower

open Lean.Grind Vik.Real Vik.Low Vik.Surf

variable {R U : Type} [Field R] [LE R] [LT R] [Std.IsLinearOrder R] [Std.LawfulOrderLT R]
  [OrderedRing R] [IsCharP R 0]

attribute [local instance low] Lean.Grind.Ring.intCast

theorem ofInt_val {w : Nat} (x : BitVec w) : BitVec.ofInt w (Vik.val x) = x := by
  apply Vik.val_inj.mp
  rw [Vik.val_ofInt, Int.emod_eq_of_lt (Vik.val_nonneg x) (Vik.val_lt x)]

section
variable {SP : SProblem} {P : Problem} {tr : Trace} {Ms : Surf.Model R U}
  (hwP : Low.wellFormed P.nodes = true) (htP : Low.typed P.sig P.nodes P.sortOf = true)
  (hG : SigSpec SP P tr) (hML : (lowModel SP P tr Ms).Sound P.sig P.bvIndex)
  (hrng : ∀ j < P.nodes.length,
    inRange P.bvIndex (P.sortOf[j]?.getD 0) (Low.val (lowModel SP P tr Ms).toInterp P.nodes j))

set_option quotPrecheck false in
local notation "ρ" => Low.val (lowModel SP P tr Ms).toInterp P.nodes
set_option quotPrecheck false in
local notation "MI" => (lowModel SP P tr Ms).toInterp
set_option quotPrecheck false in
local notation "ιρ" => fun y => intOf (Low.val (lowModel SP P tr Ms).toInterp P.nodes y)
set_option quotPrecheck false in
local notation "τρ" => fun y => (Low.val (lowModel SP P tr Ms).toInterp P.nodes y).truth


include hwP htP hG hML hrng in
theorem modOk_sound {l w : Nat} {f : Lia.Form × Int} {V : Int} (h : tr.modOk SP P l f w = true)
    (hf : (evf ρ f) = (V : R)) : ρ l = .num ((V % 2 ^ w : Int) : R) := by
  unfold Trace.modOk at h
  split at h
  · rename_i c hc
    have e := constOf_eval hc (fun u => (ρ u).toR)
    unfold evf at hf
    rw [hf] at e
    have := cast_inj e
    subst this
    exact Low.val_num hwP (by simpa using h)
  · split at h
    · rename_i v w' lo hi hs
      simp only [Bool.and_eq_true, beq_iff_eq] at h
      obtain ⟨rfl, hfit⟩ := h
      rw [sym_val hwP hG hs]
      simp only [Rule.ival]
      rw [fits_num hwP htP hML hrng hfit ((symAt_args hG hs).2 v (by simp [Rule.args])) hf, intOf_num]
    · cases h

include hwP htP hG hML hrng in
theorem divOk_sound {l j : Nat} {f : Lia.Form × Int} {d V : Int} (h : tr.divOk SP P l f d j = true)
    (hf : (evf ρ f) = (V : R)) : ρ l = .num ((if j = 0 then V / d else V % d : Int) : R) := by
  unfold Trace.divOk at h
  split at h
  · rename_i c hc
    have e := constOf_eval hc (fun u => (ρ u).toR)
    unfold evf at hf
    rw [hf] at e
    have := cast_inj e
    subst this
    exact Low.val_num hwP (by simpa using h)
  · split at h
    · rename_i v d' qm j' hs
      simp only [Bool.and_eq_true, beq_iff_eq] at h
      obtain ⟨⟨rfl, rfl⟩, hfit⟩ := h
      rw [sym_val hwP hG hs]
      have hv := fits_num hwP htP hML hrng hfit ((symAt_args hG hs).2 v (by simp [Rule.args])) hf
      cases j' <;> simp only [Rule.ival, hv, intOf_num] <;> simp
    · cases h

include hwP htP hG hML hrng in
theorem casesOk_sound {l : Nat} {c : Shape} {thn els : Lia.Form × Int} {TH EL : Int}
    (h : tr.casesOk SP P l c thn els = true) (hth : (evf ρ thn) = (TH : R)) (hel : (evf ρ els) = (EL : R)) :
    ρ l = .num ((if (c.ev MI ρ).truth then TH else EL : Int) : R) := by
  unfold Trace.casesOk at h
  split at h
  · rename_i c' t e lo hi hs
    simp only [Bool.and_eq_true] at h
    obtain ⟨⟨hc, ht'⟩, he⟩ := h
    have args := (symAt_args hG hs).2
    rw [sym_val hwP hG hs]
    simp only [Rule.ival]
    rw [fits_sound hML hwP htP hrng c' (args c' (by simp [Rule.args])) _ hc,
      fits_num hwP htP hML hrng ht' (args t (by simp [Rule.args])) hth,
      fits_num hwP htP hML hrng he (args e (by simp [Rule.args])) hel]
    simp only [intOf_num]
  · cases h

include hwP hG in
theorem symNode_val {r : Rule} {j x : Nat} (h : tr.symNode SP P r j = some x) :
    ρ x = .num ((r.ival ιρ τρ j : Int) : R) :=
  sym_val hwP hG (symNode_sym h).2

end

/-! ## The operations -/

section
variable {SP : SProblem} {P : Problem} {tr : Trace} {Ms : Surf.Model R U}
  (hM : Ms.Sound SP.sig) (hw : Surf.wellFormed SP.nodes = true) (ht : Surf.typed SP.sig SP.nodes SP.sortOf = true)
  (hwP : Low.wellFormed P.nodes = true) (htP : Low.typed P.sig P.nodes P.sortOf = true)
  (hG : SigSpec SP P tr) (hML : (lowModel SP P tr Ms).Sound P.sig P.bvIndex)
  (hrng : ∀ j < P.nodes.length,
    inRange P.bvIndex (P.sortOf[j]?.getD 0) (Low.val (lowModel SP P tr Ms).toInterp P.nodes j))

set_option quotPrecheck false in
local notation "ρ" => Low.val (lowModel SP P tr Ms).toInterp P.nodes
set_option quotPrecheck false in
local notation "vs" => Surf.val Ms.toInterp SP.nodes
set_option quotPrecheck false in
local notation "MI" => (lowModel SP P tr Ms).toInterp

include hM hw ht hwP in
/-- A bit-vector child: its lowering is its value, and its linear combination
    too. -/
theorem bv_child {a w : Nat} (ha : a < SP.nodes.length) (hs : SP.sig.is (SP.sort a) (.bv w) = true)
    (e : ρ (tr.at a) = enc (vs a)) :
    ∃ x : BitVec w, vs a = ofBv x ∧ ρ (tr.at a) = .num ((Vik.val x : Int) : R) ∧
      evf ρ (linOf P tr a) = ((Vik.val x : Int) : R) := by
  obtain ⟨x, hx⟩ := v_bv hM hw ht ha hs
  refine ⟨x, hx, by rw [e, hx, ofBv_num, enc_num], ?_⟩
  have la := lin_ev hwP e
  rw [hx, ofBv_num, toR_num] at la
  exact la

theorem natAbs_mag (t : Int) : ((t.natAbs : Nat) : Int) = Vik.mag t := by unfold Vik.mag; split <;> omega

include hwP htP hG hML hrng in
/-- `bvsdiv`, `bvsrem` and `bvsmod` by a constant. -/
theorem sdivOk_sound {op : BvBin} {w l t : Nat} {c : Int} {x y : BitVec w} (hw0 : 0 < w)
    (ht : ρ t = .num ((Vik.val x : Int) : R)) (hyc : Vik.val y = c)
    (hop : op = .sdiv ∨ op = .srem ∨ op = .smod) (hb : tr.sdivOk SP P op w l t c = true) :
    ρ l = .num ((Vik.val (op.apply x y) : Int) : R) := by
  have h2 := Vik.two_pow_pred hw0
  have hT : y.toInt = smodT c w := by
    rw [Vik.toInt_eq y hw0, hyc]; unfold smodT
    rw [show (2 : Int) ^ w / 2 = 2 ^ (w - 1) by omega]
  have hX : x.toInt = signedI w (Vik.val x) := by rw [Vik.toInt_eq x hw0]; rfl
  unfold Trace.sdivOk at hb
  simp only at hb
  split at hb
  · rename_i hT0
    have hy0 : y.toInt = 0 := by rw [hT, hT0]
    have y0 : y = 0#w := by
      apply Vik.val_inj.mp
      have := Vik.val_lt y
      unfold smodT at hT0
      have : Vik.val y = 0 := by split at hT0 <;> omega
      rw [this]; rfl
    rcases hop with rfl | rfl | rfl
    · simp only at hb
      rw [casesOk_sound hwP htP hG hML hrng hb (evf_const ρ 1) (evf_const ρ _), negS_int MI ρ hwP rfl ht]
      simp only [BvBin.apply, y0, Vik.sdiv_zero_val x hw0, decide_eq_true_eq]
    · simp only at hb
      rw [beq_iff_eq.mp hb, ht]
      simp only [BvBin.apply, Vik.srem_val, hy0, Int.tmod_zero, ← Vik.val_eq_toInt_emod]
    · simp only at hb
      rw [beq_iff_eq.mp hb, ht]
      simp only [BvBin.apply, Vik.smod_val, hy0, Int.fmod_zero, ← Vik.val_eq_toInt_emod]
  · rename_i hT0
    have yne : y ≠ 0#w := by
      intro e; apply hT0; rw [← hT, e]; simp
    split at hb
    · rename_i q rm hq hrm
      have eq := symNode_val (Ms := Ms) hwP hG hq
      have er := symNode_val (Ms := Ms) hwP hG hrm
      simp only [Rule.ival, ht, intOf_num, Bool.false_eq_true, if_false] at eq er
      rw [← hX, natAbs_mag] at eq er
      rcases hop with rfl | rfl | rfl
      · simp only at hb
        have hc : (if 0 < smodT c w then (1 : Rat) else -1) = (((if 0 < smodT c w then 1 else -1 : Int)) : Rat) := by
          split <;> simp
        rw [hc] at hb
        rw [modOk_sound hwP htP hG hML hrng hb (evf_sym ρ eq _)]
        simp only [BvBin.apply, Vik.sdiv_val x y yne, hT, ← Vik.sdiv_const_lowering _ _ hT0]
        split <;> simp
      · simp only at hb
        rw [modOk_sound hwP htP hG hML hrng hb (evf_sym1 ρ er)]
        simp only [BvBin.apply, BvBin.width, Vik.srem_val, hT, Vik.srem_const_lowering]
      · simp only at hb
        split at hb
        · rename_i z hz
          have ez := symNode_val (Ms := Ms) hwP hG hz
          simp only [Rule.ival, ht, intOf_num] at ez
          rw [modOk_sound hwP htP hG hML hrng hb (evf_sym2' ρ er ez _)]
          simp only [BvBin.apply, Vik.smod_val, hT, ← Vik.smod_const_lowering _ _ hT0]
          congr 3
          have hz' : smodzVal (Vik.val x) c w = if (if 0 < smodT c w then x.toInt < 0 else ¬x.toInt < 0) ∧
              x.toInt.tmod (Vik.mag (smodT c w)) ≠ 0 then 1 else 0 := by
            unfold smodzVal
            simp only [← hX, natAbs_mag]
            simp only [Vik.toInt_neg_iff x hw0]
            by_cases h1 : 0 < smodT c w <;> by_cases h2 : 2 ^ (w - 1) ≤ Vik.val x <;>
              by_cases h3 : x.toInt.tmod (Vik.mag (smodT c w)) = 0 <;> simp [h1, h2, h3]
          rw [hz']; exact congrArg _ (Int.mul_comm _ _)
        · cases hb
    · cases hb

include hwP htP hG hML hrng in
theorem rotOk_sound {l t w s : Nat} {X : Int} (hl : l < P.nodes.length) (ht : ρ t = .num (X : R))
    (hb : tr.rotOk SP P l t w s = true) :
    0 < s ∧ s < w ∧ ρ l = .num ((2 ^ s * (X % 2 ^ (w - s)) + X / 2 ^ (w - s) : Int) : R) := by
  unfold Trace.rotOk at hb
  simp only [Bool.and_eq_true, decide_eq_true_eq] at hb
  obtain ⟨⟨hs, hsw⟩, hb⟩ := hb
  refine ⟨hs, hsw, ?_⟩
  split at hb
  · rename_i q rm hq hrm
    have eq := symNode_val (Ms := Ms) hwP hG hq
    have er := symNode_val (Ms := Ms) hwP hG hrm
    simp only [Rule.ival, ht, intOf_num] at eq er
    rw [fits_num hwP htP hML hrng hb hl (evf_sym2 ρ er eq _ _)]
    congr 2; grind
  · cases hb

include hM hw ht hwP htP hG hML hrng in
/-- The unary operations through rules. -/
theorem bvun_ops_sound {i w a : Nat} {op : BvUn} (hn : SP.nodes[i]? = some (.bvun op w a))
    (hl : tr.at i < P.nodes.length) (hb : tr.bvOk SP P i = true) (ih : ∀ c < i, ρ (tr.at c) = enc (vs c)) : ρ (tr.at i) = enc (vs i) := by
  have hai := children_lt hw hn a (by simp [SNode.children])
  have hal : a < SP.nodes.length := by have := lt_of_get hn; omega
  have hty := typed_at hM hw ht hn
  simp only [Surf.nodeTyped, Bool.and_eq_true] at hty
  obtain ⟨x, hx, ea, la⟩ := bv_child hM hw ht hwP hal hty.1 (ih a hai)
  have hv := Surf.val_node (I := Ms.toInterp) hw hn
  simp only [Surf.evalNode, surf_get_values, hx, bvOf_ofBv] at hv
  rw [hv, ofBv_num, enc_num]
  simp only [Trace.bvOk, hn] at hb
  split at hb
  · rename_i ca hca
    have e := constOf_eval hca (fun u => (ρ u).toR)
    change evf ρ (linOf P tr a) = _ at e
    rw [la] at e
    have e' := cast_inj e
    subst e'
    rw [Low.val_num hwP (by simpa using hb), bvunConst, ofInt_val]
  · cases op <;> simp only [Bool.false_eq_true] at hb
    case neg =>
      rw [modOk_sound hwP htP hG hML hrng hb (V := -Vik.val x) (by rw [evf_neg, la, Ring.intCast_neg])]
      simp only [BvUn.apply, (Vik.neg_val x).1]
    case extract hi lo =>
      simp only [Bool.and_eq_true, decide_eq_true_eq] at hb
      obtain ⟨⟨hlh, hhw⟩, hb⟩ := hb
      have hq : ∀ y, tr.extractQ SP P (tr.at a) (linOf P tr a) lo y = true →
          ρ y = .num ((Vik.val x / 2 ^ lo : Int) : R) := by
        intro y hy
        unfold Trace.extractQ at hy
        split at hy
        · rename_i h0; subst h0
          rw [beq_iff_eq.mp hy, ea]; simp
        · rw [divOk_sound hwP htP hG hML hrng hy la]; simp
      have hval := (Vik.extract_val x hi lo hlh hhw).1
      simp only [BvUn.apply, BvUn.width]
      split at hb
      · rename_i htop
        rw [hq _ hb, hval, if_pos (by omega)]
      · rename_i htop
        split at hb
        · rename_i v d qm hs
          simp only [Bool.and_eq_true, beq_iff_eq] at hb
          obtain ⟨rfl, hqv⟩ := hb
          rw [sym_val hwP hG hs]
          simp only [Rule.ival]
          rw [hq v hqv, intOf_num, hval, if_neg (by omega)]
        · cases hb
    case rotateLeft k =>
      obtain ⟨hs, hsw, e⟩ := rotOk_sound hwP htP hG hML hrng hl ea hb
      have hw0 : 0 < w := by omega
      rw [e]; simp only [BvUn.apply, (Vik.rotateLeft_val x k hw0).1]
    case rotateRight k =>
      obtain ⟨hs, hsw, e⟩ := rotOk_sound hwP htP hG hML hrng hl ea hb
      have hw0 : 0 < w := by omega
      have hk : 0 < k % w := by
        rcases Nat.eq_zero_or_pos (k % w) with h | h
        · rw [h, Nat.sub_zero, Nat.mod_self] at hs; omega
        · exact h
      have hm : (w - k % w) % w = w - k % w := Nat.mod_eq_of_lt (by have := Nat.mod_lt k hw0; omega)
      rw [e]; simp only [BvUn.apply, Vik.rotateRight_val x k hw0 hk, (Vik.rotateLeft_val x _ hw0).1, hm]
    case signExtend k =>
      simp only [Bool.and_eq_true, decide_eq_true_eq] at hb
      rw [casesOk_sound hwP htP hG hML hrng hb.2 (by rw [evf_shift, la, Ring.intCast_add]) la,
        negS_int MI ρ hwP rfl ea]
      simp only [BvUn.apply, BvUn.width, Vik.signExtend_val, decide_eq_true_eq]

include hM hw ht hwP htP hG hML hrng in
/-- The binary operations through rules. -/
theorem bvbin_ops_sound {i w a b : Nat} {op : BvBin} (hn : SP.nodes[i]? = some (.bvbin op w a b))
    (hl : tr.at i < P.nodes.length) (hb : tr.bvOk SP P i = true) (ih : ∀ c < i, ρ (tr.at c) = enc (vs c))
    (hat : ∀ c < i, tr.at c < P.nodes.length) :
    ρ (tr.at i) = enc (vs i) := by
  have hai := children_lt hw hn a (by simp [SNode.children])
  have hbi := children_lt hw hn b (by simp [SNode.children])
  have hil := lt_of_get hn
  have hty := typed_at hM hw ht hn
  simp only [Surf.nodeTyped, Bool.and_eq_true] at hty
  obtain ⟨x, hx, ea, la⟩ := bv_child hM hw ht hwP (by omega) hty.1.1 (ih a hai)
  obtain ⟨y, hy, eb, lb⟩ := bv_child hM hw ht hwP (by omega) hty.1.2 (ih b hbi)
  have hv := Surf.val_node (I := Ms.toInterp) hw hn
  simp only [Surf.evalNode, surf_get_values, hx, hy, bvOf_ofBv] at hv
  rw [hv, ofBv_num, enc_num]
  have cst : ∀ {c : Int}, constOf (linOf P tr b) = some c → Vik.val y = c := fun hc => by
    have e := constOf_eval hc (fun u => (ρ u).toR)
    change evf ρ (linOf P tr b) = _ at e
    rw [lb] at e; exact cast_inj e
  simp only [Trace.bvOk, hn] at hb
  split at hb
  · rename_i ca cb hca hcb
    have e := constOf_eval hca (fun u => (ρ u).toR)
    change evf ρ (linOf P tr a) = _ at e
    rw [la] at e
    have e' := cast_inj e
    subst e'
    have := cst hcb
    subst this
    rw [Low.val_num hwP (by simpa using hb), bvbinConst, ofInt_val, ofInt_val]
  · unfold Trace.binOk at hb
    cases op <;> (try simp only [Bool.false_eq_true] at hb)
    case add =>
      rw [modOk_sound hwP htP hG hML hrng hb (V := Vik.val x + Vik.val y) (by rw [evf_add, la, lb, Ring.intCast_add])]
      simp only [BvBin.apply, BvBin.width, (Vik.add_val x y).1]
    case sub =>
      rw [modOk_sound hwP htP hG hML hrng hb (V := Vik.val x - Vik.val y) (by rw [evf_sub, la, lb, Ring.intCast_sub])]
      simp only [BvBin.apply, BvBin.width, (Vik.sub_val x y).1]
    case mul =>
      split at hb
      · rename_i p hp
        have e := mul2_eval (fun u => (ρ u).toR) hp
        change evf ρ p = evf ρ (linOf P tr a) * evf ρ (linOf P tr b) at e
        rw [la, lb, ← Ring.intCast_mul] at e
        rw [modOk_sound hwP htP hG hML hrng hb e]
        simp only [BvBin.apply, BvBin.width, Vik.val, BitVec.toNat_mul, Int.natCast_emod, Int.natCast_mul,
          Int.natCast_pow, Int.cast_ofNat_Int]
      · cases hb
    case comp =>
      rw [casesOk_sound hwP htP hG hML hrng hb (evf_const ρ 1) (evf_const ρ 0),
        eqI_int MI ρ (ev_same_num MI ρ ea) (ev_same_num MI ρ eb)]
      simp only [BvBin.apply, BvBin.width, decide_eq_true_eq]
      by_cases h : x = y
      · subst h; simp [Vik.val]
      · have : Vik.val x ≠ Vik.val y := fun e => h (Vik.val_inj.mp e)
        rw [if_neg this]; simp [h, Vik.val]
    case shl =>
      split at hb
      · rename_i c hc
        have hyc := cst hc
        have hyn : (y.toNat : Int) = c := hyc
        have hct : c.toNat = y.toNat := by omega
        simp only [BvBin.apply, BvBin.width, BitVec.shiftLeft_eq', Vik.shl_const_val]
        split at hb
        · rw [if_pos (by omega), Low.val_num hwP (by simpa using hb)]
        · rw [if_neg (by omega), modOk_sound hwP htP hG hML hrng hb (V := 2 ^ c.toNat * Vik.val x)
            (by rw [evf_scale, la, Ring.intCast_mul])]
          rw [hct, Int.mul_comm]; simp
      · cases hb
    case lshr =>
      split at hb
      · rename_i c hc
        have hyc := cst hc
        have hyn : (y.toNat : Int) = c := hyc
        have hct : c.toNat = y.toNat := by omega
        simp only [BvBin.apply, BvBin.width, BitVec.ushiftRight_eq']
        split at hb
        · rw [Vik.lshr_const_far x _ (by omega), Low.val_num hwP (by simpa using hb)]
        · rw [(Vik.lshr_const_val x _ (by omega)).1, divOk_sound hwP htP hG hML hrng hb la, hct]; simp
      · cases hb
    case udiv =>
      split at hb
      · rename_i c hc
        have hyc := cst hc
        simp only [BvBin.apply, BvBin.width, Vik.udiv_val]
        split at hb
        · rename_i h0
          rw [if_pos (by omega), Low.val_num hwP (by simpa using hb)]
        · rename_i h0
          rw [if_neg (by omega), divOk_sound hwP htP hG hML hrng hb la, hyc]; simp
      · cases hb
    case urem =>
      split at hb
      · rename_i c hc
        have hyc := cst hc
        simp only [BvBin.apply, BvBin.width]
        rw [show x.umod y = x % y from rfl, Vik.urem_val]
        split at hb
        · rename_i h0
          rw [if_pos (by omega), beq_iff_eq.mp hb, ea]
        · rename_i h0
          rw [if_neg (by omega), divOk_sound hwP htP hG hML hrng hb la, hyc]; simp
      · cases hb
    case ashr =>
      split at hb
      · rename_i c hc
        have hyc := cst hc
        have hyn : (y.toNat : Int) = c := hyc
        simp only [Bool.and_eq_true, decide_eq_true_eq] at hb
        obtain ⟨hw0, hb⟩ := hb
        simp only [BvBin.apply, BvBin.width, BitVec.sshiftRight_eq']
        unfold Trace.ashrOk at hb
        split at hb
        · rename_i v w' lo hi hs
          simp only [Bool.and_eq_true, beq_iff_eq] at hb
          obtain ⟨rfl, hb⟩ := hb
          rw [sym_val hwP hG hs]
          simp only [Rule.ival]
          split at hb
          · rename_i hwc
            rw [casesOk_sound hwP htP hG hML hrng hb (evf_const ρ (-1)) (evf_const ρ 0),
              negS_int MI ρ hwP rfl ea, intOf_num, Vik.ashr_far_val x _ (by omega) hw0]
            simp only [decide_eq_true_eq]
          · rename_i hwc
            rw [sym_val hwP hG (beq_iff_eq.mp hb)]
            simp only [Rule.ival, if_true, ea, intOf_num]
            rw [Vik.ashr_const_val, Vik.toInt_eq x hw0]
            have hct : c.toNat = y.toNat := by omega
            simp only [signedI, hct]
        · cases hb
      · cases hb
    case and | or | xor | nand | nor | xnor =>
      exact bitwiseOk_sound hwP htP hG hML hrng hb hl (hat a hai) (hat b hbi) ea eb
    case sdiv | srem | smod =>
      split at hb
      · rename_i c hc
        simp only [Bool.and_eq_true, decide_eq_true_eq] at hb
        exact sdivOk_sound hwP htP hG hML hrng hb.1 ea (cst hc) (by simp) hb.2
      · cases hb
    all_goals (split at hb <;> cases hb)

include hM hw ht hwP htP hG hML hrng in
/-- Each bit-vector operation that `Trace.bvOk` accepts takes the value of its
    surface node. -/
theorem bvOk_sound {i : Nat} {n : SNode} (hn : SP.nodes[i]? = some n) (hl : tr.at i < P.nodes.length)
    (hb : tr.bvOk SP P i = true) (ih : ∀ c < i, ρ (tr.at c) = enc (vs c))
    (hat : ∀ c < i, tr.at c < P.nodes.length) : ρ (tr.at i) = enc (vs i) := by
  have hil := lt_of_get hn
  have hty := typed_at hM hw ht hn
  have hv := Surf.val_node (I := Ms.toInterp) hw hn
  cases n with
  | bvun op w a => exact bvun_ops_sound hM hw ht hwP htP hG hML hrng hn hl hb ih
  | bvbin op w a b => exact bvbin_ops_sound hM hw ht hwP htP hG hML hrng hn hl hb ih hat
  | bv2int sg w a =>
    cases sg with
    | false => simp [Trace.bvOk, hn] at hb
    | true =>
    have hai := children_lt hw hn a (by simp [SNode.children])
    simp only [Surf.nodeTyped, Bool.and_eq_true] at hty
    obtain ⟨x, hx, ea, la⟩ := bv_child hM hw ht hwP (by omega) hty.1 (ih a hai)
    simp only [Surf.evalNode, surf_get_values, hx, bvOf_ofBv, if_true] at hv
    rw [hv, enc_num]
    simp only [Trace.bvOk, hn] at hb
    split at hb
    · rename_i ca hca
      simp only [Bool.and_eq_true, decide_eq_true_eq] at hb
      have e := constOf_eval hca (fun u => (ρ u).toR)
      change evf ρ (linOf P tr a) = _ at e
      rw [la] at e
      have e' := cast_inj e
      subst e'
      rw [Low.val_num hwP (by simpa using hb.2), Vik.toInt_eq x hb.1]; rfl
    · simp only [Bool.and_eq_true, decide_eq_true_eq] at hb
      rw [signedOk_sound hwP htP hG hML hrng hb.2 ea, Vik.toInt_eq x hb.1]; rfl
  | int2bv w a =>
    have hai := children_lt hw hn a (by simp [SNode.children])
    simp only [Surf.nodeTyped, Bool.and_eq_true] at hty
    obtain ⟨k, hk⟩ := v_int hM hw ht (a := a) (by omega) (by simpa [SProblem.isInt, SProblem.sort] using hty.1)
    have ea := ih a hai
    rw [hk, enc_num] at ea
    have la := lin_ev hwP (ih a hai)
    rw [hk, toR_num] at la
    simp only [Surf.evalNode, surf_get_values, hk, intOf_num] at hv
    rw [hv, ofBv_num, enc_num, Vik.val_ofInt]
    simp only [Trace.bvOk, hn] at hb
    split at hb
    · rename_i ca hca
      have e := constOf_eval hca (fun u => (ρ u).toR)
      rw [la] at e
      have e' := cast_inj e
      subst e'
      exact Low.val_num hwP (by simpa using hb)
    · rw [sym_val hwP hG (beq_iff_eq.mp hb)]
      simp only [Rule.ival, ea, intOf_num]
  | _ => simp [Trace.bvOk, hn] at hb

end

end Vik.Lower
