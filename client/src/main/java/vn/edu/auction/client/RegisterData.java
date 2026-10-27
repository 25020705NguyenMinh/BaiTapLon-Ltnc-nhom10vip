package vn.edu.auction.client;

/**
 * FILE MỚI - giữ dữ liệu người dùng đã chọn khi đi qua 3 trang đăng ký:
 * trang 1 (chọn Buyer/Seller) -> trang 2 (nhập email) -> trang 3 (thiết lập tài khoản).
 */
public final class RegisterData {

    public static String role = "BUYER";   // "BUYER" hoặc "SELLER"
    public static String email = "";

    private RegisterData() {
    }

    public static void reset() {
        role = "BUYER";
        email = "";
    }
}
