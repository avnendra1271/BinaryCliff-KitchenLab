-- Insert default admin user
INSERT INTO admins (id, username, password, email, first_name, last_name, role, is_enabled, is_deleted, created_at, updated_at, restaurant_id) 
VALUES (
    uuid_generate_v4(),
    'admin',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', -- admin123 (BCrypt)
    'admin@kitchenlab.com',
    'System',
    'Administrator',
    'SUPER_ADMIN',
    true,
    false,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '123e4567-e89b-12d3-a456-426614174000'
) ON CONFLICT (username) DO NOTHING;
