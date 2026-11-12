# Cài MySQL và nối TECH AUCTION

## Hiểu ba phần trước

- **JavaFX client**: cửa sổ app bạn bấm để đăng ký, đăng bán, đặt giá.
- **Spring Boot server**: nhận yêu cầu, kiểm tra quyền và cập nhật dữ liệu.
- **MySQL Server**: chương trình giữ dữ liệu lâu dài. **MySQL Workbench** chỉ là công cụ để xem/chạy SQL; cài Workbench riêng chưa có MySQL Server.

Client → API server ở cổng **8080** → MySQL ở cổng **3306** (mặc định). Client không cần biết mật khẩu MySQL.

## MySQL local trên Windows

### 1. Cài server

Tải **MySQL Community Server 8.4 LTS, Windows x86 64-bit MSI** tại <https://dev.mysql.com/downloads/mysql/8.4.html>.
Cài xong chạy **MySQL Configurator** đi kèm:

- Chọn cấu hình Development, TCP/IP, cổng `3306`.
- Đặt và ghi nhớ mật khẩu `root` — đây là tài khoản quản trị database.
- Cho chạy dưới dạng Windows Service và khởi động cùng Windows.
- Apply Configuration và hoàn tất. Không cần mở cổng router để chạy local.

Có thể cài thêm Workbench từ <https://dev.mysql.com/downloads/workbench/>. Nếu đã có MySQL hoạt động thì bỏ qua bước cài.

### 2. Tạo database và user cho app

Mở Workbench, tạo kết nối đến `127.0.0.1`, cổng `3306`, user `root`, nhập mật khẩu vừa đặt. Mở SQL tab, thay `TU_DAT_MAT_KHAU_MANH` rồi chạy:

```sql
CREATE DATABASE IF NOT EXISTS auction
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'auction_app'@'localhost'
  IDENTIFIED BY 'TU_DAT_MAT_KHAU_MANH';
GRANT ALL PRIVILEGES ON auction.* TO 'auction_app'@'localhost';
```

User app chỉ được thao tác trong database `auction`. Nếu đã tạo `auction_app` từ trước, `CREATE USER IF NOT EXISTS` **không đổi mật khẩu**; muốn đổi hãy dùng `ALTER USER 'auction_app'@'localhost' IDENTIFIED BY 'MAT_KHAU_MOI';`.

### 3. Điền cấu hình app

Tại thư mục gốc repo, chạy PowerShell:

```powershell
Copy-Item config/application-local.example.properties config/application-local.properties
notepad config/application-local.properties
```

Sửa đúng hai dòng:

```properties
spring.datasource.username=auction_app
spring.datasource.password=MAT_KHAU_BAN_VUA_DAT
```

Dùng mật khẩu của **auction_app**, không phải tài khoản đăng nhập app. File này được git bỏ qua. Chạy server tại **thư mục gốc repo** để file được tự đọc.

Hoặc không dùng file, đặt biến môi trường trong chính PowerShell sắp chạy server:

```powershell
$env:DB_USER = 'auction_app'
$env:DB_PASSWORD = 'MAT_KHAU_BAN_VUA_DAT'
```

Chọn một cách cấu hình để tránh nhầm mật khẩu; file local được ưu tiên so với các giá trị DB_* mặc định trong file app.

### 4. Build và mở app

```powershell
mvn clean install
java -jar server/target/server-1.0-SNAPSHOT.jar
```

Hibernate sẽ tạo bảng khi server khởi động; không cần tự viết các bảng. Test trong `mvn clean install` dùng H2 riêng, nên **test xanh chưa chứng minh mật khẩu MySQL đúng**.

Mở PowerShell thứ hai tại repo:

```powershell
mvn -pl client javafx:run
```

Sau khi server chạy, trong Workbench kiểm tra:

```sql
USE auction;
SHOW TABLES;
SELECT id, full_name, email, role, status FROM users;
```

Database mới trống là bình thường. Đăng ký và xác minh tài khoản trên app; mặc định OTP xuất hiện trong console server. Tạo admin đầu tiên theo README để duyệt sản phẩm.

