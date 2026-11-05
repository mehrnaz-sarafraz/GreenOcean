-- Current platform authority remains necessary after an educational grant is issued.
CREATE OR REPLACE FUNCTION educational_permission_live(who UUID,capability TEXT,category TEXT) RETURNS BOOLEAN LANGUAGE SQL STABLE AS $$
 SELECT EXISTS(SELECT 1 FROM educational_permissions p JOIN users u ON u.id=p.user_id
 WHERE p.user_id=who AND p.action=capability AND p.scope=category AND u.status='ACTIVE' AND p.revoked_at IS NULL AND p.expires_at>CURRENT_TIMESTAMP
 AND EXISTS(SELECT 1 FROM users issuer JOIN user_roles ur ON ur.user_id=issuer.id JOIN roles r ON r.id=ur.role_id WHERE issuer.id=p.issued_by AND issuer.status='ACTIVE' AND r.name='ADMIN')
 AND(capability NOT IN('REVIEW','APPROVE','PUBLISH','FEATURE','RETIRE') OR EXISTS(SELECT 1 FROM user_roles ur JOIN roles r ON r.id=ur.role_id WHERE ur.user_id=who AND r.name IN('ADMIN','MODERATOR'))))
$$;
CREATE OR REPLACE FUNCTION educational_credential_live(who UUID,category TEXT) RETURNS BOOLEAN LANGUAGE SQL STABLE AS $$
 SELECT EXISTS(SELECT 1 FROM educational_credentials c JOIN professional_verifications v ON v.id=c.verification_id JOIN professional_profiles p ON p.user_id=c.user_id
 WHERE c.user_id=who AND c.scope=category AND c.revoked_at IS NULL AND c.expires_at>CURRENT_TIMESTAMP AND c.verified_by<>who
 AND p.verification_status='VERIFIED' AND v.status='APPROVED' AND v.reviewed_by<>who AND v.reviewed_at IS NOT NULL
 AND EXISTS(SELECT 1 FROM users reviewer JOIN user_roles ur ON ur.user_id=reviewer.id JOIN roles r ON r.id=ur.role_id WHERE reviewer.id=v.reviewed_by AND reviewer.status='ACTIVE' AND r.name IN('ADMIN','MODERATOR'))
 AND EXISTS(SELECT 1 FROM professional_application_documents d WHERE d.application_id=v.id AND d.scan_status='CLEAN' AND d.scanned_at IS NOT NULL AND d.reviewed_by<>who
  AND EXISTS(SELECT 1 FROM users reviewer JOIN user_roles ur ON ur.user_id=reviewer.id JOIN roles r ON r.id=ur.role_id WHERE reviewer.id=d.reviewed_by AND reviewer.status='ACTIVE' AND r.name IN('ADMIN','MODERATOR')))
 AND EXISTS(SELECT 1 FROM users u JOIN user_roles ur ON ur.user_id=u.id JOIN roles r ON r.id=ur.role_id WHERE u.id=c.verified_by AND u.status='ACTIVE' AND r.name='ADMIN'))
$$;
