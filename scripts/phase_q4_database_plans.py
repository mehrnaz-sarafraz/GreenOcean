"""Minimized plans against the explicitly isolated synthetic load database only."""
import argparse
import json
import os
from pathlib import Path
import re
import subprocess


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--pg-bin", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    if os.environ.get("GREENOCEAN_LOAD_QA") != "isolated-only":
        raise SystemExit("Synthetic loopback QA opt-in required")
    executable = args.pg_bin / ("psql.exe" if os.name == "nt" else "psql")

    def query(sql):
        return subprocess.run([str(executable), "-h", "127.0.0.1", "-p", "55432", "-U", "postgres",
            "-d", "greenocean_q4_load_qa", "-At", "-v", "ON_ERROR_STOP=1", "-c", sql],
            check=True, capture_output=True, text=True).stdout.strip()

    subject = query("SELECT id FROM users WHERE email LIKE 'i-%@example.test' ORDER BY created_at LIMIT 1")
    if not re.fullmatch(r"[a-f0-9-]{36}", subject):
        raise SystemExit("Synthetic subject fixture required")
    who = "'" + subject + "'::uuid"
    code = Path("backend/src/main/java/com/greenocean/backend/post/repository/PostReadRepository.java").read_text(encoding="utf-8")

    def constant(name):
        block = re.search(r"String\s+" + name + r'\s*=\s*"""(.*?)""";', code, re.S)
        if block:
            return block.group(1)
        line = re.search(r"String\s+" + name + r'\s*=\s*"([^"\n]*)"\s*;', code)
        if line:
            return line.group(1)
        raise AssertionError("Source query constant changed: " + name)

    # Reconstruct the actual unfiltered first-feed SQL, not a simplified visibility predicate.
    feed = constant("POST_SELECT") + " WHERE p.status = 'PUBLISHED' " + constant("ACCESS_PREDICATE") + constant("DISCOVERY_FILTER") + constant("GLOBAL_GROUP_FILTER")
    values = iter([who] * 5)
    feed = re.sub(r"\?", lambda _: next(values), feed)
    queries = {
        "feed-first-page-source-sql": feed + " ORDER BY p.created_at DESC,p.id DESC LIMIT 21 OFFSET 0",
        "feed-deep-page-source-sql": feed + " ORDER BY p.created_at DESC,p.id DESC LIMIT 21 OFFSET 2000",
        "current-purpose-authorization": "SELECT EXISTS(SELECT 1 FROM users u JOIN user_roles ur ON ur.user_id=u.id JOIN roles r ON r.id=ur.role_id WHERE u.id=" + who + " AND u.status='ACTIVE' AND u.email_verified AND u.deleted_at IS NULL AND r.name='TRUST_ACCOUNT')",
        "own-journal-page": "SELECT id FROM wellbeing_journal_entries WHERE user_id=" + who + " ORDER BY created_at DESC,id DESC LIMIT 21 OFFSET 0",
        "active-group-membership": "SELECT community_id FROM community_members WHERE user_id=" + who + " LIMIT 21",
        "listener-bookings": "SELECT id FROM listener_bookings WHERE member_id=" + who + " ORDER BY starts_at DESC LIMIT 21",
        "queue-current-state": "SELECT id FROM listener_wait_queue WHERE member_id=" + who + " AND status IN('WAITING','OFFERED')",
        "appointment-participant-page": "SELECT id FROM consultation_requests WHERE requester_user_id=" + who + " ORDER BY created_at DESC LIMIT 21",
        "payment-ledger-lookup": "SELECT id FROM clinic_financial_events WHERE appointment_id=" + who,
        "refund-lookup": "SELECT id FROM clinic_refunds WHERE appointment_id=" + who,
        "clinical-grant-operator": "SELECT id FROM clinic_record_access_grants WHERE operator_id=" + who + " AND revoked_at IS NULL",
        "moderation-purpose-queue": "SELECT id FROM trust_cases WHERE domain='SOCIAL' AND state<>'CLOSED' ORDER BY created_at,id LIMIT 51",
        "draft-retention-candidates": "SELECT user_id FROM wellbeing_journal_drafts WHERE expires_at<CURRENT_TIMESTAMP LIMIT 100",
        "private-reward-ledger": "SELECT id FROM growth_reward_ledger WHERE owner_id=" + who + " LIMIT 101",
        "account-erasure-state": "SELECT user_id FROM account_deletions WHERE user_id=" + who,
    }
    cursor = query("SELECT created_at::text||'|'||id::text FROM posts WHERE status='PUBLISHED' ORDER BY created_at DESC,id DESC OFFSET 1000 LIMIT 1")
    if cursor:
        timestamp, identifier = cursor.split("|", 1)
        if not re.fullmatch(r"[a-f0-9-]{36}", identifier):
            raise SystemExit("Invalid synthetic cursor fixture")
        timestamp = timestamp.replace("'", "''")
        queries["cursor-page-after-position1000-source-sql"] = feed + " AND (p.created_at,p.id)<('" + timestamp + "'::timestamptz,'" + identifier + "'::uuid) ORDER BY p.created_at DESC,p.id DESC LIMIT 21"

    def nodes(plan):
        result = [{key: plan[key] for key in ("Node Type", "Relation Name", "Index Name", "Actual Rows", "Actual Loops", "Shared Hit Blocks", "Shared Read Blocks", "Rows Removed by Filter") if key in plan}]
        for child in plan.get("Plans", []):
            result.extend(nodes(child))
        return result

    output = {}
    for name, sql in queries.items():
        try:
            plan = json.loads(query("EXPLAIN(ANALYZE,BUFFERS,FORMAT JSON) " + sql))[0]
        except subprocess.CalledProcessError:
            raise SystemExit("Plan probe failed: " + name + "; review current source schema") from None
        output[name] = {"planningMs": plan["Planning Time"], "executionMs": plan["Execution Time"], "nodes": nodes(plan["Plan"])}
    report = {"evidenceKind": "SYNTHETIC_LOCAL_POSTGRESQL_PLANS", "plans": output,
        "settings": {setting: query("SHOW " + setting) for setting in ("max_connections", "statement_timeout", "lock_timeout", "deadlock_timeout", "shared_buffers")},
        "limitations": ["Small synthetic dataset; empty service tables are not realistic performance evidence",
            "Only feed query reconstructed exactly from current repository source; remaining probes are representative access paths",
            "Deep legacy offset diagnostic is rejected by the new public API budget; it remains a direct SQL regression measurement",
            "Function internals and full account archive/erasure/reward domain workflows require separate analysis",
            "No new index justified by these small-sample results alone", "No private filters, identifiers or row bodies exported"]}
    args.output.write_text(json.dumps(report, indent=2), encoding="utf-8")
    print(json.dumps({"measuredPlans": len(output), "output": str(args.output)}))


if __name__ == "__main__":
    main()
