-- Chạy 1 lần bằng MySQL Workbench / mysql CLI (không bắt buộc: server cũng tự tạo DB nhờ createDatabaseIfNotExist=true)
CREATE DATABASE IF NOT EXISTS auction CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE auction;

-- Các bảng do Hibernate tự tạo khi chạy server lần đầu (ddl-auto=update):
--   users, items, auctions, bid_transactions, otps,
--   wallet_transactions, notifications, auction_watches, user_follows, chat_messages

-- Sau khi chạy server 1 lần và ĐĂNG KÝ 1 tài khoản bình thường, nâng nó lên ADMIN:
-- UPDATE users SET role = 'ADMIN' WHERE email = 'admin@gmail.com';

-- Xem nhanh dữ liệu để kiểm tra:
-- SELECT id, full_name, email, role, status, wallet_balance FROM users;
-- SELECT id, item_id, current_price, highest_bidder_id, status, hidden, end_time FROM auctions;
