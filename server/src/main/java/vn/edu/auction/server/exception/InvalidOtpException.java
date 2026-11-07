package vn.edu.auction.server.exception;

/** Mã OTP sai, hết hạn hoặc nhập sai quá số lần cho phép. */
public class InvalidOtpException extends RuntimeException {
    public InvalidOtpException(String message) {
        super(message);
    }
}