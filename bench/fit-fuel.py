#!/usr/bin/env python3
"""Checks and refits the costs of the fuel (`Solver.kinds`, `Solver.loweredCost`).

Usage: bench/fit-fuel.py <corpus-run.tsv>...

Reads the files that `vik.CorpusRun` answered within its limit (the last column
holds their counts of each kind of work), and, for those that took at least
20 ms and then 100 ms of thread CPU time, prints how well the costs in the
sources predict that time: the share within 2x and 4x and the worst ratio, for
the Stainless files and for the others. It then prints the costs, in ns, that a
non-negative least squares fit of the relative error gives, and how well they
predict, with the files they predict worst.
"""
import pathlib, re, sys
import numpy as np
from scipy.optimize import nnls

root = pathlib.Path(__file__).resolve().parent.parent
source = (root / "vik/src/main/scala/io/bovel/vik/Solver.scala").read_text()
kinds = re.search(r"val kinds: List\[\(String, Long\)\] = List\((.*?)\n  \)", source, re.S).group(1)
costs = {k: int(v) for k, v in re.findall(r'"([a-z-]+)" -> (\d+)', kinds)}
costs["lowered"] = int(re.search(r"val loweredCost = (\d+)L", source).group(1))
K = list(costs)

def load(threshold):
  rows = []
  for name in sys.argv[1:]:
    for line in open(name):
      f = line.rstrip("\n").split("\t")
      if len(f) < 8 or f[5] != "ok" or float(f[2]) < threshold: continue
      counts = dict(kv.split("=") for kv in f[7].split())
      rows.append((f[0], float(f[2]), np.array([float(counts[k]) for k in K])))
  return rows

def report(rows, w, label, show=0):
  X = np.array([r[2] for r in rows]); y = np.array([r[1] for r in rows])
  stainless = np.array(["/stainless/" in r[0] for r in rows])
  pred = X @ w
  unit = np.exp(np.median(np.log(y / np.maximum(pred, 1e-9))))
  ratio = y / np.maximum(pred * unit, 1e-9); err = np.abs(np.log2(ratio))
  part = lambda m: f"within 2x {np.mean(err[m] <= 1):.2f}, 4x {np.mean(err[m] <= 2):.2f}, worst {2 ** err[m].max():.0f}x"
  scale = f"unit {unit * 1e6:.2f} ns" if label == "sources" else f"scale {unit:.2f}"
  print(f"  {label}: {scale}; stainless (n={stainless.sum()}) {part(stainless)}; others (n={(~stainless).sum()}) {part(~stainless)}")
  order = np.argsort(ratio)
  for i in list(order[:show]) + list(order[len(order) - show:]):
    print(f"    {ratio[i]:6.2f}x  cpu {y[i]:.0f} ms  {rows[i][0]}")

for threshold in [20, 100]:
  rows = load(threshold)
  X = np.array([r[2] for r in rows]); y = np.array([r[1] for r in rows])
  print(f"files of at least {threshold} ms")
  report(rows, np.array([costs[k] for k in K], float), "sources")
  w, _ = nnls(X / y[:, None], np.ones(len(y)))
  print("  fit, ns: " + ", ".join(f"{k} {v * 1e6:.0f}" for k, v in zip(K, w) if v > 0))
  report(rows, w, "fit", 4)
