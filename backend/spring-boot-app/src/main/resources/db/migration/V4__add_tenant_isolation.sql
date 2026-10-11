-- Backfill legacy rows into the local/demo tenant. Secure deployments must issue tenant claims.
ALTER TABLE employee ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(255);
ALTER TABLE rag_documents ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(255);
ALTER TABLE audit_events ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(255);
UPDATE employee SET tenant_id = 'demo' WHERE tenant_id IS NULL;
UPDATE rag_documents SET tenant_id = 'demo' WHERE tenant_id IS NULL;
UPDATE audit_events SET tenant_id = 'demo' WHERE tenant_id IS NULL;
ALTER TABLE employee ALTER COLUMN tenant_id SET DEFAULT 'demo';
ALTER TABLE employee ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE rag_documents ALTER COLUMN tenant_id SET DEFAULT 'demo';
ALTER TABLE rag_documents ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE audit_events ALTER COLUMN tenant_id SET DEFAULT 'demo';
ALTER TABLE audit_events ALTER COLUMN tenant_id SET NOT NULL;
-- Legacy Hibernate-created constraints can have generated names.
DO $$ DECLARE constraint_name TEXT;
BEGIN
    FOR constraint_name IN
        SELECT c.conname FROM pg_constraint c
        WHERE c.conrelid = 'employee'::regclass AND c.contype = 'u'
          AND c.conkey = ARRAY[(SELECT attnum FROM pg_attribute
              WHERE attrelid = 'employee'::regclass AND attname = 'employee_number')]::smallint[]
    LOOP
        EXECUTE format('ALTER TABLE employee DROP CONSTRAINT %I', constraint_name);
    END LOOP;
END $$;
ALTER TABLE employee ADD CONSTRAINT uk_employee_tenant_number UNIQUE (tenant_id, employee_number);
CREATE INDEX idx_employee_tenant ON employee (tenant_id);
CREATE INDEX idx_rag_documents_tenant ON rag_documents (tenant_id);
CREATE INDEX idx_audit_tenant_timestamp ON audit_events (tenant_id, event_timestamp DESC);
