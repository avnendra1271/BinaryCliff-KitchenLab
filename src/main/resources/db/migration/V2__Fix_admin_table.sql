-- ══════════════════════════════════════════════════════════════
-- Fix Admin Table UUID Issues
-- ════════════════════════════════════════════════════════════════

-- Drop existing tables to recreate with correct UUID configuration
DROP TABLE IF EXISTS admin_permissions CASCADE;
DROP TABLE IF EXISTS admins CASCADE;

-- Re-create admins table with correct UUID configuration
CREATE TABLE admins (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    restaurant_id UUID NOT NULL DEFAULT uuid_generate_v4(),
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    profile_image_url VARCHAR(500),
    role VARCHAR(20) NOT NULL DEFAULT 'ADMIN',
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    last_login_at TIMESTAMP,
    last_login_ip VARCHAR(45),
    password_changed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Re-create admin_permissions table
CREATE TABLE admin_permissions (
    admin_id UUID NOT NULL,
    permission VARCHAR(100) NOT NULL,
    PRIMARY KEY (admin_id, permission),
    FOREIGN KEY (admin_id) REFERENCES admins(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_admins_username ON admins(username);
CREATE INDEX idx_admins_email ON admins(email);
CREATE INDEX idx_admins_restaurant_id ON admins(restaurant_id);
CREATE INDEX idx_admins_enabled ON admins(is_enabled, is_deleted);
CREATE INDEX idx_admins_role ON admins(role);
CREATE INDEX idx_admins_last_login ON admins(last_login_at);

-- Insert default super admin user with BCrypt password
INSERT INTO admins (
    username, 
    password, 
    email, 
    first_name, 
    last_name, 
    role, 
    is_enabled, 
    is_deleted,
    created_at,
    updated_at
) VALUES (
    'admin',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', -- admin123 (BCrypt)
    'admin@kitchenlab.com',
    'System',
    'Administrator',
    'SUPER_ADMIN',
    TRUE,
    FALSE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Insert basic permissions for admin
INSERT INTO admin_permissions (admin_id, permission)
SELECT id, permission FROM admins CROSS JOIN (VALUES 
    ('USER_READ'), ('USER_WRITE'), ('USER_DELETE'),
    ('ROLE_READ'), ('ROLE_WRITE'), ('ROLE_DELETE'),
    ('MENU_READ'), ('MENU_WRITE'), ('MENU_DELETE'),
    ('ORDER_READ'), ('ORDER_WRITE'), ('ORDER_DELETE'),
    ('SYSTEM_CONFIG'), ('SYSTEM_ADMIN')
) AS t(permission)
WHERE username = 'admin';
