package vn.edu.auction.client.auth;

/**
 * FILE MỚI - giữ dữ liệu người dùng đã chọn khi đi qua 3 trang đăng ký:
 *  trang 1 (nhập email) -> trang 2 (thiết lập tài khoản).
 */
public final class RegisterData {

    public static String role = "BIDDER";   // "BUYER" hoặc "SELLER"
    public static String email = "";

    private RegisterData() {
    }

    public static void reset() {
        role = "BIDDER";
        email = "";
    }
}
