-- Checksums only: do not print private values, even for disposable synthetic data.
SELECT 'users',count(*),md5(COALESCE(string_agg(row_to_json(x)::text,'' ORDER BY id),'')) FROM users x
UNION ALL SELECT 'profiles',count(*),md5(COALESCE(string_agg(row_to_json(x)::text,'' ORDER BY user_id),'')) FROM profiles x
UNION ALL SELECT 'posts',count(*),md5(COALESCE(string_agg(row_to_json(x)::text,'' ORDER BY id),'')) FROM posts x
UNION ALL SELECT 'sessions',count(*),md5(COALESCE(string_agg(row_to_json(x)::text,'' ORDER BY id),'')) FROM user_sessions x
UNION ALL SELECT 'live_requests',count(*),md5(COALESCE(string_agg(row_to_json(x)::text,'' ORDER BY id),'')) FROM live_support_requests x
UNION ALL SELECT 'authorizations',count(*),md5(COALESCE(string_agg(row_to_json(x)::text,'' ORDER BY event_order),'')) FROM processing_authorizations x
UNION ALL SELECT 'deletions',count(*),md5(COALESCE(string_agg(row_to_json(x)::text,'' ORDER BY user_id),'')) FROM account_deletions x
UNION ALL SELECT 'migration_history',count(*),md5(COALESCE(string_agg(row_to_json(x)::text,'' ORDER BY installed_rank),'')) FROM flyway_schema_history x;
