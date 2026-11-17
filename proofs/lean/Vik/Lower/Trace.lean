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

/-- A rule of the lowering that mints integer symbols (the rules of
    `LoweringCheck.check`), over lowered nodes. -/
inductive Rule where
  /-- `modulo(v, w, lo, hi)`: `v mod 2^w`, and `v div 2^w` unless `hi - lo < 4`. -/
  | modulo (v w : Nat) (lo hi : Int)
  /-- `cases(c, thn, els, lo, hi)`: `thn` if the formula `c` holds, `els` otherwise. -/
  | cases (c thn els : Nat) (lo hi : Int)
  /-- `divide(v, d, qmax)`: `v div d` and `v mod d`. -/
  | divide (v : Nat) (d qmax : Int)
  /-- `sdivide(t, w, d, floor)`: the quotient and remainder of the signed
      value of `t` by `d`, floored or truncated. -/
  | sdivide (t w : Nat) (d : Int) (floor : Bool)
  /-- `int2bv(t, w)`: `t mod 2^w` and `t div 2^w`. -/
  | int2bv (t w : Nat)
  /-- `smodz(a, c, w)`: whether `bvsmod` by the constant `c` corrects the
      remainder. -/
  | smodz (a : Nat) (c : Int) (w : Nat)
  deriving DecidableEq, Repr

/-- The lowered nodes a rule applies to. -/
def Rule.args : Rule → List Nat
  | .modulo v _ _ _ | .divide v _ _ | .sdivide v _ _ _ | .int2bv v _ | .smodz v _ _ => [v]
  | .cases c t e _ _ => [c, t, e]

/-- The number of symbols of a rule. -/
def Rule.nsyms : Rule → Nat
  | .modulo _ _ lo hi => if hi - lo < 4 then 1 else 2
  | .cases .. | .smodz .. => 1
  | _ => 2

/-- The signed value of `t`, of width `w`. -/
def signedI (w : Nat) (t : Int) : Int := if 2 ^ (w - 1) ≤ t then t - 2 ^ w else t

/-- The signed divisor of `bvsmod` by the constant `c`. -/
def smodT (c : Int) (w : Nat) : Int := if 2 ^ w / 2 ≤ c then c - 2 ^ w else c

/-- `smodz`: `1` when the signs of `a` and of the divisor differ and the
    truncated remainder is not zero. -/
def smodzVal (a c : Int) (w : Nat) : Int :=
  let t := smodT c w
  let neg := decide (2 ^ (w - 1) ≤ a)
  let differ := if 0 < t then neg else !neg
  if differ && (signedI w a).tmod t.natAbs != 0 then 1 else 0

/-- The value of symbol `j` of a rule, the integers of its arguments `ι` and
    the truths of its conditions `τ`. -/
def Rule.ival (ι : Nat → Int) (τ : Nat → Bool) : Rule → Nat → Int
  | .modulo v w _ _, 0 => ι v % 2 ^ w
  | .modulo v w _ _, _ => ι v / 2 ^ w
  | .cases c t e _ _, _ => if τ c then ι t else ι e
  | .divide v d _, 0 => ι v / d
  | .divide v d _, _ => ι v % d
  | .sdivide t w d fl, 0 => if fl then signedI w (ι t) / d else (signedI w (ι t)).tdiv d
  | .sdivide t w d fl, _ => if fl then signedI w (ι t) % d else (signedI w (ι t)).tmod d
  | .int2bv t w, 0 => ι t % 2 ^ w
  | .int2bv t w, _ => ι t / 2 ^ w
  | .smodz a c w, _ => smodzVal (ι a) c w

/-- Why the lowering mints a symbol. -/
inductive Mint where
  /-- The value of the `ite` at surface node `i`. -/
  | ite (i : Nat)
  /-- The store of the arrays of surface sort `s`. -/
  | store (s : Nat)
  /-- Symbol `j` of a rule. -/
  | rule (r : Rule) (j : Nat)
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
  /-- Fact `f` of rule `r`, `aux` the node of the signed value of `sdivide`,
      or of the remainder for `smodz`. -/
  | rule (r : Rule) (f aux : Nat)
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

/-- `negative(t, w)`: the sign of the lowered node `t`, `2^(w-1) ≤ t`. -/
def negS (t w : Nat) : Shape :=
  match P.nodes[t]? with
  | some (.num v) => if 2 ^ (w - 1) ≤ v then .tt else .ff
  | _ => .le (.num [] (2 ^ (w - 1))) (.same t)

