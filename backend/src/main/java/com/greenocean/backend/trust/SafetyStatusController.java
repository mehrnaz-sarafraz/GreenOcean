package com.greenocean.backend.trust;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.UUID;

/** Own coordination receipts only; source evidence retains its original access rules. */
@RestController
public class SafetyStatusController {
 private final JdbcTemplate db;public SafetyStatusController(JdbcTemplate db){this.db=db;}
 @GetMapping("/api/v1/account/safety-cases")public Object mine(@AuthenticationPrincipal Jwt j){UUID who=UUID.fromString(j.getSubject());return db.queryForList("""
  SELECT c.id,c.domain,c.category,c.state,c.updated_at,CASE WHEN c.source_kind='REPORT' THEN EXISTS(SELECT 1 FROM moderation_appeals a WHERE a.report_id=c.source_id AND a.status IN('OPEN','REVIEWING')) WHEN c.source_kind='REPUTATION_CASE' THEN EXISTS(SELECT 1 FROM clinic_reputation_cases a WHERE a.id=c.source_id AND a.kind='RANKING_APPEAL' AND a.state='OPEN') ELSE FALSE END AS source_appeal_pending FROM trust_cases c
  WHERE (c.source_kind='REPORT' AND EXISTS(SELECT 1 FROM reports r WHERE r.id=c.source_id AND r.reporter_id=?))
    OR (c.source_kind='SUPPORT_REQUEST' AND EXISTS(SELECT 1 FROM support_requests r WHERE r.id=c.source_id AND r.user_id=?))
    OR (c.source_kind='CLINIC_INCIDENT' AND EXISTS(SELECT 1 FROM clinic_operational_incidents r WHERE r.id=c.source_id AND r.reporter_id=?))
    OR (c.source_kind='REPUTATION_CASE' AND EXISTS(SELECT 1 FROM clinic_reputation_cases r WHERE r.id=c.source_id AND r.requester_id=?))
  ORDER BY c.updated_at DESC,c.id DESC LIMIT 100
  """,who,who,who,who);}
}
