-- Legacy regional approvals remain an additional restriction, never a bypass around current typed scope.
CREATE OR REPLACE FUNCTION professional_scope_live(who UUID,country CHAR(2),channel TEXT) RETURNS BOOLEAN LANGUAGE SQL STABLE AS $$
 SELECT clinic_release_live($2,$3)
 AND EXISTS(SELECT 1 FROM professional_care_regions r WHERE r.professional_user_id=$1 AND r.country_code=$2 AND r.approved_by<>$1 AND r.expires_at>CURRENT_TIMESTAMP)
 AND EXISTS(SELECT 1 FROM professional_scope_authorizations s JOIN professional_verifications v ON v.id=s.application_id
 WHERE v.professional_user_id=$1 AND professional_verification_live(v.id) AND s.country_code=$2 AND s.mode=$3)
$$;
