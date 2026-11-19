-- Chạy trong MySQL Workbench trước khi chạy web lần đầu.
CREATE DATABASE IF NOT EXISTS servletjpa
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE servletjpa;
-- Hibernate tự tạo categories + videos và khóa ngoại khi khởi động.
-- Không cần chạy CREATE TABLE bằng tay.
