#!/usr/bin/env python3
"""Imports SMT-LIB files into the corpus: import.py <origin> <source root> <classpath file> [files per family]

Origins (see README.md): cvc5, z3test, princess, submission, smt-lib-incremental, smt-lib.
Every candidate is parsed by VIK (`smt2check`): only the files in the supported fragment are copied, into
`<logic>/<origin>/<family>__<file>.smt2`, with a header naming the origin, the original path and the license,
and a `(set-info :status ...)` before each `check-sat` when the expected answers came from elsewhere.
"""
import os, re, subprocess, sys
from pathlib import Path

HERE = Path(__file__).resolve().parent
LICENSES = {
    "cvc5": "modified BSD, LICENSES/cvc5.txt", "z3test": "MIT, LICENSES/z3test.txt", "princess": "BSD, LICENSES/princess.txt",
    "submission": "see the file's header", "smt-lib-incremental": "CC BY 4.0 (SMT-LIB release 2025, incremental, Zenodo 15493096)",
    "smt-lib": "CC BY 4.0 (SMT-LIB release 2025, non-incremental, Zenodo 16740866)",
}
MAX_SIZE = 200_000

def classify(text):
    """The corpus logic of a file, from `set-logic` or, for `ALL`, from what it uses."""
    m = re.search(r"\(set-logic ([A-Za-z_]+)\)", text)
    logic = m.group(1) if m else "ALL"
    if logic in ("QF_UF", "QF_LIA", "QF_UFLIA", "QF_DT", "QF_BV"): return logic
    if logic in ("QF_UFDT", "QF_UFDTLIA", "QF_DTLIA"): return "QF_DT"
    if logic.startswith("HO_") or "(lambda " in text or "(-> " in text: return "HO"
    if logic == "QF_UFBV" or logic == "BV": return "QF_BV"
    if "declare-datatype" in text: return "QF_DT"
    if re.search(r"\(_ bv\d+ \d+\)|\(_ BitVec", text): return "QF_BV"
    if re.search(r"\bInt\b", text): return "QF_UFLIA" if re.search(r"\(declare-fun \S+ \([^)]", text) or "declare-sort" in text else "QF_LIA"
    if "declare-sort" in text or re.search(r"\(declare-fun \S+ \([^)]", text): return "QF_UF"
    return None  # Bool only: with the QF_UF files? keep it simple: QF_UF
def answers_of(status_lines):
    return [s for s in status_lines if s in ("sat", "unsat", "unknown")]

def candidates(origin, root):
    root = Path(root)
    for path in sorted(root.rglob("*.smt2")):
        if not path.is_file() or path.stat().st_size > MAX_SIZE: continue
        try: text = path.read_text(errors="replace")
        except OSError: continue
        rel = path.relative_to(root)
        expected = None
        if origin == "cvc5":
            if re.search(r"^; (EXIT: [1-9]|EXPECT-ERROR|REQUIRES:)", text, re.M): continue
            options = re.findall(r"^; COMMAND-LINE: (.*)$", text, re.M)
            harmless = ("--incremental", "--produce-models", "--lang", "-q", "--no-", "--check-", "--strings-exp", "--fmf", "--full-saturate")
            if any(o.startswith("-") and not o.startswith(harmless) for line in options for o in line.split()): continue  # answers may depend on the option
            expected = [l.strip() for l in re.findall(r"^; EXPECT: (.*)$", text, re.M)]
            if not expected or any(e not in ("sat", "unsat", "unknown") for e in expected): continue
            family = rel.parts[0] + "-" + rel.parts[1] if len(rel.parts) > 2 else rel.parts[0]
        elif origin == "z3test":
            out = path.with_name(path.name[:-5] + ".expected.out")
            if not out.exists(): continue
            expected = [l.strip() for l in out.read_text(errors="replace").splitlines() if l.strip()]
            if not expected or any(e not in ("sat", "unsat", "unknown") for e in expected): continue
            family = "regressions"
        elif origin == "princess":
            answers = path.parent / "Answers"
            if not answers.exists(): continue
            log = answers.read_text(errors="replace")
            m = re.search(r"^Loading " + re.escape(path.name) + r" \.\.\.\n(.*?)(?=^Loading |\Z)", log, re.M | re.S)
            if not m: continue
            expected = [l for l in m.group(1).splitlines() if l.strip() in ("sat", "unsat", "unknown")]
            if not expected: continue
            family = rel.parts[-2]
        elif origin == "submission":  # <non-incremental|incremental>/<logic>/<family>/.../file
            parts = rel.parts
            family = parts[2] if len(parts) > 3 else (parts[-2] if len(parts) > 1 else "root")
            if parts[0] == "incremental": family = "incremental-" + family
        else:  # smt-lib, smt-lib-incremental: statuses in the file; <logic>/<family>/[subdirs/]file
            family = "__".join(rel.parts[1:-1]) if len(rel.parts) > 2 else (rel.parts[-2] if len(rel.parts) > 1 else "root")
        if "check-sat" not in text: continue  # `check-sat` or `check-sat-assuming`
        yield path, rel, text, expected, family

