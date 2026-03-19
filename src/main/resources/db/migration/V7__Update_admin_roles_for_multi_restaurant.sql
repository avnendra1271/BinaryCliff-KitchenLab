-- ══════════════════════════════════════════════════════════════
-- Update Admin Roles for Multi-Restaurant Platform
-- ══════════════════════════════════════════════════════════════

-- Update existing admin role format
UPDATE admins 
SET role = 'SUPER_ADMIN',
    updated_at = CURRENT_TIMESTAMP
WHERE role = 'ADMIN' OR role = 'SUPER_ADMIN';

-- Add a restaurant admin user for testing
INSERT INTO admins (
    id,
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
    '123e4567-e89b-12d3-a456-426614174000',
    'restaurant',
    '$2a$10$SAn5ZOLveVdkdwpoH0F3veDyvozc8Mgr6g9HRvZJ1p8TLTU6Nr/f6',
    'restaurant@kitchenlab.com',
    'Restaurant',
    'Owner',
    'RESTAURANT_ADMIN',
    TRUE,
    FALSE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);
