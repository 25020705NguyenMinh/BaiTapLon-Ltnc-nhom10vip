# TECH AUCTION

TECH AUCTION là ứng dụng đấu giá trực tuyến dành cho người mua và người bán. Người dùng có thể khám phá sản phẩm, theo dõi phiên đấu giá và xem lịch sử giá.

Dự án sử dụng **JavaFX** cho giao diện và **Spring Boot** cho backend, gồm ba phần: `client`, `server` và `shared`.

## Hiện đã có

- Giao diện khám phá sản phẩm, danh sách và chi tiết phiên đấu giá.
- Giao diện đăng ký, đăng nhập và xác minh OTP.
- Biểu đồ lịch sử đặt giá trong cửa sổ chi tiết.
- Backend quản lý sản phẩm, tạo phiên, đặt giá và lưu lịch sử.
- Xử lý đặt giá đồng thời và gia hạn 60 giây khi có bid hợp lệ trong 10 giây cuối.

**Một số màn hình và biểu đồ hiện dùng dữ liệu mẫu.** Backend đã có, nhưng giao diện chưa kết nối đầy đủ với dữ liệu thật.

## Cần bổ sung

- Kết nối các màn hình với backend để thêm, sửa, xóa sản phẩm và đặt giá trực tiếp trên giao diện.
- Cập nhật giá và biểu đồ ngay khi có lượt đặt mới.
- Tự kết thúc phiên và hiển thị người thắng.
- Hoàn thiện cấu hình email, đăng nhập Google và database MySQL.
- Tiếp tục kiểm thử và cải thiện trải nghiệm sử dụng.

## Build và test

Cần **JDK 21** và **Maven 3.9 trở lên**. Mở terminal tại thư mục gốc có `pom.xml`:

```bash
mvn clean install
mvn test
```

Lệnh đầu build toàn bộ dự án và chạy test. Lệnh thứ hai dùng khi chỉ muốn chạy lại test. Bộ test hiện sử dụng database H2 riêng.
