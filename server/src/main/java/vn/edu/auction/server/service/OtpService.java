package vn.edu.auction.server.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.auction.server.entity.OtpEntity;
import vn.edu.auction.server.entity.OtpPurpose;
import vn.edu.auction.server.exception.InvalidOtpException;
import vn.edu.auction.server.exception.OtpCooldownException;
import vn.edu.auction.server.repository.OtpRepository;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;

@Service
public class OtpService {

    // VÌ SAO SecureRandom: Random thường đoán được dãy số kế tiếp, không an toàn cho mã bảo mật
    private static final SecureRandom RANDOM = new SecureRandom();

    private final OtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final long expirationMinutes;
    private final long cooldownSeconds;
    private final int maxAttempts;

    public OtpService(OtpRepository otpRepository,
                      PasswordEncoder passwordEncoder,
                      MailService mailService,
                      @Value("${app.otp.expiration-minutes}") long expirationMinutes,
                      @Value("${app.otp.resend-cooldown-seconds}") long cooldownSeconds,
                      @Value("${app.otp.max-attempts}") int maxAttempts) {
        this.otpRepository = otpRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
        this.expirationMinutes = expirationMinutes;
        this.cooldownSeconds = cooldownSeconds;
        this.maxAttempts = maxAttempts;
    }

    /** Tạo mã 6 số, lưu bản băm, rồi gửi email. */
    @Transactional
    public void generateAndSend(String email, OtpPurpose purpose) {
        Instant now = Instant.now();
        OtpEntity otp = otpRepository.findByEmailAndPurpose(email, purpose).orElse(null);

        // Chống spam: không cho xin mã liên tục (tốn mail, và để kẻ xấu đoán nhiều đợt mã)
        if (otp != null) {
            long waited = Duration.between(otp.getCreatedAt(), now).getSeconds();
            if (waited < cooldownSeconds) {
                throw new OtpCooldownException(cooldownSeconds - waited);
            }
        }

        // %06d để mã luôn đủ 6 chữ số (vd. 004821)
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        String hash = passwordEncoder.encode(code);
        Instant expiresAt = now.plus(Duration.ofMinutes(expirationMinutes));

        // Có dòng cũ thì ghi đè (mã cũ mất hiệu lực ngay), chưa có thì tạo mới
        if (otp == null) {
            otp = new OtpEntity(email, purpose, hash, expiresAt, now);
        } else {
            otp.renew(hash, expiresAt, now);
        }
        otpRepository.save(otp);

        // Gửi mail lỗi sẽ ném exception và rollback, không để lại mã "ma" trong database
        mailService.sendOtp(email, code, expirationMinutes);
    }

    /**
     * Kiểm tra mã. Đúng thì xóa mã (dùng 1 lần). Sai thì ném InvalidOtpException.
     *
     * VÌ SAO có noRollbackFor: mặc định ném RuntimeException sẽ ROLLBACK toàn bộ transaction,
     * khiến việc "tăng số lần sai" không được lưu, và kẻ xấu đoán mã vô hạn.
     * Với noRollbackFor, số lần sai vẫn được ghi dù có ném lỗi.
     */
    @Transactional(noRollbackFor = InvalidOtpException.class)
    public void verify(String email, OtpPurpose purpose, String code) {
        OtpEntity otp = otpRepository.findByEmailAndPurpose(email, purpose)
                .orElseThrow(() -> new InvalidOtpException("Mã OTP không đúng hoặc đã hết hạn"));

        if (Instant.now().isAfter(otp.getExpiresAt())) {
            otpRepository.delete(otp);
            throw new InvalidOtpException("Mã OTP đã hết hạn, vui lòng yêu cầu mã mới");
        }

        if (!passwordEncoder.matches(code, otp.getCodeHash())) {
            otp.increaseAttempts();
            if (otp.getAttempts() >= maxAttempts) {
                otpRepository.delete(otp); // buộc phải xin mã mới
                throw new InvalidOtpException("Bạn đã nhập sai quá nhiều lần, vui lòng yêu cầu mã mới");
            }
            otpRepository.save(otp);
            throw new InvalidOtpException("Mã OTP không đúng. Bạn còn "
                    + (maxAttempts - otp.getAttempts()) + " lần thử");
        }

        otpRepository.delete(otp);
    }
}