package vn.edu.auction.server.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import vn.edu.auction.shared.dto.ApiErrorResponse;

import java.time.LocalDateTime;

/** Gom mọi lỗi về dạng ApiErrorResponse, không bao giờ trả stack trace cho client. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateEmail(
            DuplicateEmailException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    // Hai request đăng ký cùng email gửi cùng lúc -> database chặn bằng unique
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "Email này đã được sử dụng", request);
    }

    @ExceptionHandler({InvalidCredentialsException.class, MissingRequestHeaderException.class})
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(
            Exception ex, HttpServletRequest request) {
        String message = ex instanceof InvalidCredentialsException
                ? ex.getMessage()
                : "Thiếu thông tin xác thực";
        return build(HttpStatus.UNAUTHORIZED, message, request);
    }

    @ExceptionHandler(AccountNotActiveException.class)
    public ResponseEntity<ApiErrorResponse> handleNotActive(
            AccountNotActiveException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    // Lỗi kiểm tra dữ liệu đầu vào (AuthService ném IllegalArgumentException)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(
            IllegalArgumentException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    // Body rỗng, JSON sai cú pháp, role không có trong enum...
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Dữ liệu gửi lên không hợp lệ", request);
    }
    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidOtp(
            InvalidOtpException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(OtpCooldownException.class)
    public ResponseEntity<ApiErrorResponse> handleOtpCooldown(
            OtpCooldownException ex, HttpServletRequest request) {
        return build(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage(), request);
    }

    @ExceptionHandler(EmailSendException.class)
    public ResponseEntity<ApiErrorResponse> handleEmailSend(
            EmailSendException ex, HttpServletRequest request) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), request);
    }
    // Lỗi còn lại: chỉ ghi log phía server, trả thông báo chung cho client
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleOthers(Exception ex, HttpServletRequest request) {
        // Các lỗi chuẩn của Spring MVC (405, 404, 415...) giữ nguyên mã của chúng
        if (ex instanceof ErrorResponse errorResponse) {
            HttpStatus status = HttpStatus.valueOf(errorResponse.getStatusCode().value());
            return build(status, status.getReasonPhrase(), request);
        }
        log.error("Lỗi không mong muốn tại {}", request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "Đã xảy ra lỗi hệ thống, vui lòng thử lại sau", request);
    }

    private ResponseEntity<ApiErrorResponse> build(
            HttpStatus status, String message, HttpServletRequest request) {
        ApiErrorResponse body = new ApiErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}