# Hướng dẫn chạy bản tích hợp (P3 + P4 + tính năng mở rộng)

## 1. Chạy
1. Cài **MySQL 8**, đảm bảo đang chạy ở cổng 3306 (database `auction` tự tạo nhờ `createDatabaseIfNotExist=true`).
2. Đặt biến môi trường rồi chạy server (`server`, class `MainApplication`):
   - Windows PowerShell: `$env:DB_PASSWORD="mat_khau_mysql"; $env:MAIL_PASSWORD="x"`
   - Linux/macOS: `export DB_PASSWORD=mat_khau_mysql MAIL_PASSWORD=x`
   - Đổi user/host nếu cần: `DB_USER` (mặc định root), `DB_HOST`, `DB_PORT`, `DB_NAME`.
3. Chạy client (`client`). Đổi địa chỉ server bằng `-Dauction.server.url=http://host:8080`.
4. Tạo admin: đăng ký 1 tài khoản bình thường rồi chạy trong MySQL:
   `UPDATE users SET role='ADMIN' WHERE email='email_cua_ban';`

## 2. Những gì đã thêm
### P4 - Backend
- Tự đóng phiên hết hạn: `AuctionScheduler` (1 giây/lần) -> `AuctionClosingService` (khóa dòng, chọn người thắng từ bid đã lưu, FINISHED, sản phẩm SOLD, idempotent).
- WebSocket `/ws/auctions`: `WebSocketConfig`, `AuctionEventPublisher`, `AuctionEvent` (NEW_BID, AUCTION_FINISHED, chỉ gửi sau khi commit).
### P3 - Frontend
- `client/.../api/`: BaseApiClient, FxAsync, ItemApiClient, AuctionApiClient, BidApiClient, AuctionRealtimeClient.
- Danh sách sản phẩm / phiên đấu giá dùng API thật; dialog phiên có ô đặt giá, lịch sử bid, cập nhật giá realtime.
- Quản lý sản phẩm (người bán) dùng API thật + nút "Tạo phiên đấu giá".
### Mở rộng
- Ô user góc phải: Đăng nhập / Đăng xuất / Ví (số dư, nạp, rút, lịch sử) / Thông tin tài khoản (đổi mật khẩu).
- Trang admin (menu user -> "Trang quản trị"): tài khoản (vô hiệu hóa, xóa), sản phẩm chờ duyệt (duyệt/từ chối), phiên đấu giá (dừng, ẩn/hiện).
- Chat giữa các tài khoản (nút 💬), WebSocket `/ws/user`.
- Chuông thông báo 🔔: bid mới, phiên kết thúc, người bán bạn follow đăng sản phẩm mới, kết quả duyệt sản phẩm.
  Trong dialog phiên: Theo dõi sản phẩm, Tắt/Bật thông báo, Theo dõi người bán.
- Database chuyển từ H2 sang MySQL (H2 chỉ còn cho test tự động).

## 3. Quy tắc nghiệp vụ cần biết
- Sản phẩm mới ở trạng thái "Chờ phê duyệt" (DRAFT). Admin duyệt -> ACTIVE mới tạo được phiên.
- "Xóa" tài khoản = xóa mềm (BANNED). Dừng phiên = CANCELED (phát AUCTION_FINISHED với status CANCELED).
- Server dùng giờ UTC; client tự đổi sang giờ máy khi hiển thị và gửi giờ UTC khi tạo phiên.
- Khách chưa đăng nhập vẫn không xem được danh sách (giữ nguyên hành vi cũ mà test đang kiểm tra).
- Ví chưa gắn với đặt giá (chưa giữ cọc / trừ tiền khi thắng).

## 4. Luồng test gợi ý (3 cửa sổ client)
1. Đăng ký seller, bidder, admin (xem mục 1.4).
2. Seller: Quản lý sản phẩm -> thêm sản phẩm (Chờ phê duyệt).
3. Admin: Trang quản trị -> Sản phẩm chờ duyệt -> Duyệt. Seller nhận thông báo 🔔.
4. Seller: chọn sản phẩm -> Tạo phiên đấu giá (vd. 2 phút).
5. Bidder: mở phiên, bấm Theo dõi, Đặt giá. Cửa sổ khác thấy giá nhảy ngay; seller nhận thông báo.
6. Hết giờ: phiên tự FINISHED, mọi người nhận AUCTION_FINISHED, người thắng hiện ở "Người dẫn đầu".
7. Chat: nút 💬 -> chọn người -> nhắn. Admin có thể Dừng/Ẩn phiên ở tab 3.

## 4. Lưu ý kiểm tra
- Phần client đã biên dịch thử; phần server chỉ kiểm tra cú pháp (chưa build bằng Maven do môi trường không tải được thư viện). Hãy chạy `mvn test` và `mvn spring-boot:run` để kiểm tra đầy đủ.
- `P3BackendTest` đã được sửa theo quy tắc mới (sản phẩm phải được duyệt) và thêm test đóng phiên.
