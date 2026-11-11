package vn.edu.auction.server.exception;

public class ResourceNotFoundException extends RuntimeException {

    // Constructor nhận vào câu thông báo lỗi khi không tìm thấy phiên đấu giá/user
    public ResourceNotFoundException(String message) {
        super(message);
    }
}