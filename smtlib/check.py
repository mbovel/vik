#!/usr/bin/env python3
"""Records what the solver does on every file of the corpus: check.py <classpath file> [timeout seconds] [path regex]

Writes `status.tsv`: path, outcome — the answers (`sat`/`unsat`, one per `check-sat`, joined by `,`) when they
all agree with the expected statuses, else `wrong`, `timeout`, `unsupported` (a parse error) or `error` — and
the seconds taken by the solver process (JVM start-up included). Files not matched by the regex keep their line.
"""
import re, subprocess, sys, time
from pathlib import Path

HERE = Path(__file__).resolve().parent
cp = Path(sys.argv[1]).read_text().strip()
limit = float(sys.argv[2]) if len(sys.argv) > 2 else 10.0
pattern = re.compile(sys.argv[3]) if len(sys.argv) > 3 else None
status_file = HERE / "status.tsv"
old = {}
if status_file.exists():
    for line in status_file.read_text().splitlines()[1:]:
        if line: p, o, s = line.split("\t"); old[p] = (o, s)

def expected_of(text):
    """The expected status of each check-sat: the last `set-info :status` before it."""
    out, status = [], None
    for m in re.finditer(r"\(set-info :status (\w+)\)|\(check-sat-assuming|\(check-sat-using|\(check-sat(?=[ )])", text):
        if m.group(0) in ("(check-sat", "(check-sat-assuming", "(check-sat-using"): out.append(status); status = None
        else: status = m.group(1)
    return out

files = sorted(str(p.relative_to(HERE)) for p in HERE.rglob("*.smt2") if ".import" not in p.parts)
results = {}
for i, rel in enumerate(files):
    if pattern and not pattern.search(rel):
        if rel in old: results[rel] = old[rel]
        continue
    text = (HERE / rel).read_text(errors="replace")
    expected = expected_of(text)
    start = time.time()
    try:
        r = subprocess.run(["java", "-cp", cp, "net.bovel.smt2.smt2", str(HERE / rel)], capture_output=True, text=True, timeout=limit)
        seconds = time.time() - start
        answers = [l for l in r.stdout.splitlines() if l in ("sat", "unsat")]
        if r.returncode != 0 or not answers:
            err = (r.stderr + r.stdout).strip().splitlines()
            outcome = "unsupported" if any("Smt2Error" in l or "not supported" in l for l in err) else "error"
        elif len(answers) != len(expected) or any(e not in (None, "unknown", a) for e, a in zip(expected, answers)):
            outcome = "wrong"
        else:
            outcome = ",".join(answers) if len(answers) <= 8 else f"{answers[0]}...({len(answers)} answers)"
    except subprocess.TimeoutExpired:
        seconds, outcome = limit, "timeout"
    results[rel] = (outcome, f"{seconds:.2f}")
    if outcome in ("wrong", "error", "timeout", "unsupported"): print(f"{outcome:12s} {rel}", flush=True)
with open(status_file, "w") as f:
    f.write("path\toutcome\tseconds\n")
    for rel in sorted(results): f.write(f"{rel}\t{results[rel][0]}\t{results[rel][1]}\n")
from collections import Counter
kinds = Counter("ok" if o not in ("wrong", "error", "timeout", "unsupported") else o for o, _ in results.values())
print(f"{len(results)} files: " + ", ".join(f"{k} {n}" for k, n in sorted(kinds.items())))
