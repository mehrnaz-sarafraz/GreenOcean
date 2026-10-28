CREATE INDEX listener_planned_end ON live_support_requests(planned_ends_at) WHERE status IN('ACTIVE','INTERRUPTED') AND planned_ends_at IS NOT NULL;
CREATE INDEX listener_feedback_owner ON listener_session_feedback(owner_id,updated_at,session_id);
CREATE INDEX listener_queue_terminal_notice ON listener_wait_queue(updated_at,id) WHERE status IN('EXPIRED','SERVICE_UNAVAILABLE','SAFETY_TERMINATED') AND reminders;
