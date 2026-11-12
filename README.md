# TECH AUCTION

Ứng dụng đấu giá của nhóm 10: **JavaFX** (client), **Spring Boot** (server), **MySQL** (database), module `shared` chứa DTO và enum dùng chung. Cần **JDK 21+** và **Maven 3.9+**.

## Chạy lần đầu trên Windows

1. Cài và cấu hình MySQL theo [hướng dẫn từng bước](docs/MYSQL.md). Tạo database `auction` và tài khoản `auction_app`.
2. Chép `config/application-local.example.properties` thành `config/application-local.properties`, điền mật khẩu MySQL của bạn.
3. Mở PowerShell tại thư mục gốc của repo (nơi có `pom.xml`), chạy:

```powershell
mvn clean install
java -jar server/target/server-1.0-SNAPSHOT.jar
```

Giữ cửa sổ server mở. Mở **PowerShell thứ hai**, cũng tại thư mục gốc:

```powershell
mvn -pl client javafx:run
```

Server mặc định ở `http://localhost:8080`; kiểm tra bằng trình duyệt: `http://localhost:8080/api/health`.
Nếu dùng JDK 26, bytecode vẫn được biên dịch theo Java 21. Có thể dùng JDK 21 để đồng nhất môi trường của nhóm.

## Thử luồng chính

- Đăng ký, xem mã OTP trong **console server** rồi xác minh và đăng nhập. Nếu bật profile `mail`, OTP gửi qua SMTP thay cho console.
- Khi đăng ký có thể chọn vai trò `BIDDER` hoặc `SELLER`; `ADMIN` được tạo riêng. Cả hai vai trò thường được đăng bán lẫn đặt giá, nhưng không được tự bid sản phẩm của mình.
- Thêm sản phẩm → admin duyệt → người bán tạo phiên → tài khoản khác đặt giá.
- Giá, lịch sử, biểu đồ nhận cập nhật realtime. Khi hết hạn, server chọn người thắng từ lượt bid đã lưu.
- Bid hợp lệ trong 10 giây cuối gia hạn thêm 60 giây, có thể lặp nhiều lần.

Tạo admin đầu tiên: đăng ký và xác minh một tài khoản, rồi chạy trong MySQL (đổi email):

```sql
UPDATE auction.users SET role = 'ADMIN' WHERE email = 'email-cua-ban@example.com' AND status = 'ACTIVE';
```

Đăng nhập lại để giao diện nhận vai trò admin. Sau đó mở menu tài khoản → Trang quản trị để duyệt sản phẩm.

## Kiểm thử

```powershell
mvn test
```

Test tự động dùng H2 riêng, **không cần MySQL và không tác động database app**. Có test transaction/rollback, quyền, gia hạn, đóng phiên và 100 lượt đặt giá đồng thời.

## Cấu hình tùy chọn

- MySQL local: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`; hoặc `DB_URL` là URL JDBC đầy đủ.
- Railway: xem [phần cloud](docs/MYSQL.md#mysql-cloud-railway). Chỉ server giữ thông tin MySQL; client gọi API server.
- SMTP: bật `SPRING_PROFILES_ACTIVE=mail`, điền `MAIL_USER`, `MAIL_PASSWORD`, tùy chọn `MAIL_HOST`, `MAIL_PORT`.
- Google: bật profile `google`, điền `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`. Nếu dùng cả mail và Google: `SPRING_PROFILES_ACTIVE=mail,google`.
- Khi dùng ngoài máy cá nhân, đặt `JWT_SECRET` ngẫu nhiên dài ít nhất 32 ký tự.

Không commit file cấu hình chứa mật khẩu. Thông tin bí mật từng đưa lên GitHub cần đổi/thu hồi tại dịch vụ tương ứng; xóa khỏi file hiện tại không xóa lịch sử Git.
