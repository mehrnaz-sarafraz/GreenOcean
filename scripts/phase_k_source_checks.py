"""Read-only current-source credential patterns and unchanged-migration verification.
Never print matched values. This is not a full-history/entropy secret assessment.
"""
import hashlib
import json
from pathlib import Path
import re
import subprocess

ROOT = Path(__file__).resolve().parents[1]
BASE = "ab44bc19fe72877b35f2709adc8a771c29cd2c00"


def git(*args):
    return subprocess.check_output(["git", *args], cwd=ROOT)


def main():
    patterns = {
        "private-key": re.compile(r"-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----"),
        "github-token": re.compile(r"\b(?:gh[pousr]_[A-Za-z0-9]{36,}|github_pat_[A-Za-z0-9_]{50,})\b"),
        "aws-access-id": re.compile(r"\b(?:AKIA|ASIA)[A-Z0-9]{16}\b"),
        "google-api-key": re.compile(r"\bAIza[A-Za-z0-9_-]{35}\b"),
    }
    findings = []
    scanned = 0
    for name in git("ls-files", "-z").decode().split("\0"):
        if not name:
            continue
        data = (ROOT / name).read_bytes()
        if len(data) > 2_000_000 or b"\0" in data:
            continue
        try:
            content = data.decode("utf-8")
        except UnicodeDecodeError:
            continue
        scanned += 1
        for kind, pattern in patterns.items():
            for match in pattern.finditer(content):
                findings.append({"kind": kind, "file": name, "line": content.count("\n", 0, match.start()) + 1})
    prefix = "backend/src/main/resources/db/migration"
    original = git("ls-tree", "-r", "--name-only", BASE, "--", prefix).decode().splitlines()
    migrations = []
    for name in original:
        before = git("show", f"{BASE}:{name}").replace(b"\r\n", b"\n")
        after = (ROOT / name).read_bytes().replace(b"\r\n", b"\n")
        migrations.append({"path": name, "sha256": hashlib.sha256(after).hexdigest(), "unchanged": before == after})
    report = {"commit": git("rev-parse", "HEAD").decode().strip(), "scope": "current tracked UTF-8 files <=2MB; four credential patterns; no history or entropy claim", "filesScanned": scanned, "findings": findings, "baseline": BASE, "originalMigrations": migrations}
    target = ROOT / ".tmp/phase-k/source-checks.json"
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
    print(json.dumps({"filesScanned": scanned, "candidateCredentialLocations": findings, "originalMigrations": len(migrations), "allOriginalMigrationsUnchanged": all(m["unchanged"] for m in migrations)}))
    if findings or not all(m["unchanged"] for m in migrations):
        raise SystemExit(1)


if __name__ == "__main__":
    main()
