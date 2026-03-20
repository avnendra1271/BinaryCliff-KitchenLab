-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Insert default restaurant
INSERT INTO restaurants (
    id, name, subdomain, description, email, is_active, allows_online_orders, 
    allows_dine_in, allows_takeout, is_deleted, created_at, updated_at
) VALUES (
    uuid_generate_v4(),
    'Demo Restaurant',
    'demo',
    'A demo restaurant for testing the multi-tenant platform',
    'demo@kitchenlab.com',
    true,
    true,
    true,
    true,
    false,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Insert default admin user
INSERT INTO admins (
    id, restaurant_id, username, password, email, first_name, last_name, 
    role, is_enabled, is_deleted, created_at, updated_at
) VALUES (
    uuid_generate_v4(),
    (SELECT id FROM restaurants WHERE subdomain = 'demo'),
    'admin',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', -- admin123
    'admin@kitchenlab.com',
    'System',
    'Administrator',
    'SUPER_ADMIN',
    true,
    false,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);
