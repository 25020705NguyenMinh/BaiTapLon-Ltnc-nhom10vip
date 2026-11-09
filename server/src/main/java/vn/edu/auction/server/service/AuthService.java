package vn.edu.auction.server.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.auction.server.entity.OtpPurpose;
import vn.edu.auction.server.entity.UserEntity;
import vn.edu.auction.server.exception.AccountNotActiveException;
import vn.edu.auction.server.exception.DuplicateEmailException;
import vn.edu.auction.server.exception.InvalidCredentialsException;
import vn.edu.auction.server.exception.InvalidOtpException;
import vn.edu.auction.server.repository.UserRepository;
import vn.edu.auction.server.security.JwtService;
import vn.edu.auction.shared.dto.*;
import vn.edu.auction.shared.enums.UserRole;
import vn.edu.auction.shared.enums.UserStatus;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class AuthService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final Pattern OTP_PATTERN = Pattern.compile("^\\d{6}$");
    private static final int MAX_EMAIL_LENGTH = 255;
    private static final int MAX_NAME_LENGTH = 100;
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_PASSWORD_BYTES = 72;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OtpService otpService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       OtpService otpService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.otpService = otpService;
    }

    // ==================== ĐĂNG KÝ ====================
    /**
     * Quy ước: INACTIVE = đã đăng ký nhưng CHƯA xác minh email.
     * Chưa có token: phải nhập đúng OTP (verifyEmail) mới được cấp token.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu đăng ký không được để trống");
        }

        String fullName = validateFullName(request.getFullName());
        String email = validateEmail(request.getEmail());
        validatePassword(request.getPassword());
        UserRole role = resolveRole(request.getRole());
        String passwordHash = passwordEncoder.encode(request.getPassword());

        UserEntity user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            user = new UserEntity(fullName, email, passwordHash, role, UserStatus.INACTIVE);
        } else if (user.getStatus() == UserStatus.INACTIVE) {
            // VÌ SAO không báo trùng: người dùng từng đăng ký nhưng làm mất mã/chưa xác minh.
            // Nếu chặn bằng "email đã tồn tại" họ sẽ bị kẹt vĩnh viễn. Cho đăng ký lại và gửi mã mới.
            user.setFullName(fullName);
            user.setPasswordHash(passwordHash);
            user.setRole(role);
        } else {
            throw new DuplicateEmailException();
        }

        user = userRepository.saveAndFlush(user);
        otpService.generateAndSend(email, OtpPurpose.REGISTER);

        return toResponse(user, null); // token = null cho tới khi xác minh
    }

    // ==================== XÁC MINH EMAIL ====================
    // noRollbackFor: giống OtpService, để số lần nhập sai vẫn được lưu khi ném InvalidOtpException
    @Transactional(noRollbackFor = InvalidOtpException.class)
    public AuthResponse verifyEmail(VerifyOtpRequest request) {
        if (request == null || request.getEmail() == null || request.getOtp() == null) {
            throw new IllegalArgumentException("Email và mã OTP không được để trống");
        }
        String email = normalizeEmail(request.getEmail());
        String otp = request.getOtp().trim();
        if (!OTP_PATTERN.matcher(otp).matches()) {
            throw new IllegalArgumentException("Mã OTP gồm 6 chữ số");
        }

        // Email lạ cũng trả cùng 1 thông báo như mã sai, không để lộ email nào đã đăng ký
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidOtpException("Mã OTP không đúng hoặc đã hết hạn"));

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new IllegalArgumentException("Tài khoản đã được xác minh, hãy đăng nhập");
        }
        if (user.getStatus() == UserStatus.BANNED) {
            throw new AccountNotActiveException();
        }

        otpService.verify(email, OtpPurpose.REGISTER, otp);

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        // Xác minh xong cấp token luôn, người dùng khỏi phải đăng nhập lại
        return toResponse(user, jwtService.generateToken(user));
    }

    // ==================== GỬI LẠI OTP ====================
    @Transactional
    public MessageResponse resendOtp(EmailRequest request) {
        if (request == null || request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email không được để trống");
        }
        String email = normalizeEmail(request.getEmail());

        // Chỉ gửi cho tài khoản đang chờ xác minh. Luôn trả cùng 1 câu để không lộ email nào tồn tại.
        userRepository.findByEmail(email)
                .filter(u -> u.getStatus() == UserStatus.INACTIVE)
                .ifPresent(u -> otpService.generateAndSend(email, OtpPurpose.REGISTER));

        return new MessageResponse("Nếu email đang chờ xác minh, mã OTP mới đã được gửi");
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
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new AccountNotActiveException(
                    "Tài khoản chưa xác minh email. Hãy đăng ký lại để nhận mã OTP mới");
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AccountNotActiveException();
        }
    }

    private AuthResponse toResponse(UserEntity user, String token) {
        return new AuthResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                token,
                user.getRole());
    }
    // ==================== GIAI ĐOẠN B: QUÊN MẬT KHẨU ====================
    @Transactional
    public MessageResponse forgotPassword(EmailRequest request) {
        if (request == null || request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email không được để trống");
        }
        String email = normalizeEmail(request.getEmail());

        // Chỉ gửi OTP quên mật khẩu cho tài khoản ACTIVE
        userRepository.findByEmail(email)
                .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                .ifPresent(u -> otpService.generateAndSend(email, OtpPurpose.RESET_PASSWORD));

        return new MessageResponse("Mã OTP đổi mật khẩu đã được gửi đến email của bạn");
    }

    @Transactional(noRollbackFor = InvalidOtpException.class)
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        if (request == null || request.getEmail() == null || request.getOtp() == null || request.getNewPassword() == null) {
            throw new IllegalArgumentException("Thông tin không được để trống");
        }

        String email = normalizeEmail(request.getEmail());
        String otp = request.getOtp().trim();
        validatePassword(request.getNewPassword());

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidOtpException("Mã OTP không đúng hoặc đã hết hạn"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AccountNotActiveException("Tài khoản chưa được kích hoạt hoặc đã bị khóa");
        }

        // Xác minh OTP loại RESET_PASSWORD
        otpService.verify(email, OtpPurpose.RESET_PASSWORD, otp);

        // Cập nhật mật khẩu băm mới
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return new MessageResponse("Đặt lại mật khẩu thành công. Vui lòng đăng nhập");
}

}