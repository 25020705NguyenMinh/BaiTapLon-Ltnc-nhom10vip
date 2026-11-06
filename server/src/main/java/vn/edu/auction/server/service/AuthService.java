package vn.edu.auction.server.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.auction.server.entity.UserEntity;
import vn.edu.auction.server.exception.AccountNotActiveException;
import vn.edu.auction.server.exception.DuplicateEmailException;
import vn.edu.auction.server.exception.InvalidCredentialsException;
import vn.edu.auction.server.repository.UserRepository;
import vn.edu.auction.server.security.JwtService;
import vn.edu.auction.shared.dto.AuthResponse;
import vn.edu.auction.shared.dto.LoginRequest;
import vn.edu.auction.shared.dto.RegisterRequest;
import vn.edu.auction.shared.enums.UserRole;
import vn.edu.auction.shared.enums.UserStatus;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class AuthService {

    // Cùng quy tắc với client (AuthController phía JavaFX)
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final int MAX_EMAIL_LENGTH = 255;
    private static final int MAX_NAME_LENGTH = 100;
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_PASSWORD_BYTES = 72; // giới hạn của BCrypt

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // ==================== ĐĂNG KÝ ====================
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu đăng ký không được để trống");
        }

        String fullName = validateFullName(request.getFullName());
        String email = validateEmail(request.getEmail());
        validatePassword(request.getPassword());
        UserRole role = resolveRole(request.getRole());

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException();
        }

        UserEntity user = new UserEntity(
                fullName,
                email,
                passwordEncoder.encode(request.getPassword()), // băm BCrypt, không lưu mật khẩu gốc
                role,
                UserStatus.ACTIVE);

        user = userRepository.saveAndFlush(user);
        return toResponse(user, jwtService.generateToken(user));
    }

    // ==================== ĐĂNG NHẬP ====================
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        if (request == null
                || request.getEmail() == null || request.getEmail().isBlank()
                || request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Email và mật khẩu không được để trống");
        }

        String email = normalizeEmail(request.getEmail());

        // Email không tồn tại và sai mật khẩu dùng chung 1 thông báo (tránh lộ email nào đã đăng ký)
        UserEntity user = userRepository.findByEmail(email)
                .filter(u -> passwordEncoder.matches(request.getPassword(), u.getPasswordHash()))
                .orElseThrow(() -> new InvalidCredentialsException("Email hoặc mật khẩu không đúng"));

        ensureActive(user);
        return toResponse(user, jwtService.generateToken(user));
    }

    // ==================== THÔNG TIN NGƯỜI DÙNG HIỆN TẠI ====================
    @Transactional(readOnly = true)
    public AuthResponse getCurrentUser(String token) {
        Long userId;
        try {
            Claims claims = jwtService.parse(token);
            userId = Long.valueOf(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidCredentialsException("Token không hợp lệ hoặc đã hết hạn");
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidCredentialsException("Token không hợp lệ hoặc đã hết hạn"));

        ensureActive(user);
        return toResponse(user, token);
    }

    // ==================== HÀM HỖ TRỢ ====================
    private String validateFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Họ tên không được để trống");
        }
        String trimmed = fullName.trim();
        if (trimmed.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Họ tên không được dài quá " + MAX_NAME_LENGTH + " ký tự");
        }
        return trimmed;
    }

    private String validateEmail(String rawEmail) {
        if (rawEmail == null || rawEmail.isBlank()) {
            throw new IllegalArgumentException("Email không được để trống");
        }
        String email = normalizeEmail(rawEmail);
        if (email.length() > MAX_EMAIL_LENGTH || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Email không đúng định dạng");
        }
        return email;
    }

    private void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống");
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất " + MIN_PASSWORD_LENGTH + " ký tự");
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
            throw new IllegalArgumentException("Mật khẩu quá dài");
        }
    }

    // Không cho tự đăng ký ADMIN; không gửi role thì mặc định là BIDDER
    private UserRole resolveRole(UserRole role) {
        if (role == null) {
            return UserRole.BIDDER;
        }
        if (role == UserRole.ADMIN) {
            throw new IllegalArgumentException("Không thể đăng ký tài khoản với vai trò này");
        }
        return role;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private void ensureActive(UserEntity user) {
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AccountNotActiveException();
        }
    }

    // Không đưa passwordHash vào response
    private AuthResponse toResponse(UserEntity user, String token) {
        return new AuthResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                token,
                user.getRole().name());
    }
}