/-! ## The symbols of rules -/

/-- The rank of a symbol: one more than its index among the minted symbols if
    a rule mints it, `0` otherwise. A rule's arguments only depend on the
    symbols of rules minted before it. -/
def Trace.rank (k : Nat) : Nat :=
  if SP.sig.decls.length ≤ k then
    match tr.mints[k - SP.sig.decls.length]? with
    | some (.rule _ _) => k - SP.sig.decls.length + 1
    | _ => 0
  else 0

/-- The rule and index of the symbol of lowered node `x`, if a rule mints it. -/
def Trace.symAt (x : Nat) : Option (Rule × Nat) :=
  match P.nodes[x]? with
  | some (.const k) =>
    if SP.sig.decls.length ≤ k then
      match tr.mints[k - SP.sig.decls.length]? with
      | some (.rule r j) => some (r, j)
      | _ => none
    else none
  | _ => none

/-- The lowered node of symbol `j` of rule `r`. -/
def Trace.symNode (r : Rule) (j : Nat) : Option Nat :=
  let m := tr.mints.idxOf (.rule r j)
  if m < tr.mints.length then
    let x := P.nodes.idxOf (.const (SP.sig.decls.length + m))
    if x < P.nodes.length then some x else none
  else none

/-! ## Intervals of lowered integers -/

/-- The width of a lowered node of a bit-vector: of its sort, or of a
    surface node it lowers. -/
def Trace.bvWidth (l : Nat) : Option Nat :=
  match SP.sortWidth (P.sortOf[l]?.getD 0) with
  | some w => some w
  | none => (List.range SP.nodes.length).findSome? fun i => if tr.at i == l then SP.width i else none

/-- The interval of a linear combination, from those of its unknowns. -/
def linIv (leaf : Nat → Option (Int × Int)) : Lia.Form → Option (Int × Int)
  | [] => some (0, 0)
  | (u, a) :: f =>
    if a.den = 1 then
      match leaf u, linIv leaf f with
      | some (l, h), some (L, H) => if 0 ≤ a.num then some (a.num * l + L, a.num * h + H) else some (a.num * h + L, a.num * l + H)
      | _, _ => none
    else none

/-- The interval of lowered node `x`, from those of its unknowns. -/
def nodeIv (leaf : Nat → Option (Int × Int)) (x : Nat) : Option (Int × Int) :=
  (linIv leaf (lin P.nodes x).1).map fun p => (p.1 + (lin P.nodes x).2, p.2 + (lin P.nodes x).2)

/-- Whether lowered node `x` is of sort `Int`. -/
def isIntN (x : Nat) : Bool := P.sig.is (P.sortOf[x]?.getD 0) .int

/-- The constraint `g + gc ≥ 0` a comparison of integers `x` gives when it
    holds. -/
def condAtom (x : Nat) : List (Lia.Form × Int) :=
  match P.nodes[x]? with
  | some (.le a b) =>
    if isIntN P a && isIntN P b then [((lin P.nodes b).1 ++ Lia.neg (lin P.nodes a).1, (lin P.nodes b).2 - (lin P.nodes a).2)]
    else []
  | _ => []

/-- The constraints `g + gc ≥ 0` the formula `c` gives, or its negation: those
    of comparisons of integers. -/
def condCons (c : Nat) (pos : Bool) : List (Lia.Form × Int) :=
  if pos then
    match P.nodes[c]? with
    | some (.and xs) => xs.flatMap (condAtom P)
    | _ => condAtom P c
  else
    match P.nodes[c]? with
    | some (.le a b) =>
      if isIntN P a && isIntN P b then [((lin P.nodes a).1 ++ Lia.neg (lin P.nodes b).1, (lin P.nodes a).2 - (lin P.nodes b).2 - 1)]
      else []
    | _ => []

/-- The interval of a linear combination `f` narrowed by constraints: `f` is
    at least `-gc` more than `g`, or at most `gc` less than `-g`. -/
def refine (f : Lia.Form × Int) (cons : List (Lia.Form × Int)) (i : Option (Int × Int)) : Option (Int × Int) :=
  cons.foldl (fun acc gc => acc.map fun p =>
    (if (canon (f.1 ++ Lia.neg gc.1)).isEmpty then max p.1 (f.2 - gc.2) else p.1,
     if (canon (f.1 ++ gc.1)).isEmpty then min p.2 (f.2 + gc.2) else p.2)) i

def ivHull : Option (Int × Int) → Option (Int × Int) → Option (Int × Int)
  | some (a, b), some (c, d) => some (min a c, max b d)
  | _, _ => none

