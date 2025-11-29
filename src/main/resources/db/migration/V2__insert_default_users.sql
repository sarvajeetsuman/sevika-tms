-- Insert default admin user
-- Password: admin123
INSERT INTO users (id, username, email, password, first_name, last_name, role, enabled, created_at)
VALUES (
    gen_random_uuid(),
    'admin',
    'admin@sevika.online',
    '$2a$10$dXJ3SW6G7P5QSM9kcBQhPOLYHYL4i6M6TQVXvl7H0CaU0gPPnBQ0q',
    'Admin',
    'User',
    'ADMIN',
    true,
    CURRENT_TIMESTAMP
);

-- Insert demo regular user
-- Password: demo123
INSERT INTO users (id, username, email, password, first_name, last_name, role, enabled, created_at)
VALUES (
    gen_random_uuid(),
    'demo',
    'demo@sevika.online',
    '$2a$10$dXJ3SW6G7P5QSM9kcBQhPOLYHYL4i6M6TQVXvl7H0CaU0gPPnBQ0q',
    'Demo',
    'User',
    'USER',
    true,
    CURRENT_TIMESTAMP
);
