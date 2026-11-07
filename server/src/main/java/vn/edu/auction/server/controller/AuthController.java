package vn.edu.auction.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import vn.edu.auction.server.exception.InvalidCredentialsException;
import vn.edu.auction.server.entity.UserEntity;
import vn.edu.auction.shared.enums.UserRole;
import vn.edu.auction.shared.enums.UserStatus;
import vn.edu.auction.server.repository.UserRepository;
import vn.edu.auction.server.security.JwtService;
import vn.edu.auction.server.service.AuthService;
import vn.edu.auction.shared.dto.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String BEARER_PREFIX = "Bearer ";

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    // ===== LƯỒNG XÁC THỰC GOOGLE OAUTH2 (TỐI ƯU LOCAL CALLBACK) =====

    @GetMapping("/oauth2/success")
    public ResponseEntity<Void> handleGoogleSuccess(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        String email = principal.getAttribute("email");
        String name = principal.getAttribute("name");

        UserEntity user = userRepository.findByEmail(email).orElseGet(() -> {
            UserEntity newUser = new UserEntity();
            newUser.setEmail(email);
            newUser.setFullName(name);

            // Dùng quyền BIDDER (vì UserRole không có USER)
            newUser.setRole(UserRole.BIDDER);

            newUser.setStatus(UserStatus.ACTIVE);
            newUser.setPasswordHash("GOOGLE_OAUTH_DUMMY_PASS");

            return userRepository.save(newUser);
        });

        // Truyền trực tiếp object user vào hàm sinh token
        String token = jwtService.generateToken(user);

        // MỚI: Mã hóa tên người dùng để truyền an toàn qua URL (tránh lỗi khoảng trắng/tiếng Việt)
        String encodedName = java.net.URLEncoder.encode(user.getFullName(), java.nio.charset.StandardCharsets.UTF_8);

        // MỚI: Nối thêm tham số name vào cuối redirectUrl
        String redirectUrl = "http://localhost:8081/callback?token=" + token + "&name=" + encodedName;

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, redirectUrl)
                .build();
    }
    // ===== CÁC API AUTHENTICATION TRUYỀN THỐNG =====

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/verify-email")
    public AuthResponse verifyEmail(@RequestBody VerifyOtpRequest request) {
        return authService.verifyEmail(request);
    }

    @PostMapping("/resend-otp")
    public MessageResponse resendOtp(@RequestBody EmailRequest request) {
        return authService.resendOtp(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/forgot-password")
    public MessageResponse forgotPassword(@RequestBody EmailRequest request) {
        return authService.forgotPassword(request);
    }

    @PostMapping("/reset-password")
    public MessageResponse resetPassword(@RequestBody ResetPasswordRequest request) {
        return authService.resetPassword(request);
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