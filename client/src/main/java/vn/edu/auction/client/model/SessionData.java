package vn.edu.auction.client.model;

/**
 * Lớp Singleton quản lý phiên làm việc (Session) của người dùng ở phía Client.
 * Lưu trạng thái đăng nhập trong bộ nhớ và không lưu mật khẩu ra file[cite: 1, 2].
 */
public class SessionData {

    private static SessionData instance;

    private Long userId;
    private String fullName;
    private String email;
    private String role; // Hoặc kiểu enum UserRole tương ứng[cite: 2]
    private String token;

    // Private constructor để triển khai Singleton pattern
    private SessionData() {}

    /**
     * Trả về phiên hiện tại (Singleton instance)[cite: 2].
     */
    public static synchronized SessionData getInstance() {
        if (instance == null) {
            instance = new SessionData();
        }
        return instance;
    }

    /**
     * Cập nhật thông tin session sau khi đăng nhập thành công[cite: 2].
     */
    public void setSession(Long userId, String fullName, String email, String role, String token) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.token = token;
    }

    /**
     * Xóa sạch thông tin session khi người dùng đăng xuất[cite: 2].
     */
    public void clearSession() {
        this.userId = null;
        this.fullName = null;
        this.email = null;
        this.role = null;
        this.token = null;
    }

    // Các hàm Getter[cite: 2]
    public Long getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getToken() { return token; }

    public boolean isLoggedIn() {
        return token != null && !token.isEmpty();
    }
}