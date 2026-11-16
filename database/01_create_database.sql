-- Chạy trong MySQL Workbench trước khi chạy web lần đầu.
CREATE DATABASE IF NOT EXISTS fashion_jpa
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE fashion_jpa;
-- Hibernate tự tạo categories + videos và khóa ngoại khi khởi động.
-- Không cần chạy CREATE TABLE bằng tay.
