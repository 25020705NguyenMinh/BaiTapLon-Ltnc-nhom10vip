package vn.edu.auction.server.exception;

/** Tài khoản không ở trạng thái ACTIVE (bị khóa / vô hiệu hóa). */
public class AccountNotActiveException extends RuntimeException {

    public AccountNotActiveException() {
        super("Tài khoản không ở trạng thái hoạt động");
    }
}