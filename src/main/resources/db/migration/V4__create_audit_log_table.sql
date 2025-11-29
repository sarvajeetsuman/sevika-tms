-- V4__create_audit_log_table.sql
-- Migration to create audit log table for activity tracking

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    entity_type VARCHAR(30) NOT NULL,
    entity_id VARCHAR(100) NOT NULL,
    action VARCHAR(30) NOT NULL,
    user_id UUID NOT NULL,
    username VARCHAR(100) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    timestamp TIMESTAMP NOT NULL,
    description VARCHAR(500),
    CONSTRAINT chk_entity_type CHECK (entity_type IN ('USER', 'PROJECT', 'TASK', 'SUBSCRIPTION', 'SUBSCRIPTION_PLAN', 'PAYMENT')),
    CONSTRAINT chk_action CHECK (action IN ('CREATED', 'UPDATED', 'DELETED', 'VIEWED', 'LOGIN', 'LOGOUT', 'STATUS_CHANGED', 'ASSIGNED', 'UNASSIGNED'))
);

-- Create indexes for better query performance
CREATE INDEX idx_audit_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_user ON audit_logs(user_id);
CREATE INDEX idx_audit_action ON audit_logs(action);
CREATE INDEX idx_audit_timestamp ON audit_logs(timestamp);

-- Add comments for documentation
COMMENT ON TABLE audit_logs IS 'Stores audit log entries for all activities in the system';
COMMENT ON COLUMN audit_logs.entity_type IS 'Type of entity being audited (USER, PROJECT, TASK, etc.)';
COMMENT ON COLUMN audit_logs.entity_id IS 'ID of the entity being audited';
COMMENT ON COLUMN audit_logs.action IS 'Action performed (CREATED, UPDATED, DELETED, etc.)';
COMMENT ON COLUMN audit_logs.user_id IS 'ID of the user who performed the action';
COMMENT ON COLUMN audit_logs.username IS 'Username of the user who performed the action';
COMMENT ON COLUMN audit_logs.old_value IS 'JSON representation of the old value (for updates)';
COMMENT ON COLUMN audit_logs.new_value IS 'JSON representation of the new value';
COMMENT ON COLUMN audit_logs.ip_address IS 'IP address of the client';
COMMENT ON COLUMN audit_logs.user_agent IS 'User agent string from the request';
COMMENT ON COLUMN audit_logs.timestamp IS 'Timestamp when the action was performed';
COMMENT ON COLUMN audit_logs.description IS 'Human-readable description of the action';
