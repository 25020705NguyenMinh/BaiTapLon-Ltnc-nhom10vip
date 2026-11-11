# TECH AUCTION

TECH AUCTION là ứng dụng đấu giá trực tuyến dành cho người mua và người bán. Người dùng có thể khám phá sản phẩm, theo dõi phiên đấu giá và xem lịch sử giá.

Dự án dùng JavaFX cho giao diện và Spring Boot cho backend, gồm ba module: `client`, `server` và `shared`.

## Tính năng hiện có

- Giao diện khám phá sản phẩm, danh sách và chi tiết phiên đấu giá.
- Giao diện đăng ký, đăng nhập và xác minh OTP.
- Biểu đồ lịch sử đặt giá trong cửa sổ chi tiết.
- Backend quản lý sản phẩm, tạo phiên, đặt giá và lưu lịch sử.
- Xử lý đặt giá đồng thời và gia hạn 60 giây khi có lượt đặt giá hợp lệ trong 10 giây cuối.

Một số màn hình và biểu đồ còn dùng dữ liệu mẫu; giao diện chưa kết nối đầy đủ với dữ liệu thật.

## Yêu cầu

- JDK 21
- Maven 3.9 trở lên
- Git

Kiểm tra cài đặt:

```bash
java -version
mvn -version
```

## Lấy mã nguồn `main`

Lần đầu tải dự án:

```bash
git clone https://github.com/25020705NguyenMinh/BaiTapLon-Ltnc-nhom10vip.git
cd BaiTapLon-Ltnc-nhom10vip
```

Nếu đã clone từ trước, cập nhật mã mới nhất của `main`:

```bash
git switch main
git pull --ff-only origin main
```

## Build và kiểm thử

Chạy từ thư mục gốc, nơi có `pom.xml`:

```bash
mvn clean install
```

Lệnh này build cả ba module và chạy các bài test. Chỉ chạy test lại:

```bash
mvn test
```

## Chạy ứng dụng

Cần mở hai terminal ở thư mục gốc dự án để chạy backend và giao diện cùng lúc.

**Terminal 1 — backend:**

```bash
mvn -pl server org.springframework.boot:spring-boot-maven-plugin:3.5.6:run
```

Server chạy tại `http://localhost:8080`. Database H2 được lưu local ở `data/auction`.

**Terminal 2 — giao diện JavaFX:**

```bash
mvn -pl client javafx:run
```

Giao diện mở cửa sổ `Sàn đấu giá | Khám phá`.

Để kiểm tra server, mở `http://localhost:8080/api/health` trên trình duyệt. Nếu server yêu cầu cấu hình email, hãy đặt biến môi trường `MAIL_PASSWORD` trước khi chạy backend. Để gửi OTP thật qua Gmail, giá trị này cần là App Password của tài khoản gửi mail; không đưa mật khẩu vào Git.

## Chức năng dự kiến bổ sung

- Kết nối các màn hình với backend để thêm, sửa, xóa sản phẩm và đặt giá trực tiếp.
- Cập nhật giá và biểu đồ khi có lượt đặt giá mới.
- Tự kết thúc phiên và hiển thị người thắng.
- Hoàn thiện cấu hình email, đăng nhập Google và database MySQL.
- Tiếp tục kiểm thử và cải thiện trải nghiệm sử dụng.
