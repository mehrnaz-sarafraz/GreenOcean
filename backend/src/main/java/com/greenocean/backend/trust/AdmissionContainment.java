package com.greenocean.backend.trust;

import org.springframework.jdbc.core.JdbcTemplate;
import com.greenocean.backend.common.exception.ForbiddenException;

/** Source services call only at creation/admission, never to delete existing obligations. */
public final class AdmissionContainment {
 private AdmissionContainment(){}
 public static boolean paused(JdbcTemplate db,String control){return Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM operation_containment c WHERE c.control=? AND c.action='PAUSE' AND c.approved_at IS NOT NULL AND c.revoked_at IS NULL AND NOT EXISTS(SELECT 1 FROM operation_containment n WHERE n.incident_id=c.incident_id AND n.control=c.control AND n.target_user IS NOT DISTINCT FROM c.target_user AND n.approved_at>c.approved_at AND n.action='RESUME' AND n.approved_at IS NOT NULL AND n.revoked_at IS NULL))",Boolean.class,control));}
 public static void require(JdbcTemplate db,String control){db.queryForList("SELECT pg_advisory_xact_lock_shared(hashtextextended(?,0))","containment:"+control);if(paused(db,control))throw new ForbiddenException("New service admission is paused for an operational review; existing rights and obligations remain available");}
}
