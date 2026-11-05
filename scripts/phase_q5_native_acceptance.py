"""Prepare/validate a physical-device evidence package. Never grants release approval."""
import argparse
import datetime as dt
import hashlib
import json
import re
from pathlib import Path

CASES = ["registration", "session_recovery", "logout", "secure_storage", "biometric_passkey",
         "rtl", "screen_reader", "large_fonts", "background_foreground", "network_loss",
         "push_notifications", "private_journal_privacy", "listener_readiness", "appointment_reminders",
         "microphone_camera_permissions", "approved_clinical_media", "app_termination", "account_switch",
         "stale_sensitive_cache_clearing"]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--initialize", type=Path)
    parser.add_argument("--validate", type=Path)
    args = parser.parse_args()
    if bool(args.initialize) == bool(args.validate):
        raise SystemExit("Choose initialize or validate")
    if args.initialize:
        package = {"status": "DEVICE_TEST_REQUIRED", "evidenceKind": "PHYSICAL_DEVICE_REQUIRED",
                   "scope": "Synthetic QA accounts only; no JavaScript export is physical-device proof",
                   "platforms": [{"platform": platform, "deviceModel": "", "osVersion": "", "appBuildCommit": "",
                                  "testDate": "", "reviewer": "", "deviceType": "PHYSICAL_DEVICE",
                                  "cases": [{"scenario": case, "outcome": "DEVICE_TEST_REQUIRED",
                                             "evidenceSha256": "", "failureReference": ""} for case in CASES]}
                                 for platform in ["ANDROID", "IOS"]]}
        with args.initialize.open("x", encoding="utf-8") as file:
            json.dump(package, file, indent=2)
        print("DEVICE_TEST_REQUIRED: acceptance template created; no tests claimed")
        return
    package = json.loads(args.validate.read_text(encoding="utf-8"))
    missing = []
    for platform in ["ANDROID", "IOS"]:
        entries = [p for p in package.get("platforms", []) if p.get("platform") == platform]
        if len(entries) != 1:
            missing.append(platform + ":DEVICE_TEST_REQUIRED")
            continue
        record = entries[0]
        if record.get("deviceType") != "PHYSICAL_DEVICE" or any(not record.get(key) for key in ["deviceModel", "osVersion", "appBuildCommit", "testDate", "reviewer"]):
            missing.append(platform + ":PHYSICAL_METADATA_REQUIRED")
        try:
            tested = dt.date.fromisoformat(record.get("testDate", ""))
            if tested > dt.date.today():
                raise ValueError("Future test date")
        except ValueError:
            missing.append(platform + ":VALID_TEST_DATE_REQUIRED")
        if not re.fullmatch(r"[a-f0-9]{40}", str(record.get("appBuildCommit", ""))):
            missing.append(platform + ":EXACT_BUILD_COMMIT_REQUIRED")
        for scenario in CASES:
            cases = [c for c in record.get("cases", []) if c.get("scenario") == scenario]
            if len(cases) != 1 or cases[0].get("outcome") != "PASS" or not re.fullmatch(r"[a-f0-9]{64}", str(cases[0].get("evidenceSha256", ""))):
                missing.append(platform + ":" + scenario)
    print(json.dumps({"status": "DEVICE_TEST_REQUIRED" if missing else "INDEPENDENT_REVIEW_REQUIRED",
                      "remainingCases": missing, "packageSha256": hashlib.sha256(args.validate.read_bytes()).hexdigest(),
                      "releaseApprovalChanged": False, "runtimeActivationChanged": False}))


if __name__ == "__main__":
    main()
