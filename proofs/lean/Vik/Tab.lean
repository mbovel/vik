/-!
# Tables with logarithmic lookups

The checkers run in the kernel (`decide +kernel`), which looks up the `i`-th
element of a list in `i` steps, each slow. A `Tab` is a binary tree of the
list's elements, element `0` at the root and the others split by parity, so a
lookup takes about `log₂ i` steps. The kernel shares a tree built once from a
list across lookups, as it caches the values of closed terms.

`Tab.get_of` says a lookup in the tree is one in the list, so a checker can
look up with `Tab.at` and its proof reason about `l[i]?`.
-/

namespace Vik

inductive Tab (α : Type) where
  | leaf
  | node (x : α) (l r : Tab α)

/-- The elements at even positions. -/
def evens {α : Type} : List α → List α
  | [] => []
  | [x] => [x]
  | x :: _ :: xs => x :: evens xs

/-- The elements at odd positions. -/
def odds {α : Type} : List α → List α
  | [] => []
  | _ :: xs => evens xs

/-- The tree of a list of at most `f` elements. -/
def Tab.ofF {α : Type} : Nat → List α → Tab α
  | 0, _ => .leaf
  | _, [] => .leaf
  | f + 1, x :: xs => .node x (Tab.ofF f (evens xs)) (Tab.ofF f (odds xs))

def Tab.of {α : Type} (l : List α) : Tab α := Tab.ofF l.length l

def Tab.get {α : Type} : Tab α → Nat → Option α
  | .leaf, _ => none
  | .node x l r, n =>
    match n with
    | 0 => some x
    | n + 1 => if n % 2 == 0 then l.get (n / 2) else r.get (n / 2)

/-- `l[i]?`, in about `log₂ i` kernel steps once the tree of `l` is built. -/
def Tab.at {α : Type} (l : List α) (i : Nat) : Option α := (Tab.of l).get i

theorem evens_get {α : Type} : ∀ (l : List α) (k : Nat), (evens l)[k]? = l[2 * k]?
  | [], _ => rfl
  | [x], k => by
    cases k with
    | zero => rfl
    | succ k =>
      simp only [evens, List.getElem?_cons_succ, List.getElem?_nil, show 2 * (k + 1) = 2 * k + 1 + 1 by omega]
  | x :: y :: xs, k => by
    cases k with
    | zero => rfl
    | succ k =>
      simp only [evens, List.getElem?_cons_succ]
      rw [evens_get xs k, show 2 * (k + 1) = 2 * k + 1 + 1 by omega]
      simp

theorem odds_get {α : Type} (l : List α) (k : Nat) : (odds l)[k]? = l[2 * k + 1]? := by
  cases l with
  | nil => rfl
  | cons x xs => simp [odds, evens_get]

theorem evens_length {α : Type} : ∀ (l : List α), (evens l).length ≤ l.length
  | [] => Nat.le_refl _
  | [_] => Nat.le_refl _
  | _ :: _ :: xs => by simp only [evens, List.length_cons]; have := evens_length xs; omega

theorem odds_length {α : Type} (l : List α) : (odds l).length + 1 ≤ l.length + 1 := by
  cases l with
  | nil => simp [odds]
  | cons x xs => simp only [odds, List.length_cons]; have := evens_length xs; omega

theorem Tab.get_ofF {α : Type} : ∀ (f : Nat) (l : List α), l.length ≤ f → ∀ i, (Tab.ofF f l).get i = l[i]?
  | 0, l, h, i => by
    have : l = [] := List.eq_nil_of_length_eq_zero (by omega)
    subst this; cases i <;> rfl
  | f + 1, [], _, i => by cases i <;> rfl
  | f + 1, x :: xs, h, i => by
    simp only [List.length_cons] at h
    cases i with
    | zero => rfl
    | succ n =>
      simp only [Tab.ofF, Tab.get, List.getElem?_cons_succ]
      split
      · rename_i hn
        rw [Tab.get_ofF f (evens xs) (Nat.le_trans (evens_length xs) (by omega)), evens_get]
        congr 1
        have := Nat.mod_two_eq_zero_or_one n
        simp only [beq_iff_eq] at hn
        omega
      · rename_i hn
        rw [Tab.get_ofF f (odds xs) (by have := odds_length xs; omega), odds_get]
        congr 1
        simp only [beq_iff_eq] at hn
        omega

@[simp] theorem Tab.at_eq {α : Type} (l : List α) (i : Nat) : Tab.at l i = l[i]? :=
  Tab.get_ofF _ l (Nat.le_refl _) i

end Vik
