CREATE TABLE outbox_events (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(255) NOT NULL DEFAULT 'demo',
    event_type VARCHAR(100) NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id VARCHAR(255),
    payload TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP WITH TIME ZONE,
    attempts INTEGER NOT NULL DEFAULT 0,
    last_error TEXT
);

CREATE INDEX idx_outbox_pending ON outbox_events (published_at, created_at);
CREATE INDEX idx_outbox_tenant ON outbox_events (tenant_id, created_at DESC);
