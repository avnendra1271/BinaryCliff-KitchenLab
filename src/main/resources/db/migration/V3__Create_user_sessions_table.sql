-- ══════════════════════════════════════════════════════════════
-- Create User Sessions Table
-- ══════════════════════════════════════════════════════════════

-- Create user_sessions table
CREATE TABLE user_sessions (
    id VARCHAR(255) PRIMARY KEY,
    admin_id UUID NOT NULL,
    access_token TEXT,
    refresh_token TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    expires_at TIMESTAMP,
    last_accessed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX idx_user_sessions_admin_id ON user_sessions(admin_id);
CREATE INDEX idx_user_sessions_active ON user_sessions(is_active);
CREATE INDEX idx_user_sessions_expires_at ON user_sessions(expires_at);
CREATE INDEX idx_user_sessions_access_token ON user_sessions(access_token);
CREATE INDEX idx_user_sessions_refresh_token ON user_sessions(refresh_token);

-- Add foreign key constraint
ALTER TABLE user_sessions 
ADD CONSTRAINT fk_user_sessions_admin_id 
FOREIGN KEY (admin_id) REFERENCES admins(id) ON DELETE CASCADE;
