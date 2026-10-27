"""Query public advisory services with public dependency coordinates only.
Findings are evidence for triage, not an automatic assertion of exploitability.
Network/registry errors make the scan incomplete rather than a clean result.
"""
import json
from pathlib import Path
import subprocess
import urllib.request

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / ".tmp/phase-k/dependencies.json"


def request(url, data=None):
    body = None if data is None else json.dumps(data).encode()
    req = urllib.request.Request(url, body, {"Content-Type": "application/json", "User-Agent": "GreenOcean-isolated-QA"})
    with urllib.request.urlopen(req, timeout=30) as response:
        return json.load(response)


def main():
    tree = json.loads((ROOT / "backend/target/phase-k-dependencies.json").read_text())
    packages = set()

    def walk(node):
        if node.get("scope") != "test" and node.get("version"):
            packages.add((node["groupId"] + ":" + node["artifactId"], node["version"]))
            for child in node.get("children", []):
                walk(child)

    for child in tree.get("children", []):
        walk(child)
    coordinates = sorted(packages)
    findings = []
    for offset in range(0, len(coordinates), 100):
        group = coordinates[offset:offset + 100]
        result = request("https://api.osv.dev/v1/querybatch", {"queries": [{"package": {"ecosystem": "Maven", "name": name}, "version": version} for name, version in group]})
        if len(result.get("results", [])) != len(group):
            raise ValueError("Incomplete OSV batch response")
        for (name, version), entry in zip(group, result["results"]):
            if entry.get("next_page_token"):
                raise ValueError("OSV pagination requires follow-up; refusing a clean scan")
            for vulnerability in entry.get("vulns", []):
                findings.append({"package": name, "version": version, "advisory": vulnerability["id"]})
    details = {}
    for advisory in sorted({f["advisory"] for f in findings}):
        record = request("https://api.osv.dev/v1/vulns/" + advisory)
        details[advisory] = {key: record.get(key) for key in ["id", "aliases", "summary", "severity", "database_specific", "references", "affected", "withdrawn"]}
    audit = subprocess.run(["npm", "audit", "--json", "--ignore-scripts"], cwd=ROOT / "frontend", capture_output=True, text=True, timeout=120)
    npm = json.loads(audit.stdout)
    if npm.get("error") or "metadata" not in npm or audit.returncode not in (0, 1):
        raise ValueError("npm advisory scan incomplete; see registry availability")
    report = {"mavenPackages": len(coordinates), "mavenCoordinates": coordinates, "mavenFindings": findings, "mavenAdvisories": details, "npm": npm, "scope": "resolved Maven runtime tree; npm installed dependency graph incl build tooling; advisory matching is not exploitability proof"}
    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
    print(json.dumps({"mavenPackages": len(coordinates), "mavenAdvisoryMatches": len(findings), "npmVulnerabilities": npm["metadata"].get("vulnerabilities", {})}))


if __name__ == "__main__":
    try:
        main()
    except Exception as failure:
        OUT.parent.mkdir(parents=True, exist_ok=True)
        OUT.write_text(json.dumps({"status": "INCOMPLETE", "errorType": type(failure).__name__}) + "\n")
        raise SystemExit("Dependency scan incomplete (no clean result claimed): " + type(failure).__name__)
