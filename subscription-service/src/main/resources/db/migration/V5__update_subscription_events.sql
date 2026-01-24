-- V5__update_subscription_events.sql
-- SAFE: only CREATE indexes, no DROP

CREATE INDEX idx_subevent_user_subscription_id
ON subscription_events (user_subscription_id);

CREATE INDEX idx_subevent_event_type
ON subscription_events (event_type);

CREATE INDEX idx_subevent_user_sub_ts
ON subscription_events (user_subscription_id, event_timestamp);
