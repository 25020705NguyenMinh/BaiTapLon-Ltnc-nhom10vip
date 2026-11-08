package vn.edu.auction.server.exception;

import org.springframework.http.HttpStatus;

public class AuctionBusinessException extends RuntimeException {
    private final HttpStatus status;

    public AuctionBusinessException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() { return status; }
}
