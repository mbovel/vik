import Vik.Surface
import Vik.Lower.Shape
import Vik.Cert.Extend
import Vik.Rules

/-!
# Lowering traces

A surface script, the lowered problem Vik solves, and a trace between them:
the lowered node of each surface node, and the symbols the lowering mints
after those of the script, each with its reason (the value of an `ite`, the
store of an array sort). `Trace.check` checks the trace node by node, against
the shape the lowering gives each operation (`expect`).

This covers the operations that lower to themselves: Booleans, linear integer
and real arithmetic, uninterpreted functions, datatypes, arrays, and `ite` on
terms; and the operations on bit-vectors that lower to linear terms and
comparisons of their values.
-/

set_option linter.unusedSectionVars false

namespace Vik.Lower

open Lean.Grind Vik.Real Vik.Low Vik.Surf

/-- The lowered sort of a surface sort: a bit-vector is its integer, an array
    the function from its indices. -/
def lowerSort : SSort → SortDef
  | .bool => .bool
  | .int => .int
  | .real => .real
  | .user => .user
  | .bv _ => .int
  | .fn as r => .fn as r
  | .array i e => .fn [i] e

/-- A surface script: its sorts and symbols, its nodes with their sorts, the
    assertions, and its datatypes (for each sort, the constructors with their
    testers and selectors). -/
structure SProblem where
  sig : SSig
  nodes : List SNode
  sortOf : List Nat
  asserts : List Nat
  datatypes : List (Nat × List (Nat × Nat × List Nat))

/-- Why the lowering mints a symbol. -/
inductive Mint where
  /-- The value of the `ite` at surface node `i`. -/
  | ite (i : Nat)
  /-- The store of the arrays of surface sort `s`. -/
  | store (s : Nat)
  deriving DecidableEq, Repr

/-- Why a lowered input holds. -/
inductive InputWhy where
  /-- It is the lowering of the surface assertion at node `a`. -/
  | assert (a : Nat)
  /-- `c → k = t` for the `ite` symbol `k`. -/
  | iteThen (k : Nat)
  /-- `¬c → k = e` for the `ite` symbol `k`. -/
  | iteElse (k : Nat)
  /-- `0 ≤ t ≤ 2^w - 1` for the lowered node `t` of the surface node `i`, a
      bit-vector of width `w`. -/
  | range (i : Nat)
  deriving DecidableEq, Repr

structure Trace where
  /-- The lowered node of each surface node. -/
  node : List Nat
  /-- The symbols after those of the script, in order. -/
  mints : List Mint
  /-- Why each lowered input holds, in order. -/
  inputs : List InputWhy

/-! ## The expected shapes -/

/-- Consecutive pairs. -/
def consec {α : Type} : List α → List (α × α)
  | a :: b :: rest => (a, b) :: consec (b :: rest)
  | _ => []

/-- All pairs, in order. -/
def allPairs {α : Type} : List α → List (α × α)
  | [] => []
  | a :: rest => rest.map (a, ·) ++ allPairs rest

/-- The conjunction of conditions, itself when there is one. -/
def chainS : List Shape → Shape
  | [s] => s
  | ss => .and ss

/-- `(=> a₁ … aₙ)`, to the right. -/
def impliesS : List Shape → Option Shape
  | [] => none
  | [s] => some s
  | s :: rest => (impliesS rest).map (.implies s)

/-- `(xor a₁ … aₙ)`, to the left. -/
def xorS : List Shape → Option Shape
  | [] => none
  | s :: rest => some (rest.foldl (fun acc x => .not (.iff acc x)) s)

/-- The sum of linear combinations. -/
def sumForms : List (Lia.Form × Int) → Lia.Form × Int
  | [] => ([], 0)
  | (f, c) :: ps => (f ++ (sumForms ps).1, c + (sumForms ps).2)

