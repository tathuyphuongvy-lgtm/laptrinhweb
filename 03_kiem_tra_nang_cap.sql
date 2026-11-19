-- Chỉ kiểm tra sau khi bản mới khởi động thành công với hibernate.hbm2ddl.auto=update.
-- Sao lưu DB cũ trước khi nâng cấp. Script không xóa dữ liệu.
USE servletjpa;
SHOW TABLES;
SHOW CREATE TABLE users;
SHOW CREATE TABLE otp_tokens;
SHOW CREATE TABLE videos;
SELECT VideoId, Title, user_id, source, created_at FROM videos;
-- Video cũ không có user_id/source chưa được đưa ra giao diện để tránh gán nhầm chủ sở hữu.
-- Khi xác định đúng chủ sở hữu, tự sửa và chạy ví dụ sau (không chạy nguyên placeholder):
-- UPDATE videos SET user_id=(SELECT id FROM users WHERE username='USERNAME_THAT')
-- WHERE VideoId='VIDEO_ID_THAT' AND user_id IS NULL;
-- source phải là link MP4/WebM/OGG trực tiếp hoặc tên UUID của file trong VIDEO_DIR.
-- Có thể dùng form Thêm video để đăng lại bản cũ với tài khoản chính xác.
