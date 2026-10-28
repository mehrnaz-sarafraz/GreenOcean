-- Forward correction: bind mode/jurisdiction to the same live credential, with unambiguous positional parameters.
CREATE FUNCTION professional_verification_live(application UUID) RETURNS BOOLEAN LANGUAGE SQL STABLE AS $$
 SELECT EXISTS(SELECT 1 FROM professional_verifications v JOIN professional_profiles p ON p.user_id=v.professional_user_id JOIN users u ON u.id=p.user_id
 WHERE v.id=$1 AND u.status='ACTIVE' AND p.verification_status='VERIFIED' AND v.qualification_state='VERIFIED' AND v.status='APPROVED'
 AND v.reviewed_by<>v.professional_user_id AND v.reviewed_at IS NOT NULL AND v.valid_from<=CURRENT_TIMESTAMP AND v.valid_until>CURRENT_TIMESTAMP AND v.recheck_at>CURRENT_TIMESTAMP
 AND v.professional_category=p.professional_type AND v.license_identifier=p.license_number
 AND length(trim(v.evidence_reference))>=10 AND length(trim(v.verification_source))>=10
 AND EXISTS(SELECT 1 FROM user_roles ur JOIN roles r ON r.id=ur.role_id JOIN users reviewer ON reviewer.id=ur.user_id WHERE ur.user_id=v.reviewed_by AND reviewer.status='ACTIVE' AND r.name IN('ADMIN','MODERATOR'))
 AND EXISTS(SELECT 1 FROM professional_application_documents WHERE application_id=v.id)
 AND NOT EXISTS(SELECT 1 FROM professional_application_documents WHERE application_id=v.id AND(scan_status<>'CLEAN' OR encryption_version<>1 OR scanner_reference IS NULL)))
$$;
CREATE OR REPLACE FUNCTION professional_qualification_live(who UUID) RETURNS BOOLEAN LANGUAGE SQL STABLE AS $$
 SELECT EXISTS(SELECT 1 FROM professional_verifications v WHERE v.professional_user_id=$1 AND professional_verification_live(v.id))
$$;
CREATE OR REPLACE FUNCTION professional_scope_live(who UUID,country CHAR(2),channel TEXT) RETURNS BOOLEAN LANGUAGE SQL STABLE AS $$
 SELECT clinic_release_live($2,$3) AND EXISTS(SELECT 1 FROM professional_scope_authorizations s JOIN professional_verifications v ON v.id=s.application_id
 WHERE v.professional_user_id=$1 AND professional_verification_live(v.id) AND s.country_code=$2 AND s.mode=$3)
$$;