/-- The product of two linear combinations, one of them a constant. -/
def mul2 (p q : Lia.Form × Int) : Option (Lia.Form × Int) :=
  if p.1.isEmpty then some (Lia.scale (p.2 : Rat) q.1, p.2 * q.2)
  else if q.1.isEmpty then some (Lia.scale (q.2 : Rat) p.1, p.2 * q.2)
  else none

def mulForms : List (Lia.Form × Int) → Option (Lia.Form × Int)
  | [] => some ([], 1)
  | p :: ps => (mulForms ps).bind (mul2 p)

section
variable (SP : SProblem) (P : Problem) (tr : Trace)

/-- The lowered node of surface node `i`. -/
def Trace.at (i : Nat) : Nat := tr.node[i]?.getD 0

def SProblem.sort (i : Nat) : Nat := SP.sortOf[i]?.getD 0

def SProblem.isBool (i : Nat) : Bool := SP.sig.is (SP.sort i) .bool
def SProblem.isInt (i : Nat) : Bool := SP.sig.is (SP.sort i) .int
def SProblem.isNum (i : Nat) : Bool := SP.sig.numeric (SP.sort i)

/-- The width of surface sort `s`, if it is a sort of bit-vectors. -/
def SProblem.sortWidth (s : Nat) : Option Nat :=
  match SP.sig.sorts[s]? with
  | some (SSort.bv w) => some w
  | _ => none

/-- The width of surface node `i`, if it is a bit-vector. -/
def SProblem.width (i : Nat) : Option Nat := SP.sortWidth (SP.sort i)

def SProblem.isBv (i : Nat) : Bool := (SP.width i).isSome

/-- Whether surface node `i` lowers to an integer: an integer or a bit-vector. -/
def SProblem.isIntLike (i : Nat) : Bool := SP.isInt i || SP.isBv i

/-- Whether surface node `i` lowers to a number. -/
def SProblem.isArith (i : Nat) : Bool := SP.isNum i || SP.isBv i

/-- The sorts of bit-vectors, with their widths. -/
def SProblem.bvIndex : List (Nat × Nat) :=
  (List.range SP.sig.sorts.length).filterMap fun s => (SP.sortWidth s).map (s, ·)

/-- Surface node `a` as a lowered term. -/
def leaf (a : Nat) : Shape := if SP.isBool a then .bool (tr.at a) else .same (tr.at a)

/-- Surface node `a`, a formula. -/
def form (a : Nat) : Shape := .bool (tr.at a)

/-- Surface node `a` as a linear combination. -/
def linOf (a : Nat) : Lia.Form × Int := lin P.nodes (tr.at a)

/-- `a < b` on integers: `a + 1 ≤ b`. -/
def ltInt (a b : Nat) : Shape := .le (.num (linOf P tr a).1 ((linOf P tr a).2 + 1)) (.same (tr.at b))

/-- The equality of two lowered terms of surface sort `s`. -/
def eqS (num : Bool) (a b : Shape) : Shape := if num then .and [.le a b, .le b a] else .eq a b

def Trace.nsd : Nat := SP.sig.decls.length

/-- `Σ_{j<k} 2^(j·w) · p`: `p` repeated `k` times. -/
def repeatForm (w : Nat) (p : Lia.Form × Int) : Nat → Lia.Form × Int
  | 0 => ([], 0)
  | k + 1 =>
    let r := repeatForm w p k
    (r.1 ++ Lia.scale ((2 ^ (k * w) : Int) : Rat) p.1, r.2 + 2 ^ (k * w) * p.2)

/-- `negative(t, w)`: the sign of the lowered node `t`, `2^(w-1) ≤ t`. -/
def negS (t w : Nat) : Shape :=
  match P.nodes[t]? with
  | some (.num v) => if 2 ^ (w - 1) ≤ v then .tt else .ff
  | _ => .le (.num [] (2 ^ (w - 1))) (.same t)

/-- A signed comparison of the lowered nodes of `l` and `r`, of width `w`,
    from the unsigned one `u`: `(nl ∧ ¬nr) ∨ ((nl ↔ nr) ∧ u)`. -/
