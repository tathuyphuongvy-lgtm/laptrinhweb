USE servletjpa;
-- Chạy ứng dụng với db.schema=update trước để tự bổ sung cột.
SELECT id, username, full_name, phone, email, pendingEmail, images FROM users;
