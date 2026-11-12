-- Chạy bằng MySQL Workbench / mysql CLI trước lần khởi động server đầu tiên.
-- Xem docs/MYSQL.md để tạo user auction_app và cấu hình mật khẩu.
CREATE DATABASE IF NOT EXISTS auction CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE auction;

-- Các bảng do Hibernate tự tạo khi chạy server lần đầu (ddl-auto=update):
--   users, items, auctions, bid_transactions, otps,
--   wallet_transactions, notifications, auction_watches, user_follows, chat_messages

-- Sau khi chạy server và ĐĂNG KÝ + XÁC MINH 1 tài khoản thường, nâng nó lên ADMIN:
-- UPDATE users SET role = 'ADMIN' WHERE email = 'email-cua-ban@example.com' AND status = 'ACTIVE';

-- Xem nhanh dữ liệu để kiểm tra:
-- SELECT id, full_name, email, role, status, wallet_balance FROM users;
-- SELECT id, item_id, current_price, highest_bidder_id, status, hidden, end_time FROM auctions;
