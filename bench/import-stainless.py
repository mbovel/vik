#!/usr/bin/env python3
"""Copies Stainless's solver sessions into corpus/<logic>/stainless/, with z3's answers as statuses.

  bench/import-stainless.py <replay tsv> <z3 tsv> <commit text for the headers>

The tsvs have a line per session: path, milliseconds, then the answers as s, u and ? (last column); the replay
tsv is from StainlessReplay, with the outcome before the answers. A session is kept when it is at most 100 KB,
or at most 1 MB and takes a second or more: a larger file costs the repository more than it adds. Exact
duplicates keep one copy. A file is named `<valid or invalid>-<benchmark>__<session>`, so that `prune trivial`
takes a benchmark's sessions for a family. Then run `prune alike order --delete`, `prune trivial 10 5 --delete`
and `prune logic --move` on the `stainless` directories, as for the corpus as it is (see corpus/README.md).
"""
import hashlib
import os
import re
import sys

KEEP_ALWAYS = 100_000
KEEP_IF_SLOW = 1_000_000
SLOW_MS = 1000
STATUS = {"s": "sat", "u": "unsat"}


def logic_of(text):
    has = lambda pattern: re.search(pattern, text) is not None
    ints = has(r"[( ]Int[) ]")
    reals = has(r"[( ]Real[) ]")
    name = ""
    if has(r"\(Array |\(select |\(store "): name += "A"
    if has(r"\(declare-fun [^ ]+ \([^)]") or has(r"\(declare-sort "): name += "UF"
    if has(r"\(_ BitVec |\(bv[a-z]+ |#b[01]|#x[0-9a-fA-F]"): name += "BV"
    if has(r"\(declare-datatype"): name += "DT"
    arith = "LIRA" if ints and reals else "LIA" if ints else "LRA" if reals else ""
    # SMT-LIB has no logic of datatypes beside another theory, or of bit-vectors with arithmetic, without UF.
    others = name.startswith("A") + ("BV" in name) + bool(arith)
    if "UF" not in name and (("DT" in name and others) or ("BV" in name and arith)):
        name = ("A" if name.startswith("A") else "") + "UF" + name.removeprefix("A")
    return "QF_" + (name + arith or "UF")


def main(replay, z3, commit):
    corpus = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "corpus")
    answers = {}
    for line in open(z3):
        fields = line.rstrip("\n").split("\t")
        answers[fields[0]] = fields[2] if len(fields) > 2 else ""
    seen, kept, dropped, duplicates = set(), 0, 0, 0
    for line in open(replay):
        fields = line.rstrip("\n").split("\t")
        path, ms = fields[0], int(fields[1])
        size = os.path.getsize(path)
        if size > KEEP_IF_SLOW or (size > KEEP_ALWAYS and ms < SLOW_MS):
            dropped += 1
            continue
        lines = [l for l in open(path).read().split("\n") if l.strip()]
        digest = hashlib.sha256("\n".join(lines).encode()).digest()
        if digest in seen:
            duplicates += 1
            continue
        seen.add(digest)
        text = "\n".join(lines)
        checks = sum(l.startswith("(check-sat") for l in lines)
        got = answers.get(path, "")
        statuses = iter(got if len(got) == checks else "?" * checks)
        family = os.path.basename(os.path.dirname(path))
        session = os.path.basename(path).rsplit("-", 1)[1].removesuffix(".smt2")
        scala = os.path.basename(path)[len("z3-"):].rsplit("-", 1)[0]
        kind = family.split("__")[0]
        source = f"frontends/benchmarks/verification/{kind}/{scala}"
        logic = logic_of(text)
        out = [
            f"; origin: Stainless verification benchmarks, {commit}, one solver session",
            f"; original path: {source}, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30",
            "; license: Apache-2.0 (the stainless repository); statuses from z3",
            f"(set-logic {logic})",
        ]
        for l in lines:
            if l.startswith("(check-sat"):
                status = STATUS.get(next(statuses))
                if status: out.append(f"(set-info :status {status})")
            out.append(l)
        target = os.path.join(corpus, logic, "stainless")
        os.makedirs(target, exist_ok=True)
        with open(os.path.join(target, f"{kind}-{family.split('__', 1)[1]}__{session}.smt2"), "w") as f:
            f.write("\n".join(out) + "\n")
        kept += 1
    print(f"kept {kept}, dropped {dropped} by size, {duplicates} duplicates")


if __name__ == "__main__":
    main(*sys.argv[1:4])
