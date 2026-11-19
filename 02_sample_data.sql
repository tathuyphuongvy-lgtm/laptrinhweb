-- Tùy chọn: chỉ chạy SAU khi ứng dụng đã tạo bảng lần đầu.
USE servletjpa;
INSERT INTO categories (CategoryName, Images, status)
SELECT 'Điện thoại', NULL, 1 WHERE NOT EXISTS (SELECT 1 FROM categories WHERE CategoryName = 'Điện thoại');
INSERT INTO categories (CategoryName, Images, status)
SELECT 'Laptop', NULL, 1 WHERE NOT EXISTS (SELECT 1 FROM categories WHERE CategoryName = 'Laptop');
INSERT INTO categories (CategoryName, Images, status)
SELECT 'Phụ kiện', NULL, 0 WHERE NOT EXISTS (SELECT 1 FROM categories WHERE CategoryName = 'Phụ kiện');
