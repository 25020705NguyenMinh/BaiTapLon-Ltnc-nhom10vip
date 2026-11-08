package vn.edu.auction.server.exception;

/** Email đã có người đăng ký. */
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException() {
        super("Email này đã được sử dụng");
    }
}