-- Checksums only: do not print private values, even for disposable synthetic data.
SELECT 'users',count(*),md5(COALESCE(string_agg(row_to_json(x)::text,'' ORDER BY id),'')) FROM users x
UNION ALL SELECT 'profiles',count(*),md5(COALESCE(string_agg(row_to_json(x)::text,'' ORDER BY user_id),'')) FROM profiles x
UNION ALL SELECT 'posts',count(*),md5(COALESCE(string_agg(row_to_json(x)::text,'' ORDER BY id),'')) FROM posts x
UNION ALL SELECT 'sessions',count(*),md5(COALESCE(string_agg(row_to_json(x)::text,'' ORDER BY id),'')) FROM user_sessions x
UNION ALL SELECT 'live_requests',count(*),md5(COALESCE(string_agg(row_to_json(x)::text,'' ORDER BY id),'')) FROM live_support_requests x
UNION ALL SELECT 'authorizations',count(*),md5(COALESCE(string_agg(row_to_json(x)::text,'' ORDER BY event_order),'')) FROM processing_authorizations x
UNION ALL SELECT 'deletions',count(*),md5(COALESCE(string_agg(row_to_json(x)::text,'' ORDER BY user_id),'')) FROM account_deletions x
UNION ALL SELECT 'listener_calendar_rules',count(*),md5(COALESCE(string_agg(row_to_json(x)::text,'' ORDER BY id),'')) FROM listener_calendar_rules x
UNION ALL SELECT 'listener_calendar_exceptions',count(*),md5(COALESCE(string_agg(row_to_json(x)::text,'' ORDER BY id),'')) FROM listener_calendar_exceptions x
UNION ALL SELECT 'listener_bookings',count(*),md5(COALESCE(string_agg(row_to_json(x)::text,'' ORDER BY id),'')) FROM listener_bookings x
UNION ALL SELECT 'listener_booking_events',count(*),md5(COALESCE(string_agg(row_to_json(x)::text,'' ORDER BY id),'')) FROM listener_booking_events x
UNION ALL SELECT 'listener_wait_queue',count(*),md5(COALESCE(string_agg(row_to_json(x)::text,'' ORDER BY id),'')) FROM listener_wait_queue x
UNION ALL SELECT 'listener_queue_events',count(*),md5(COALESCE(string_agg(row_to_json(x)::text,'' ORDER BY id),'')) FROM listener_queue_events x
UNION ALL SELECT 'listener_queue_coverage_windows',count(*),md5(COALESCE(string_agg(row_to_json(x)::text,'' ORDER BY id),'')) FROM listener_queue_coverage_windows x
UNION ALL SELECT 'listener_session_extensions',count(*),md5(COALESCE(string_agg(row_to_json(x)::text,'' ORDER BY id),'')) FROM listener_session_extensions x
UNION ALL SELECT 'listener_session_feedback',count(*),md5(COALESCE(string_agg(row_to_json(x)::text,'' ORDER BY session_id),'')) FROM listener_session_feedback x
UNION ALL SELECT 'migration_history',count(*),md5(COALESCE(string_agg(row_to_json(x)::text,'' ORDER BY installed_rank),'')) FROM flyway_schema_history x;

SELECT 'o_journal', md5(COALESCE(string_agg(to_jsonb(t)::text, '|' ORDER BY id), '')) FROM wellbeing_journal_entries t;
SELECT 'o_moods', md5(COALESCE(string_agg(to_jsonb(t)::text, '|' ORDER BY id), '')) FROM wellbeing_check_ins t;
SELECT 'o_goals', md5(COALESCE(string_agg(to_jsonb(t)::text, '|' ORDER BY id), '')) FROM wellbeing_goals t;
SELECT 'o_daily', md5(COALESCE(string_agg(to_jsonb(t)::text, '|' ORDER BY id), '')) FROM wellbeing_daily_answers t;
