package vn.edu.auction.server.exception;

/** Xin mã mới quá nhanh (chưa hết thời gian chờ). */
public class OtpCooldownException extends RuntimeException {
    public OtpCooldownException(long secondsLeft) {
        super("Vui lòng chờ " + secondsLeft + " giây trước khi yêu cầu mã mới");
    }
}