def ivSub (i : Option (Int × Int)) (lo hi : Int) : Bool :=
  match i with
  | some (a, b) => lo ≤ a && b ≤ hi
  | none => false

/-- The interval of branch `t` of `cases` when `c` is `pos`. -/
def branchIv (node : Nat → Option (Int × Int)) (c t : Nat) (pos : Bool) : Option (Int × Int) :=
  refine (lin P.nodes t) (condCons P c pos) (node t)

/-- The interval of symbol `j` of a rule, from those of the nodes. -/
def Rule.iv (node : Nat → Option (Int × Int)) : Rule → Nat → Option (Int × Int)
  | .modulo _ w _ _, 0 => some (0, 2 ^ w - 1)
  | .modulo v w _ _, _ => (node v).map fun p => (p.1 / 2 ^ w, p.2 / 2 ^ w)
  | .cases c t e _ _, _ => ivHull (branchIv P node c t true) (branchIv P node c e false)
  | .divide v d _, 0 => if 0 < d then (node v).map fun p => (p.1 / d, p.2 / d) else none
  | .divide _ d _, _ => if 0 < d then some (0, d - 1) else none
  | .sdivide t w d fl, 0 =>
    if 0 < d && 0 < w && ivSub (node t) 0 (2 ^ w - 1) then
      let half : Int := 2 ^ (w - 1)
      if fl then some ((-half) / d, (half - 1) / d)
      else some (-(half / d), (half - 1) / d)
    else none
  | .sdivide _ _ d fl, _ => if 0 < d then (if fl then some (0, d - 1) else some (-(d - 1), d - 1)) else none
  | .int2bv _ w, 0 => some (0, 2 ^ w - 1)
  | .int2bv _ _, _ => none
  | .smodz .., _ => some (0, 1)

/-- The interval of a leaf: a number, a symbol of a rule (`sym`), or a
    bit-vector. -/
def Trace.leafIv (sym : Rule → Nat → Option (Int × Int)) (l : Nat) : Option (Int × Int) :=
  match P.nodes[l]? with
  | some (.num v) => some (v, v)
  | some _ =>
    match tr.symAt SP P l with
    | some (r, j) => sym r j
    | none => (tr.bvWidth SP P l).map fun w => (0, 2 ^ w - 1)
  | none => none

/-- The intervals of the lowered nodes, unfolding `n` rules deep. -/
def Trace.ivAt : Nat → Nat → Option (Int × Int)
  | 0 => nodeIv P (tr.leafIv SP P fun _ _ => none)
  | n + 1 => nodeIv P (tr.leafIv SP P (Rule.iv P (Trace.ivAt n)))

def Trace.iv (x : Nat) : Option (Int × Int) := tr.ivAt SP P (tr.mints.length + 1) x

/-! ## The facts of rules -/

/-- `lo ≤ k ≤ hi`. -/
def intS (k : Nat) (lo hi : Int) : List Shape := [.le (.num [] lo) (.same k), .le (.same k) (.num [] hi)]

/-- `a = b` on integers. -/
def eqI (a b : Shape) : Shape := .and [.le a b, .le b a]

/-- The facts of rule `r`, its symbols at nodes `ks`, `aux` the node of the
    signed value of `sdivide` or of the remainder of `smodz`. -/
