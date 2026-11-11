package vn.edu.auction.server.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import vn.edu.auction.server.exception.AuctionBusinessException;

final class AuctionPrincipal {
    private AuctionPrincipal() {}

    static Long userId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof Long id)) {
            throw new AuctionBusinessException(HttpStatus.UNAUTHORIZED, "Vui long dang nhap bang JWT.");
        }
        return id;
    }
}
