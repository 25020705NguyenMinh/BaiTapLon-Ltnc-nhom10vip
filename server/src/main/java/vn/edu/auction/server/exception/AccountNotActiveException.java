package vn.edu.auction.server.exception;

/** Tài khoản không ở trạng thái ACTIVE. */
public class AccountNotActiveException extends RuntimeException {

    public AccountNotActiveException() {
        super("Tài khoản không ở trạng thái hoạt động");
    }

    public AccountNotActiveException(String message) {
        super(message);
    }
}