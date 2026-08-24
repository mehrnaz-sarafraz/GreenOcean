INSERT INTO roles (name)
VALUES
    ('USER'),
    ('PROFESSIONAL'),
    ('MODERATOR'),
    ('ADMIN')
ON CONFLICT (name) DO NOTHING;
