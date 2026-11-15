-- Chỉ chạy sau khi ứng dụng khởi động và tạo bảng lần đầu.
USE fashion_jpa;
INSERT INTO categories (CategoryName, Images, status) SELECT 'Áo thun', NULL, 1 WHERE NOT EXISTS (SELECT 1 FROM categories WHERE CategoryName = 'Áo thun');
INSERT INTO categories (CategoryName, Images, status) SELECT 'Áo sơ mi', NULL, 1 WHERE NOT EXISTS (SELECT 1 FROM categories WHERE CategoryName = 'Áo sơ mi');
INSERT INTO categories (CategoryName, Images, status) SELECT 'Áo khoác', NULL, 1 WHERE NOT EXISTS (SELECT 1 FROM categories WHERE CategoryName = 'Áo khoác');
INSERT INTO categories (CategoryName, Images, status) SELECT 'Phụ kiện', NULL, 0 WHERE NOT EXISTS (SELECT 1 FROM categories WHERE CategoryName = 'Phụ kiện');
INSERT INTO categories (CategoryName, Images, status) SELECT 'Quần dài', NULL, 1 WHERE NOT EXISTS (SELECT 1 FROM categories WHERE CategoryName = 'Quần dài');
INSERT INTO categories (CategoryName, Images, status) SELECT 'Váy đầm', NULL, 1 WHERE NOT EXISTS (SELECT 1 FROM categories WHERE CategoryName = 'Váy đầm');