## Cả nhóm chạy với nhau

Nếu mỗi bạn chạy `localhost` và MySQL riêng, dữ liệu của bốn máy **không tự đồng bộ**.
Cách đơn giản khi thử cùng mạng Wi-Fi: một máy chạy server + MySQL; các máy còn lại chỉ chạy JavaFX, trỏ đến IP LAN của máy server:

```powershell
$env:JAVA_TOOL_OPTIONS = '-Dauction.server.url=http://192.168.1.10:8080'
mvn -pl client javafx:run
```

Thay IP thật; Windows Firewall trên máy server cần cho phép Java/cổng 8080 trong mạng riêng. MySQL vẫn nằm trên máy server, không cần mở cổng 3306 cho các máy client.

## MySQL cloud Railway

Chưa tạo hay chuyển dữ liệu lên cloud trong lần tích hợp này. Có thể dùng cấu hình sẵn khi nhóm muốn làm issue #26.

1. Trong Railway project, thêm MySQL service.
2. Nếu Spring Boot vẫn chạy trên laptop, bật **Public Access/TCP Proxy** trong Settings → Networking. Lấy host và port công khai (port có thể khác 3306).
3. Xem username, password và database của service. Nếu Spring Boot cũng chạy trong cùng Railway project, dùng địa chỉ private của service.
4. Xóa/comment cấu hình MySQL local trước khi chuyển sang biến môi trường:

```powershell
$env:DB_HOST = 'HOST_CONG_KHAI_TU_RAILWAY'
$env:DB_PORT = 'PORT_CONG_KHAI_TU_RAILWAY'
$env:DB_NAME = 'TEN_DATABASE_TU_RAILWAY'
$env:DB_USER = 'USER_TU_RAILWAY'
$env:DB_PASSWORD = 'MAT_KHAU_TU_RAILWAY'
java -jar server/target/server-1.0-SNAPSHOT.jar
```

Nếu dùng `DB_URL`, phải là `jdbc:mysql://HOST:PORT/DATABASE?...`, **không** dán nguyên chuỗi `mysql://user:password@...` vào đó; user/password đặt riêng. Railway cung cấp biến `MYSQLHOST`, `MYSQLPORT`, `MYSQLUSER`, `MYSQLPASSWORD`, `MYSQLDATABASE`; map chúng sang DB_* hoặc SPRING_DATASOURCE_* của app.

Chỉ chuyển MySQL lên cloud thì các laptop vẫn có thể chạy server riêng; chưa phải một backend chung trên Internet. Nhóm nên thống nhất một server API để realtime hoạt động giữa các người dùng. Dữ liệu MySQL cũ cũng không tự chuyển lên dịch vụ mới.

## Lỗi thường gặp

| Hiện tượng | Cần kiểm tra |
| --- | --- |
| `mvn` không nhận diện | Cài Maven, thêm `bin` vào PATH, mở lại PowerShell/VS Code. |
| `JAVA_HOME` sai hoặc `release 21 not supported` | Trỏ JAVA_HOME tới **JDK** 21+, không phải JRE; kiểm tra `mvn -v`. |
| `Communications link failure` / connection refused | MySQL Service đã chạy chưa? Host/port đúng chưa? |
| `Access denied for user` | Username/password MySQL, user host và quyền trong `auction`. |
| `Unknown database 'auction'` | Chạy câu `CREATE DATABASE` ở bước 2. |
| Cổng 8080 đã được dùng | Tắt server cũ hoặc đặt `$env:PORT='8082'` và đổi URL client tương ứng. |
| Test thành công nhưng server không chạy | Test dùng H2; cần hoàn tất cấu hình MySQL thật. |
| Không có email OTP | Mặc định đọc mã trong console server; muốn mail thật bật profile `mail` và cấu hình SMTP. |

Nguồn: [MySQL Windows installation](https://dev.mysql.com/doc/refman/8.4/en/windows-installation.html), [MySQL Configurator](https://dev.mysql.com/doc/refman/8.4/en/mysql-configurator.html), [Railway MySQL](https://docs.railway.com/databases/mysql).