def with_statuses(text, expected):
    """`(set-info :status X)` before each `(check-sat)`, X from `expected` in order."""
    if expected is None: return text
    parts = re.split(r"(\(check-sat-assuming|\(check-sat-using|\(check-sat(?=[ )]))", text)
    out, k = [], 0
    for part in parts:
        if part in ("(check-sat", "(check-sat-assuming", "(check-sat-using"):
            status = expected[k] if k < len(expected) else "unknown"
            k += 1
            out.append(f"(set-info :status {status})\n{part}")
        else: out.append(part)
    return "".join(out)

def main():
    origin, root, cp = sys.argv[1], sys.argv[2], Path(sys.argv[3]).read_text().strip()
    staging = HERE / ".import" / origin
    if staging.exists():
        for p in staging.rglob("*"): 
            if p.is_file(): p.unlink()
    staging.mkdir(parents=True, exist_ok=True)
    per_family = int(sys.argv[4]) if len(sys.argv) > 4 else None
    found = list(candidates(origin, root))
    if per_family:  # the smallest files of each family
        by_family = {}
        for c in found: by_family.setdefault(c[4], []).append(c)
        found = [c for group in by_family.values() for c in sorted(group, key=lambda c: len(c[2]))[:per_family]]
    entries = []
    for path, rel, text, expected, family in found:
        logic = classify(text) or "QF_UF"
        name = f"{family}__{path.name}".replace("/", "_")
        header = f"; From {origin}: {rel} ({LICENSES[origin]})\n"
        (staging / name).write_text(header + with_statuses(text, expected))
        entries.append((logic, name, str(rel)))
    names = [str(staging / n) for _, n, _ in entries]
    ok, rejected = set(), {}
    for i in range(0, len(names), 200):  # the fragment check, in batches
        r = subprocess.run(["java", "-cp", cp, "net.bovel.smt2.smt2check"] + names[i:i + 200], capture_output=True, text=True)
        for line in r.stdout.splitlines():
            if "\t" not in line: continue
            f, _, msg = line.partition("\t")
            if msg.strip() == "ok": ok.add(Path(f.strip()).name)
            else: rejected[msg.strip()[:60]] = rejected.get(msg.strip()[:60], 0) + 1
    copied = 0
    for logic, name, rel in entries:
        if name in ok:
            dest = HERE / logic / origin
            dest.mkdir(parents=True, exist_ok=True)
            (dest / name).write_text((staging / name).read_text())
            copied += 1
    print(f"{origin}: {len(entries)} candidates, {copied} in the fragment, copied")
    for msg, n in sorted(rejected.items(), key=lambda x: -x[1])[:15]: print(f"  rejected {n:5d}  {msg}")

main()
