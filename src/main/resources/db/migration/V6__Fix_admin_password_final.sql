-- ══════════════════════════════════════════════════════════════
-- Fix Admin Password BCrypt Hash - Final
-- ══════════════════════════════════════════════════════════════

-- Update admin password with correct BCrypt hash for 'admin123'
UPDATE admins 
SET password = '$2a$10$SAn5ZOLveVdkdwpoH0F3veDyvozc8Mgr6g9HRvZJ1p8TLTU6Nr/f6',
    updated_at = CURRENT_TIMESTAMP
WHERE username = 'admin';