def Rule.facts (ks : List Nat) (aux : Nat) : Rule → List Shape
  | .modulo v w lo hi =>
    let m : Int := 2 ^ w
    let r := ks.getD 0 0
    intS r 0 (m - 1) ++
      if hi - lo < 4 then
        [.or ((List.range (hi - lo + 1).toNat).map fun (i : Nat) => eqI (.same v) (.num [(r, 1)] ((lo + (i : Int)) * m)))]
      else
        let q := ks.getD 1 0
        intS q lo hi ++ [eqI (.same v) (.num [(r, 1), (q, (m : Rat))] 0)]
  | .cases c t e lo hi =>
    let k := ks.getD 0 0
    intS k lo hi ++ [.implies (.bool c) (eqI (.same k) (.same t)), .implies (.not (.bool c)) (eqI (.same k) (.same e))]
  | .divide v d qmax =>
    let q := ks.getD 0 0
    let r := ks.getD 1 0
    intS q 0 qmax ++ intS r 0 (d - 1) ++ [eqI (.same v) (.num [(q, (d : Rat)), (r, 1)] 0)]
  | .sdivide t w d fl =>
    let q := ks.getD 0 0
    let r := ks.getD 1 0
    let half : Int := 2 ^ (w - 1)
    intS q (-(half / d) - 1) ((half - 1) / d) ++ intS r (if fl then 0 else -(d - 1)) (d - 1) ++
      [eqI (.same aux) (.num [(q, (d : Rat)), (r, 1)] 0)] ++
      if fl then []
      else [.implies (negS P t w) (.le (.same r) (.num [] 0)), .implies (.not (negS P t w)) (.le (.num [] 0) (.same r))]
  | .int2bv t w =>
    let k0 := ks.getD 0 0
    let k1 := ks.getD 1 0
    intS k0 0 (2 ^ w - 1) ++ [eqI (.same t) (.num [(k0, 1), (k1, ((2 ^ w : Int) : Rat))] 0)]
  | .smodz a c w =>
    let z := ks.getD 0 0
    let differ := if 0 < smodT c w then negS P a w else .not (negS P a w)
    let rz := eqI (.same aux) (.num [] 0)
    intS z 0 1 ++
      [.implies (.not differ) (eqI (.same z) (.num [] 0)), .implies rz (eqI (.same z) (.num [] 0)),
        .implies (.and [differ, .not rz]) (eqI (.same z) (.num [] 1))]

/-- Whether node `x` is `signed(t, w)`: the constant, or the `cases` tagged
    `signed`. -/
def Trace.signedOk (t w x : Nat) : Bool :=
  match P.nodes[t]? with
  | some (.num v) => P.nodes[x]? == some (.num (signedI w v))
  | _ =>
    match tr.symAt SP P x with
    | some (.cases c thn els lo hi, 0) =>
      lo == -(2 ^ (w - 1)) && hi == 2 ^ (w - 1) - 1 && fits P.sig P.nodes P.sortOf c (negS P t w) &&
        fits P.sig P.nodes P.sortOf thn (.num (lin P.nodes t).1 ((lin P.nodes t).2 - 2 ^ w)) && els == t
    | _ => false

/-- Whether the arguments of a rule are within the bounds under which its
    facts hold. -/
def Trace.ruleOk (r : Rule) (aux : Nat) : Bool :=
  match r with
  | .modulo v w lo hi => isIntN P v && ivSub (tr.iv SP P v) (lo * 2 ^ w) ((hi + 1) * 2 ^ w - 1)
  | .cases c t e lo hi =>
    isIntN P t && isIntN P e && P.sig.is (P.sortOf[c]?.getD 0) .bool &&
      ivSub (branchIv P (tr.iv SP P) c t true) lo hi && ivSub (branchIv P (tr.iv SP P) c e false) lo hi
  | .divide v d qmax => isIntN P v && 0 < d && ivSub (tr.iv SP P v) 0 (qmax * d + d - 1)
  | .sdivide t w d _ => isIntN P t && 0 < d && 0 < w && ivSub (tr.iv SP P t) 0 (2 ^ w - 1) && tr.signedOk SP P t w aux
  | .int2bv t _ => isIntN P t
  | .smodz a c w =>
    isIntN P a && 0 < w && smodT c w != 0 &&
      tr.symAt SP P aux == some (.sdivide a w (smodT c w).natAbs false, 1)

/-- The facts of the rule `r`, if its symbols have nodes. -/
def Trace.ruleFacts (r : Rule) (aux : Nat) : Option (List Shape) :=
  ((List.range r.nsyms).mapM fun j => tr.symNode SP P r j).map fun ks => r.facts P ks aux

/-- `Σ_{j<k} 2^(j·w) · p`: `p` repeated `k` times. -/
def repeatForm (w : Nat) (p : Lia.Form × Int) : Nat → Lia.Form × Int
  | 0 => ([], 0)
  | k + 1 =>
    let r := repeatForm w p k
    (r.1 ++ Lia.scale ((2 ^ (k * w) : Int) : Rat) p.1, r.2 + 2 ^ (k * w) * p.2)

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
    | some (.rule r _) =>
      (match P.sig.decls[k]? with
        | some s => SP.sig.sorts[s]? == some .int
        | none => false) &&
        r.args.all fun x => x < P.nodes.length && depthOf (depths (tr.rank SP) P.nodes) x ≤ m
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
  | .rule r j aux =>
    tr.ruleOk SP P r aux &&
      match tr.ruleFacts SP P r aux with
      | some fs =>
        match fs[j]? with
        | some s => fits P.sig P.nodes P.sortOf f s
        | none => false
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
