CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(255) NOT NULL,
    recipient_subject VARCHAR(255) NOT NULL,
    source_event_id VARCHAR(255) NOT NULL,
    notification_type VARCHAR(100) NOT NULL,
    title VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uq_notifications_source UNIQUE (tenant_id, recipient_subject, source_event_id)
);

CREATE INDEX idx_notifications_recipient
    ON notifications (tenant_id, recipient_subject, created_at DESC);
CREATE INDEX idx_notifications_cursor ON notifications (tenant_id, recipient_subject, id);