def signedS (w l r : Nat) (u : Shape) : Shape :=
  .or [.and [negS P (tr.at l) w, .not (negS P (tr.at r) w)],
    .and [.iff (negS P (tr.at l) w) (negS P (tr.at r) w), u]]

/-- A comparison of bit-vectors of width `w`. -/
def cmpS (op : BvCmp) (w a b : Nat) : Shape :=
  match op with
  | .ult => ltInt P tr a b
  | .ule => .le (.same (tr.at a)) (.same (tr.at b))
  | .ugt => ltInt P tr b a
  | .uge => .le (.same (tr.at b)) (.same (tr.at a))
  | .slt => signedS P tr w a b (ltInt P tr a b)
  | .sle => signedS P tr w a b (.le (.same (tr.at a)) (.same (tr.at b)))
  | .sgt => signedS P tr w b a (ltInt P tr b a)
  | .sge => signedS P tr w b a (.le (.same (tr.at b)) (.same (tr.at a)))

/-- Whether the lowered symbol `k` is minted for the `ite` at `i`, or one
    before it with the same lowered children. -/
def Trace.iteOf (k i : Nat) : Bool :=
  k ≥ SP.sig.decls.length &&
    match tr.mints[k - SP.sig.decls.length]?, SP.nodes[i]? with
    | some (.ite i'), some (.ite c a b) =>
      i' ≤ i &&
        match SP.nodes[i']? with
        | some (.ite c' a' b') => tr.at c' == tr.at c && tr.at a' == tr.at a && tr.at b' == tr.at b
        | _ => false
    | _, _ => false

/-- Whether the lowered symbol `n` is minted for the store of surface sort `s`. -/
def Trace.storeOf (n s : Nat) : Bool :=
  n ≥ SP.sig.decls.length && tr.mints[n - SP.sig.decls.length]? == some (.store s)

/-- The shape of the lowering of surface node `i`. -/
def expect (i : Nat) : Option Shape :=
  let T := tr.at
  let L := leaf SP tr
  let F := form tr
  match SP.nodes[i]? with
  | none => none
  | some n =>
  match n with
  | .const k => if k < SP.sig.decls.length then some (.const k) else none
  | .app f args => some (.app (.same (T f)) (args.map L))
  | .num v => some (.num [] v)
  | .tt => some .tt
  | .ff => some .ff
  | .not a => some (.not (F a))
  | .and as => some (.and (as.map F))
  | .or as => some (.or (as.map F))
  | .implies as => impliesS (as.map F)
  | .xor as => xorS (as.map F)
  | .iff as => some (chainS ((consec as).map fun p => .iff (F p.1) (F p.2)))
  | .eq as =>
    match as with
    | [] => none
    | a :: _ =>
      if SP.isBool a then none
      else some (chainS ((consec as).map fun p => eqS (SP.isArith a) (.same (T p.1)) (.same (T p.2))))
  | .distinct as =>
    match as with
    | [] => none
    | a :: _ =>
      if SP.isBool a then none
      else if SP.isIntLike a then
        some (.and ((allPairs as).map fun p => .or [ltInt P tr p.1 p.2, ltInt P tr p.2 p.1]))
      else if SP.isNum a then
        some (.and ((allPairs as).map fun p => .or [.lt (.same (T p.1)) (.same (T p.2)), .lt (.same (T p.2)) (.same (T p.1))]))
      else some (.and ((allPairs as).map fun p => .not (.eq (.same (T p.1)) (.same (T p.2)))))
  | .le as => some (chainS ((consec as).map fun p => .le (.same (T p.1)) (.same (T p.2))))
  | .ge as => some (chainS ((consec as).map fun p => .le (.same (T p.2)) (.same (T p.1))))
  | .lt as =>
    if as.all SP.isInt then some (chainS ((consec as).map fun p => ltInt P tr p.1 p.2))
    else some (chainS ((consec as).map fun p => .lt (.same (T p.1)) (.same (T p.2))))
  | .gt as =>
    if as.all SP.isInt then some (chainS ((consec as).map fun p => ltInt P tr p.2 p.1))
    else some (chainS ((consec as).map fun p => .lt (.same (T p.2)) (.same (T p.1))))
  | .ite c a b =>
    if SP.isBool i then some (.ite (F c) (F a) (F b))
    else
      match P.nodes[T i]? with
      | some (.const k) => if tr.iteOf SP k i then some (.const k) else none
      | _ => none
  | .add as => some (.num (sumForms (as.map (linOf P tr))).1 (sumForms (as.map (linOf P tr))).2)
  | .sub as =>
    match as with
    | [] => some (.num [] 0)
    | a :: rest =>
      let r := sumForms (rest.map (linOf P tr))
      some (.num ((linOf P tr a).1 ++ Lia.neg r.1) ((linOf P tr a).2 - r.2))
  | .neg a => some (.num (Lia.neg (linOf P tr a).1) (-(linOf P tr a).2))
  | .mul as => (mulForms (as.map (linOf P tr))).map fun p => .num p.1 p.2
  | .select a x => some (.app (.same (T a)) [L x])
  | .store a x v =>
    match P.nodes[T i]? with
    | some (.app g _) =>
      match P.nodes[g]? with
      | some (.const n) => if tr.storeOf SP n (SP.sort a) then some (.app (.const n) [L a, L x, L v]) else none
      | _ => none
    | _ => none
  | .bvlit w v => if v < 2 ^ w then some (.num [] v) else none
  | .bvun op w a =>
    match op with
    | .not => some (.num (Lia.neg (linOf P tr a).1) (2 ^ w - 1 - (linOf P tr a).2))
    | .zeroExtend _ => some (.same (T a))
    | .repeat k => some (.num (repeatForm w (linOf P tr a) k).1 (repeatForm w (linOf P tr a) k).2)
    | .rotateLeft k | .rotateRight k => if k % w = 0 then some (.same (T a)) else none
    | .extract i j => if j = 0 && i + 1 = w then some (.same (T a)) else none
    | .signExtend k => if k = 0 then some (.same (T a)) else none
    | .neg => none
  | .concat _ wb a b =>
    some (.num (Lia.scale ((2 ^ wb : Int) : Rat) (linOf P tr a).1 ++ (linOf P tr b).1)
      (2 ^ wb * (linOf P tr a).2 + (linOf P tr b).2))
  | .bv2int false _ a => some (.same (T a))
  | .bv2int true _ _ => none
  | .bvcmp op w a b => if w = 0 then none else some (cmpS P tr op w a b)
  | .bvbin _ _ _ _ => none
  | .int2bv _ _ => none

/-- The facts of the `ite` symbol `k`: `c → k = t`, or `¬c → k = e`. -/
def iteFact (k : Nat) (thn : Bool) : Option Shape :=
  if k < SP.sig.decls.length then none
  else
  match tr.mints[k - SP.sig.decls.length]? with
  | some (.ite i) =>
    match SP.nodes[i]? with
    | some (.ite c a b) =>
      if thn then some (.implies (form tr c) (eqS (SP.isArith i) (.const k) (.same (tr.at a))))
      else some (.implies (.not (form tr c)) (eqS (SP.isArith i) (.const k) (.same (tr.at b))))
    | _ => none
  | _ => none

/-! ## The checks -/

/-- Whether the surface sorts refer to declared sorts only. -/
def SProblem.sortsOk : Bool :=
  let n := SP.sig.sorts.length
  SP.sig.sorts.all (fun s =>
    match s with
    | .fn as r => as.all (· < n) && r < n
    | .array i e => i < n && e < n
    | _ => true) &&
  SP.sig.decls.all (· < n) && SP.sortOf.all (· < n) &&
  SP.datatypes.all fun (d, cs) =>
    d < n && SP.sig.sorts[d]? == some .user && cs.all fun (C, T, Ss) =>
      C < SP.sig.decls.length && T < SP.sig.decls.length && Ss.all (· < SP.sig.decls.length)

/-- Whether the lowered signature extends the surface one: its sorts lowered,
    then the sorts of stores; its symbols, then the minted ones. -/
def Trace.sigOk : Bool :=
  let ns := SP.sig.sorts.length
  P.sig.sorts.take ns == SP.sig.sorts.map lowerSort &&
  P.sig.decls.take SP.sig.decls.length == SP.sig.decls &&
  P.sig.decls.length == SP.sig.decls.length + tr.mints.length &&
  -- the sorts after those of the script are those of stores
  (List.range P.sig.sorts.length).all (fun s =>
    s < ns ||
      match P.sig.sorts[s]? with
      | some (.fn [arr, idx, el] arr') => arr' == arr && SP.sig.sorts[arr]? == some (.array idx el)
      | _ => false) &&
  -- each minted symbol, of the sort of its reason
  (List.range tr.mints.length).all (fun m =>
    let k := SP.sig.decls.length + m
    match tr.mints[m]? with
    | some (.ite i) =>
      (match SP.nodes[i]? with | some (.ite _ _ _) => true | _ => false) &&
        !SP.isBool i && P.sig.decls[k]? == some (SP.sort i)
    | some (.store s) =>
      match SP.sig.sorts[s]?, P.sig.decls[k]? with
      | some (.array idx el), some ds =>
        ns ≤ ds && P.sig.sorts[ds]? == some (.fn [s, idx, el] s) &&
          -- the only store of its sort
          (List.range tr.mints.length).all fun m' =>
            m' == m || match tr.mints[m']? with
              | some (.store _) => P.sig.decls[SP.sig.decls.length + m']? != some ds
              | _ => true
      | _, _ => false
    | none => false) &&
  P.stores.all (fun n => n ≥ SP.sig.decls.length &&
    match tr.mints[n - SP.sig.decls.length]? with
    | some (.store _) => true
    | _ => false) &&
  P.datatypes == SP.datatypes && P.bvIndex == SP.bvIndex &&
  -- the nodes of bit-vector sorts are symbols and applications
  (List.range P.nodes.length).all fun j =>
    (SP.bvIndex.lookup (P.sortOf[j]?.getD 0)).isNone ||
      match P.nodes[j]? with
      | some (.const _) | some (.app _ _) => true
      | _ => false

/-- Whether a lowered input holds by its reason. -/
def Trace.inputOk (f : Nat) : InputWhy → Bool
  | .assert a => SP.asserts.contains a && fits P.sig P.nodes P.sortOf f (form tr a)
  | .iteThen k => match iteFact SP tr k true with
    | some s => fits P.sig P.nodes P.sortOf f s
    | none => false
  | .iteElse k => match iteFact SP tr k false with
    | some s => fits P.sig P.nodes P.sortOf f s
    | none => false
  | .range i =>
    match SP.width i with
    | some w =>
      i < SP.nodes.length &&
        fits P.sig P.nodes P.sortOf f
          (.and [.le (.num [] 0) (.same (tr.at i)), .le (.same (tr.at i)) (.num [] (2 ^ w - 1))])
    | none => false

/-- Whether the trace lowers the script to the problem. -/
def Trace.check (A : Answer) : Bool :=
  Surf.wellFormed SP.nodes && Surf.typed SP.sig SP.nodes SP.sortOf && SP.sortsOk && tr.sigOk SP P &&
  tr.node.length == SP.nodes.length &&
  (List.range SP.nodes.length).all (fun i =>
    tr.at i < P.nodes.length &&
      match expect SP P tr i with
      | some s => fits P.sig P.nodes P.sortOf (tr.at i) s
      | none => false) &&
  P.inputs.length == tr.inputs.length &&
  (P.inputs.zip tr.inputs).all (fun p => p.1 < P.nodes.length && tr.inputOk SP P p.1 p.2) &&
  A.assumed == []

end

end Vik.Lower
