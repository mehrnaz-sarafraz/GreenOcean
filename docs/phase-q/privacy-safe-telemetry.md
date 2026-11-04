# Privacy-safe process telemetry

`GREENOCEAN_TELEMETRY_ENABLED` defaults false. SafeTelemetryFilter measures elapsed time using a monotonic clock and maps the request transiently into ten fixed service classes and five HTTP response families. It stores counts and aggregate elapsed nanoseconds in bounded process-local counters. No request/response body, query, identifier, full URI, header, token, contact, pseudonym, clinical text, exact search or arbitrary label is retained. Restart resets counters; this is deliberately not a durable service-quality or clinical-outcome store.

`GET /api/v1/trust/metrics` requires the separate enabled trust service and current TRUST_PLATFORM role with recent strong authentication. It does not authorize access to another domain's reports. No collector, tracking SDK or third-party transmission is added. PhaseQ2SecurityTest submits synthetic secret/identity/path markers and verifies they are absent from the snapshot and that ordinary users cannot read it. No percentile, delivery acknowledgement, continuous monitoring or external alerting is claimed here; Q3/Q4 supply their own evidence.

## Q3 extension

Three additional fixed categories separate WELLBEING_WRITE, ACCOUNT_EXPORT and ACCOUNT_ERASURE HTTP request outcomes. The bounded contract is now13 service/request categories ×5 status families. Method/path are classified transiently; no arbitrary route, ID, body or token label is stored. ServiceQuality filters technical aggregates by current operational purpose. An accepted erase request is not proof of completed external/provider/backup erasure.
