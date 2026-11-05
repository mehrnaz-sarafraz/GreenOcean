"""Kill only child JVMs owned by this synthetic drill while PostgreSQL is in-flight.

Creates and retains a uniquely named local QA database. Never accepts a real host,
customer account, arbitrary SQL table or existing server PID. Requires compiled tests.
"""
import argparse
from concurrent.futures import ThreadPoolExecutor
import json
import os
from pathlib import Path
import subprocess
import time
from urllib.request import Request, urlopen
from urllib.error import HTTPError, URLError
import uuid


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--pg-bin", type=Path, required=True)
    parser.add_argument("--java", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    if os.environ.get("GREENOCEAN_PROCESS_CRASH_QA") != "isolated-only":
        raise SystemExit("Explicit isolated process-crash opt-in required")
    root = Path.cwd().resolve()
    backend = root / "backend"
    target = backend / "target"
    pg = args.pg_bin.resolve()
    java = args.java.resolve()
    database = "greenocean_q4_crash_" + uuid.uuid4().hex + "_qa"
    source = "jdbc:postgresql://127.0.0.1:55432/" + database
    separator = ";" if os.name == "nt" else ":"
    classpath = separator.join([str(target / "test-classes"), str(target / "classes"), (target / "live-classpath").read_text().strip()])
    prefix = ["-h", "127.0.0.1", "-p", "55432", "-U", "postgres"]
    hidden = subprocess.CREATE_NO_WINDOW if os.name == "nt" else 0

    def sql(statement):
        return subprocess.run([str(pg / ("psql.exe" if os.name == "nt" else "psql")), *prefix,
            "-d", database, "-At", "-v", "ON_ERROR_STOP=1", "-c", statement],
            check=True, capture_output=True, text=True, timeout=25).stdout.strip()

    subprocess.run([str(pg / ("createdb.exe" if os.name == "nt" else "createdb")), *prefix, database], check=True, capture_output=True, timeout=20)
    results = []
    for mode, table in [("booking", "consultation_requests"), ("queue", "listener_wait_queue"), ("upload", "professional_application_documents")]:
        fixture_file = target / ("q4-crash-" + database + "-" + mode + ".json")
        object_root = target / ("q4-crash-objects-" + database)
        log_path = target / ("q4-crash-" + mode + ".log")
        env = {**os.environ, "GREENOCEAN_TEST_DB_URL": source, "GREENOCEAN_CRASH_MODE": mode,
               "GREENOCEAN_CRASH_FIXTURE": str(fixture_file), "GREENOCEAN_CRASH_OBJECTS": str(object_root)}

        def start():
            with log_path.open("ab") as log:
                process = subprocess.Popen([str(java), "-Xmx512m", "-cp", classpath,
                    "com.greenocean.backend.PhaseQ4CrashServer"], cwd=backend, env=env,
                    stdout=log, stderr=subprocess.STDOUT, creationflags=hidden)
            deadline = time.monotonic() + 100
            previous = fixture_file.stat().st_mtime_ns if fixture_file.exists() else 0
            while time.monotonic() < deadline:
                if process.poll() is not None:
                    raise RuntimeError("Owned synthetic child failed to start: " + mode)
                if fixture_file.exists() and fixture_file.stat().st_mtime_ns != previous:
                    try:
                        data = json.loads(fixture_file.read_text())
                    except json.JSONDecodeError:
                        time.sleep(0.1)
                        continue
                    if data["databaseUrl"] != source or data["mode"] != mode or data["table"] != table:
                        process.kill();process.wait();raise RuntimeError("Synthetic fixture identity mismatch")
                    return process, data
                time.sleep(0.2)
            process.kill();process.wait();raise RuntimeError("Owned synthetic child startup timed out")

        def request(data, path=None, body=None, token=None, multipart=False, method="POST"):
            if multipart:
                boundary = "q4" + uuid.uuid4().hex
                content = ("--" + boundary + '\r\nContent-Disposition: form-data; name="file"; filename="credential.pdf"\r\nContent-Type: application/pdf\r\n\r\n').encode() + b"%PDF-1.4\nSYNTHETIC-Q4-CRASH-CREDENTIAL" + ("\r\n--" + boundary + "--\r\n").encode()
                content_type = "multipart/form-data; boundary=" + boundary
            else:
                content = json.dumps(data["body"] if body is None else body).encode()
                content_type = "application/json"
            req = Request(data["baseUrl"] + (path or data["path"]), data=content, method=method,
                headers={"Authorization": "Bearer " + (token or data["token"]), "Content-Type": content_type})
            try:
                with urlopen(req, timeout=20) as response:
                    response.read();return response.status
            except HTTPError as error:
                error.read();return error.code
            except (URLError, ConnectionError, TimeoutError):
                return 0

        process = None
        try:
            process, data = start()
            if mode == "queue":
                assert request(data, "/api/v1/listeners/me/availability", {"ready": True}, data["listenerToken"], method="PUT") == 200
            before = int(sql("SELECT count(*) FROM " + table))
            sql("CREATE FUNCTION q4_crash_pause() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN PERFORM pg_sleep(8); RETURN NEW; END $$; CREATE TRIGGER q4_crash_pause BEFORE INSERT ON " + table + " FOR EACH ROW EXECUTE FUNCTION q4_crash_pause()")
            with ThreadPoolExecutor(max_workers=1) as executor:
                pending = executor.submit(request, data, multipart=mode == "upload")
                deadline = time.monotonic() + 15
                while time.monotonic() < deadline:
                    if sql("SELECT count(*) FROM pg_stat_activity WHERE datname=current_database() AND wait_event='PgSleep'") != "0":
                        break
                    if pending.done():
                        raise RuntimeError("Request never reached controlled in-flight insert: " + mode + ", status=" + str(pending.result()))
                    time.sleep(0.1)
                else:
                    raise RuntimeError("In-flight barrier not observed: " + mode)
                process.kill();process.wait(timeout=10)
                interrupted = pending.result(timeout=20)
                assert not 200 <= interrupted < 300
            # DROP waits for the abandoned transaction to roll back; no trigger enters shipped migrations.
            sql("DROP TRIGGER q4_crash_pause ON " + table + "; DROP FUNCTION q4_crash_pause()")
            assert int(sql("SELECT count(*) FROM " + table)) == before
            orphan_objects = len(list(object_root.rglob("*.enc"))) if object_root.exists() else 0
            if mode == "upload":
                assert orphan_objects == 1
                assert all(b"SYNTHETIC-Q4-CRASH-CREDENTIAL" not in file.read_bytes() for file in object_root.rglob("*.enc"))
            restart = time.monotonic()
            process, data = start()
            if mode == "queue":
                # A real synthetic client explicitly renews availability; reboot never invents presence.
                assert request(data, "/api/v1/listeners/me/availability", {"ready": True}, data["listenerToken"], method="PUT") == 200
            retry = request(data, multipart=mode == "upload")
            expected = 200 if mode == "booking" else 201
            assert retry == expected, f"{mode} retry expected {expected}, received {retry}"
            assert int(sql("SELECT count(*) FROM " + table)) == before + 1
            if mode != "upload":
                assert request(data) == expected
                assert int(sql("SELECT count(*) FROM " + table)) == before + 1
            else:
                application = str(uuid.UUID(data["path"].split("/")[-2]))
                assert int(sql("SELECT count(*) FROM professional_application_documents WHERE application_id='" + application + "' AND scan_status='CLEAN'")) == 0
                assert len(list(object_root.rglob("*.enc"))) == 2
            results.append({"scenario": mode, "inFlightBarrierObserved": True, "ownedJvmKilled": True,
                "originalResponseStatus": interrupted, "rowsCommittedBeforeCrash": 0, "retryStatus": retry,
                "exactlyOneCommittedResult": True, "rebootAndRetrySeconds": time.monotonic() - restart,
                "orphanCipherObjects": orphan_objects,
                "orphanDisposition": "ENCRYPTED_UNREFERENCED_NOT_SERVED_RECONCILIATION_REQUIRED" if mode == "upload" else "NOT_APPLICABLE"})
            print("Q4 process-crash scenario verified: " + mode, flush=True)
            args.output.write_text(json.dumps({"evidenceKind": "SYNTHETIC_LOCAL_PHYSICAL_JVM_CRASHES",
                "complete": False, "completedScenarios": results}, indent=2), encoding="utf-8")
        finally:
            if process is not None and process.poll() is None:
                process.kill();process.wait(timeout=10)
    report = {"evidenceKind": "SYNTHETIC_LOCAL_PHYSICAL_JVM_CRASHES", "complete": True, "database": database, "scenarios": results,
        "limitations": ["Synthetic loopback accounts and credentials only", "Controlled BEFORE INSERT delay exposes a reproducible in-flight boundary",
            "Upload interruption leaves one inaccessible encrypted orphan; no automatic destructive cleanup is claimed",
            "No production key custody, provider or hardware rehearsal", "Fixture files contain only synthetic bearer tokens and must remain untracked"]}
    args.output.write_text(json.dumps(report, indent=2), encoding="utf-8")


if __name__ == "__main__":
    main()
