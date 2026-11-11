package vn.edu.auction.server.exception;

import java.time.LocalDateTime;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.http.*;
import org.springframework.dao.*;
import org.springframework.web.bind.annotation.*;
import vn.edu.auction.server.controller.*;
import vn.edu.auction.shared.dto.ApiErrorResponse;

@Order(-1)
@RestControllerAdvice(assignableTypes = {ItemController.class, AuctionController.class, BidController.class,
        AccountController.class, SocialController.class, AdminController.class})
public class AuctionApiExceptionHandler {
    @ExceptionHandler(AuctionBusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusiness(AuctionBusinessException ex, HttpServletRequest request) {
        return response(ex.getStatus(), ex.getMessage(), request);
    }

    @ExceptionHandler({ConcurrencyFailureException.class, DataIntegrityViolationException.class})
    public ResponseEntity<ApiErrorResponse> handleConflict(Exception ex, HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, "Du lieu vua thay doi. Vui long tai lai va thu lai.", request);
    }

    private ResponseEntity<ApiErrorResponse> response(HttpStatus status, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                LocalDateTime.now(), status.value(), status.getReasonPhrase(), message, request.getRequestURI()));
    }
}
