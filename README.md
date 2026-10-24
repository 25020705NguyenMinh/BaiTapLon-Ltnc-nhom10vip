# Đăng nhập
<img width="1917" height="1013" alt="image" src="https://github.com/user-attachments/assets/c784b140-2620-4771-8d53-57e20b713ce3" />

Mai tôi sẽ bổ xung logo Gg face, linkln
# Lấy lại mật khẩu
<img width="1917" height="996" alt="image" src="https://github.com/user-attachments/assets/95665316-6919-48f4-86e7-7dd4d9239fdd" />

phần này cần xác thực OTP và kiểm tra đầu vào xem đúng cú pháp gmail chưa
# Đăng ký tài khoản
<img width="1917" height="1015" alt="image" src="https://github.com/user-attachments/assets/a996acd8-b611-4658-b4ed-5e10da64e6a4" />

có thích thêm tranh ảnh gì không chứ tôi thấy ok rồi

<img width="1917" height="1015" alt="image" src="https://github.com/user-attachments/assets/edd79f39-459b-419b-87c1-e2c4b678bd38" />

phần này cần xác thực OTP và kiểm tra đầu vào xem đúng cú pháp gmail chưa

<img width="1917" height="1016" alt="image" src="https://github.com/user-attachments/assets/dd00139e-729d-4bbf-9cf2-37e44ffbd03a" />


##  Định hướng phát triển

### 1. Hoàn thiện chức năng xác thực người dùng

* [ ] **Đăng nhập**

  * [ ] Kiểm tra dữ liệu đầu vào (email, mật khẩu).
  * [ ] Xác thực thông tin đăng nhập với hệ thống.
  * [ ] Hiển thị thông báo khi đăng nhập thành công hoặc thất bại.
  * [ ] Điều hướng người dùng đến giao diện phù hợp với vai trò tài khoản.

* [ ] **Đăng ký tài khoản**

  * [ ] Xây dựng giao diện đăng ký.
  * [ ] Kiểm tra tính hợp lệ của họ tên, email và mật khẩu.
  * [ ] Kiểm tra email đã được sử dụng hay chưa.
  * [ ] Lưu thông tin tài khoản vào cơ sở dữ liệu.
  * [ ] Yêu cầu xác minh email sau khi đăng ký.

* [ ] **Đăng xuất**

  * [ ] Xây dựng chức năng đăng xuất.
  * [ ] Xóa thông tin phiên đăng nhập hiện tại.
  * [ ] Điều hướng người dùng về giao diện đăng nhập.

### 2. Kết nối cơ sở dữ liệu (Server)

* [ ] **Thiết lập cơ sở dữ liệu**

  * [ ] Lựa chọn hệ quản trị cơ sở dữ liệu phù hợp.
  * [ ] Thiết kế cấu trúc bảng người dùng.
  * [ ] Xây dựng bảng lưu thông tin tài khoản và vai trò.
  * [ ] Thiết lập kết nối giữa Server và cơ sở dữ liệu.

* [ ] **Xây dựng API xác thực**

  * [ ] Xây dựng API đăng ký tài khoản.
  * [ ] Xây dựng API đăng nhập.
  * [ ] Xây dựng API đăng xuất hoặc quản lý phiên đăng nhập.
  * [ ] Xây dựng API kiểm tra thông tin tài khoản.
  * [ ] Xử lý lỗi và trả về kết quả phù hợp cho Client.

* [ ] **Đảm bảo an toàn dữ liệu**

  * [ ] Mã hóa mật khẩu bằng thuật toán băm an toàn.
  * [ ] Kiểm tra dữ liệu đầu vào tại Server.
  * [ ] Ngăn chặn việc đăng ký nhiều tài khoản bằng cùng một email.
  * [ ] Không lưu mật khẩu dạng văn bản thuần trong cơ sở dữ liệu.

### 3. Tích hợp xác minh email khi đăng ký

