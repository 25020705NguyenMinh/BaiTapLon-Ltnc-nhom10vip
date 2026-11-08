package vn.edu.auction.client.model;

import vn.edu.auction.shared.dto.AuthResponse;
import vn.edu.auction.shared.enums.UserRole;
/**
 * Lớp Singleton quản lý phiên làm việc (Session) của người dùng ở phía Client.
 * Lưu trạng thái đăng nhập trong bộ nhớ và không lưu mật khẩu ra file
 */

public class SessionData {

    private static SessionData instance;

    private Long userId;
    private String fullName;
    private String email;
    private UserRole role; 
    private String token;

    // singleton pattern
    private SessionData() {}

    //singleton pattern
    public static synchronized SessionData getInstance() {
        if (instance == null) {
            instance = new SessionData();
        }
        return instance;
    }

    /**
     * Cập nhật thông tin session sau khi đăng nhập thành công
     */
    /**
     * Nhận thẳng AuthResponse của server.
     * VÌ SAO: dùng đúng DTO chung, không tự tạo cấu trúc khác, nên server thêm/sửa field
     * thì chỉ cần sửa ở đây.
     */
    public void setSession(AuthResponse response) {
        if (response == null) {
            throw new IllegalArgumentException("AuthResponse không được null");
        }
        this.userId = response.getUserId();
        this.fullName = response.getFullName();
        this.email = response.getEmail();
        this.role = response.getRole();
        this.token = response.getToken();
    }

    /**
     * Xóa sạch thông tin session khi người dùng đăng xuất.
     */
    public void clearSession() {
        this.userId = null;
        this.fullName = null;
        this.email = null;
        this.role = null;
        this.token = null;
    }

    // Các hàm Getter
    public Long getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public UserRole getRole() { return role; }
    public String getToken() { return token; }

    public boolean isLoggedIn() {
        return token != null && !token.isEmpty();
    }

    public void setSession(long l, String string, String string2, String string3, String string4) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setSession'");
    }
}
