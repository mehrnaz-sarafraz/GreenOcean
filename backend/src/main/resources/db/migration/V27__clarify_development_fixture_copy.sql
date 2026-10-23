UPDATE reports SET description = 'Automated safety signal requires a documented human review.'
 WHERE id = '0191a000-0000-7000-8000-000000000001';

UPDATE reports SET description = 'Reporter requested a contextual review.'
 WHERE id = '0191a000-0000-7000-8000-000000000002';

UPDATE audit_logs
   SET metadata = '{"source":"development-fixture","note":"Safety report assigned for review"}'::jsonb
 WHERE id = '0191c000-0000-7000-8000-000000000001';