* [ ] **Gửi mã OTP**

  * [ ] Tạo mã OTP xác minh email.
  * [ ] Gửi mã OTP đến địa chỉ email người dùng đăng ký.
  * [ ] Thiết lập thời hạn hiệu lực cho mã OTP.
  * [ ] Giới hạn số lần yêu cầu gửi lại mã OTP.

* [ ] **Xác minh mã OTP**

  * [ ] Xây dựng giao diện nhập mã OTP.
  * [ ] Kiểm tra mã OTP người dùng cung cấp.
  * [ ] Thông báo khi mã OTP không chính xác hoặc đã hết hạn.
  * [ ] Cho phép người dùng yêu cầu gửi lại mã OTP khi cần.

* [ ] **Hoàn tất đăng ký**

  * [ ] Đánh dấu email đã được xác minh sau khi xác thực thành công.
  * [ ] Hoàn tất quá trình đăng ký tài khoản.
  * [ ] Ngăn chặn việc sử dụng các chức năng yêu cầu xác minh email khi tài khoản chưa được xác minh.

### 4. Hoàn thiện quy trình lấy lại mật khẩu

* [ ] **Yêu cầu khôi phục mật khẩu**

  * [ ] Xây dựng giao diện quên mật khẩu.
  * [ ] Cho phép người dùng nhập email đã đăng ký.
  * [ ] Kiểm tra yêu cầu khôi phục mật khẩu tại Server.
  * [ ] Gửi mã OTP hoặc liên kết khôi phục mật khẩu đến email.

* [ ] **Xác minh yêu cầu**

  * [ ] Xây dựng giao diện nhập mã OTP hoặc xác nhận liên kết.
  * [ ] Kiểm tra mã xác minh và thời hạn hiệu lực.
  * [ ] Giới hạn số lần thử mã xác minh.
  * [ ] Cho phép yêu cầu gửi lại mã khi cần.

* [ ] **Đặt lại mật khẩu**

  * [ ] Xây dựng giao diện nhập mật khẩu mới.
  * [ ] Kiểm tra tính hợp lệ của mật khẩu mới.
  * [ ] Xác nhận mật khẩu mới và mật khẩu nhập lại trùng khớp.
  * [ ] Cập nhật mật khẩu mới vào cơ sở dữ liệu sau khi xác minh thành công.
  * [ ] Thông báo kết quả và điều hướng người dùng về giao diện đăng nhập.

### 5. Cải thiện khả năng tái sử dụng các thành phần giao diện

* [ ] **Chuẩn hóa giao diện**

  * [ ] Xây dựng bộ thành phần giao diện dùng chung.
  * [ ] Chuẩn hóa kiểu chữ, màu sắc, khoảng cách và kích thước.
  * [ ] Thống nhất kiểu hiển thị của nút bấm, ô nhập liệu và thông báo.

* [ ] **Tái sử dụng thành phần**

  * [ ] Tạo các thành phần nhập liệu dùng chung.
  * [ ] Tạo các thành phần nút bấm dùng chung.
  * [ ] Tái sử dụng các thành phần thông báo lỗi và thông báo thành công.
  * [ ] Tái sử dụng bố cục chung giữa các màn hình đăng nhập, đăng ký và quên mật khẩu.

* [ ] **Tối ưu cấu trúc giao diện**

  * [ ] Tách biệt bố cục FXML và định dạng CSS.
  * [ ] Hạn chế lặp lại mã nguồn giữa các màn hình.
  * [ ] Chuẩn hóa cách chuyển đổi giữa các giao diện.
  * [ ] Kiểm tra tính nhất quán của giao diện sau khi tái cấu trúc.

### 6. Kiểm thử và hoàn thiện

* [ ] Kiểm thử các trường hợp đăng nhập, đăng ký và đăng xuất.
* [ ] Kiểm thử xác minh email bằng OTP.
* [ ] Kiểm thử quy trình lấy lại mật khẩu.
* [ ] Kiểm thử kết nối giữa Client, Server và cơ sở dữ liệu.
* [ ] Kiểm tra các trường hợp dữ liệu không hợp lệ và lỗi kết nối.
* [ ] Cập nhật tài liệu hướng dẫn cài đặt và sử dụng trong README.


