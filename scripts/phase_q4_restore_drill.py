"""Disposable synthetic PostgreSQL drill. Never accepts a production host/database.

Requires Python cryptography and PostgreSQL CLI binaries. Keeps the restored database
for inspection. Its output is physical database recovery evidence, not a service SLA.
"""
import argparse
import hashlib
import json
import os
from pathlib import Path
import subprocess
import time
import uuid
from cryptography.hazmat.primitives.ciphers.aead import AESGCM
from cryptography.exceptions import InvalidTag


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--pg-bin", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--key-file", type=Path, required=True)
    args = parser.parse_args()
    if os.environ.get("GREENOCEAN_RESTORE_QA") != "isolated-only":
        raise SystemExit("Explicit synthetic restore opt-in required")
    source = "greenocean_q4_load_qa"
    restored = "greenocean_q4_restore_" + uuid.uuid4().hex + "_qa"
    out = args.output.resolve()
    key_path = args.key_file.resolve()
    if out == key_path or out in key_path.parents:
        raise SystemExit("Key custody must be outside backup/output directory")
    out.mkdir(parents=True, exist_ok=False)
    key_path.parent.mkdir(parents=True, exist_ok=True)
    key = AESGCM.generate_key(bit_length=256)
    with key_path.open("xb") as stream:
        stream.write(key)
    os.chmod(key_path, 0o600)
    connection = ["-h", "127.0.0.1", "-p", "55432", "-U", "postgres"]

    def command(tool, *arguments):
        return subprocess.run([str(args.pg_bin / (tool + (".exe" if os.name == "nt" else ""))),
                               *connection, *arguments], check=True,
                              capture_output=True, text=True).stdout.strip()

    def query(database, sql):
        return command("psql", "-d", database, "-At", "-v", "ON_ERROR_STOP=1", "-c", sql)

    # Only the hard-coded disposable load database can be backed up by this script.
    assert query(source, "SELECT current_database()") == source
    tables = ["users", "posts", "wellbeing_journal_entries", "wellbeing_check_ins", "flyway_schema_history"]
    before = {table: int(query(source, f"SELECT count(*) FROM {table}")) for table in tables}
    # Hash rows in canonical order; only digests leave PostgreSQL, never protected narratives.
    all_tables = query(source, "SELECT tablename FROM pg_tables WHERE schemaname='public' ORDER BY tablename").splitlines()

    def fingerprints(database):
        values = {}
        for table in all_tables:
            quoted = '"' + table.replace('"', '""') + '"'
            values[table] = query(database,
                f"SELECT md5(COALESCE(string_agg(payload,'|' ORDER BY payload),'')) "
                f"FROM (SELECT to_jsonb(t)::text AS payload FROM {quoted} t) rows")
        return values

    fingerprints_before = fingerprints(source)
    plain = out / "synthetic.dump"
    start = time.monotonic()
    command("pg_dump", "-d", source, "--no-owner", "--no-acl", "-Fc", "-f", str(plain))
    dump_seconds = time.monotonic() - start
    backup_boundary = time.time()
    plaintext = plain.read_bytes()
    sha = hashlib.sha256(plaintext).hexdigest()
    nonce = os.urandom(12)
    aad = b"GreenOcean-Q4-synthetic-backup-v1"
    encrypted = AESGCM(key).encrypt(nonce, plaintext, aad)
    (out / "synthetic.dump.aesgcm").write_bytes(nonce + encrypted)
    plain.unlink()
    del plaintext
    rejected = []
    for label, candidate_key, candidate in [
        ("wrong-key", AESGCM.generate_key(bit_length=256), encrypted),
        ("tampered-ciphertext", key, encrypted[:-1] + bytes([encrypted[-1] ^ 1])),
    ]:
        try:
            AESGCM(candidate_key).decrypt(nonce, candidate, aad)
        except InvalidTag:
            rejected.append(label)
        else:
            raise AssertionError("Unauthenticated backup accepted")
    # This acknowledged post-snapshot write must not appear in the restored database.
    marker = uuid.uuid4()
    query(source, f"INSERT INTO audit_logs(action,entity_type,entity_id) VALUES('Q4_POST_BACKUP_MARKER','SYNTHETIC_QA','{marker}')")
    start = time.monotonic()
    authenticated = AESGCM(key_path.read_bytes()).decrypt(nonce, encrypted, aad)
    assert hashlib.sha256(authenticated).hexdigest() == sha
    plain.write_bytes(authenticated)  # Authentication finishes before pg_restore sees any bytes.
    command("createdb", restored)
    try:
        command("pg_restore", "--exit-on-error", "--no-owner", "--no-acl", "-d", restored, str(plain))
    finally:
        plain.unlink(missing_ok=True)
    restore_seconds = time.monotonic() - start
    after = {table: int(query(restored, f"SELECT count(*) FROM {table}")) for table in tables}
    assert before == after
    fingerprints_after = fingerprints(restored)
    assert fingerprints_before == fingerprints_after
    assert query(restored, f"SELECT count(*) FROM audit_logs WHERE entity_id='{marker}'") == "0"
    assert query(source, f"SELECT count(*) FROM audit_logs WHERE entity_id='{marker}'") == "1"
    report = {
        "evidenceKind": "SYNTHETIC_LOCAL_DATABASE_RESTORE",
        "sourceDatabase": source, "restoredDatabase": restored,
        "dumpSeconds": dump_seconds, "physicalRestoreSeconds": restore_seconds,
        "dumpBytes": len(authenticated), "integritySha256": sha,
        "authenticatedDecryptionBeforeRestore": True, "negativeIntegrityChecks": rejected,
        "tableCountsBefore": before, "tableCountsRestored": after,
        "canonicalTableFingerprintsMatched": len(fingerprints_before),
        "observedPostBackupWritesMissing": 1,
        "snapshotToVerificationSeconds": time.time() - backup_boundary,
        "serviceRecovery": "NOT_MEASURED_BY_THIS_PHYSICAL_DATABASE_DRILL",
        "privacyReplay": "FRESH_EXTERNAL_EPOCH_AND_INDEPENDENT_REVIEW_REQUIRED_BEFORE_DATA_APIS",
        "objectStorage": "NOT_COVERED_BY_POSTGRESQL_DUMP",
        "ownerApprovedRtoRpo": False,
        "limitations": ["Synthetic local data only", "No staging or production rehearsal",
                        "Windows key file inherits workspace ACL; production custody not established",
                        "No object-store or real provider recovery proof"],
    }
    (out / "results.json").write_text(json.dumps(report, indent=2), encoding="utf-8")
    print(json.dumps(report, indent=2))


if __name__ == "__main__":
    main()
