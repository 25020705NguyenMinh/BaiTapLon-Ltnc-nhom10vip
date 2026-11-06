package vn.edu.auction.server.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.auction.server.exception.InvalidCredentialsException;
import vn.edu.auction.server.service.AuthService;
import vn.edu.auction.shared.dto.AuthResponse;
import vn.edu.auction.shared.dto.LoginRequest;
import vn.edu.auction.shared.dto.RegisterRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public AuthResponse me(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        if (!authorization.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            throw new InvalidCredentialsException("Header Authorization phải có dạng: Bearer <token>");
        }
        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        return authService.getCurrentUser(token);
    }
}