package vn.edu.auction.server.exception;

// Cho kế thừa RuntimeException để khi văng lỗi này, Spring Boot tự động Rollback Transaction
public class BadRequestException extends RuntimeException {
    
    // Constructor nhận vào một câu thông báo lỗi (message)
    public BadRequestException(String message) {
        super(message);
    }
}