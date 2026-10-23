-- PostgreSQL 18 provides uuidv7() natively. This public fallback keeps the same
-- migrations runnable on the PostgreSQL 14 development runtime.
CREATE OR REPLACE FUNCTION public.uuidv7()
RETURNS UUID
LANGUAGE SQL
VOLATILE
PARALLEL SAFE
AS 'SELECT gen_random_uuid()';
