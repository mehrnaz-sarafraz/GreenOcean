"""Read-only reconciliation of the captured PR graph and published SQL bytes.
Does not merge, retarget, deploy, modify Git refs or approve a release.
"""
import argparse
import datetime as dt
import hashlib
import json
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]
PREFIX = 'backend/src/main/resources/db/migration'

def git(*args, check=True):
    return subprocess.run(['git', *args], cwd=ROOT, capture_output=True, check=check)

def ancestor(first, second):
    return git('merge-base', '--is-ancestor', first, second, check=False).returncode == 0

def migrations(ref):
    files = git('ls-tree', '-r', '--name-only', ref, '--', PREFIX).stdout.decode().splitlines()
    return [f for f in files if f.endswith('.sql')]

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--snapshot', type=Path, required=True)
    parser.add_argument('--output', type=Path, required=True)
    args = parser.parse_args()
    snapshot = json.loads(args.snapshot.read_text(encoding='utf-8'))
    actual_main = git('rev-parse', 'origin/main').stdout.decode().strip()
    if actual_main != snapshot['main']:
        raise SystemExit('Main changed; refresh the snapshot before assessing integration')
    actual_head = git('rev-parse', 'HEAD').stdout.decode().strip()
    refs = []
    for pr in snapshot['pullRequests']:
        target = actual_main if pr['number'] <= 9 else snapshot['q5']
        refs.append({'number': pr['number'], 'head': pr['head'], 'headSha': pr['headSha'],
                     'base': pr['base'], 'baseSha': pr['baseSha'], 'state': pr['state'],
                     'mergedAt': pr['mergedAt'], 'representedIn': 'main' if pr['number'] <= 9 else 'Q5',
                     'headIsAncestorOfTarget': ancestor(pr['headSha'], target),
                     'baseIsAncestorOfHead': ancestor(pr['baseSha'], pr['headSha'])})
    compared = []
    for label, ref in [('initial', 'ab44bc19fe72877b35f2709adc8a771c29cd2c00'),
                       ('main', actual_main), ('P5', '1bb797d0edee82baad4da7a41c36284f3034379b'),
                       ('Q5', snapshot['q5'])]:
        rows = []
        for name in migrations(ref):
            old = git('show', f'{ref}:{name}').stdout.replace(b'\r\n', b'\n')
            current = (ROOT / name).read_bytes().replace(b'\r\n', b'\n')
            rows.append({'path': name, 'sha256': hashlib.sha256(current).hexdigest(), 'unchanged': old == current})
        compared.append({'baseline': label, 'ref': ref, 'count': len(rows), 'allUnchanged': all(x['unchanged'] for x in rows), 'files': rows})
    report = {'executedAt': dt.datetime.now(dt.timezone.utc).isoformat(), 'main': actual_main,
              'auditedWorktreeHead': actual_head, 'validatedQ5': snapshot['q5'], 'q5CI': snapshot['q5CI'],
              'q5IsAncestorOfCurrentHead': ancestor(snapshot['q5'], actual_head),
              'prReconciliation': refs, 'migrationComparisons': compared,
              'scope': 'Captured GitHub PR metadata plus local commit ancestry and normalized published SQL bytes; Flyway/runtime compatibility requires the separate actual PostgreSQL regressions',
              'mergePerformed': False, 'deploymentPerformed': False}
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(report, indent=2) + '\n', encoding='utf-8')
    valid = report['q5IsAncestorOfCurrentHead'] and all(x['headIsAncestorOfTarget'] and x['baseIsAncestorOfHead'] for x in refs) and all(x['allUnchanged'] for x in compared)
    print(json.dumps({'graphAndBytesValid': valid, 'representedHistoricalPRs': [x['number'] for x in refs if x['representedIn'] == 'main'], 'orderedUnmergedStack': [x['number'] for x in refs if x['representedIn'] == 'Q5'], 'migrationBaselines': [{k: x[k] for k in ['baseline', 'count', 'allUnchanged']} for x in compared]}))
    if not valid:
        raise SystemExit(1)

if __name__ == '__main__':
    main()
