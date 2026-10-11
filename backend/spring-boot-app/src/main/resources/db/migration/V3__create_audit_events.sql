CREATE TABLE audit_events (
    id BIGSERIAL PRIMARY KEY,
    event_timestamp TIMESTAMPTZ NOT NULL,
    actor_subject VARCHAR(255) NOT NULL,
    actor_role VARCHAR(64),
    action VARCHAR(120) NOT NULL,
    resource_type VARCHAR(120),
    resource_id VARCHAR(255),
    result VARCHAR(32) NOT NULL,
    correlation_id VARCHAR(128),
    metadata TEXT
);
CREATE INDEX idx_audit_events_timestamp ON audit_events (event_timestamp DESC);
CREATE INDEX idx_audit_events_action ON audit_events (action);
CREATE INDEX idx_audit_events_resource ON audit_events (resource_type, resource_id);
