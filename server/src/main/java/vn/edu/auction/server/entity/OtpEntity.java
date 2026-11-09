package vn.edu.auction.server.entity;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Mỗi (email, mục đích) chỉ có TỐI ĐA 1 dòng OTP đang hiệu lực.
 * VÌ SAO lưu codeHash chứ không lưu mã thô: lỡ lộ database thì mã vẫn không bị đọc ra.
 */
@Entity
@Table(name = "otp_codes", indexes = @Index(columnList = "email,purpose"))
public class OtpEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OtpPurpose purpose;

    @Column(name = "code_hash", nullable = false)
    private String codeHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private int attempts; // số lần nhập sai

    public OtpEntity() {}

    public OtpEntity(String email, OtpPurpose purpose, String codeHash,
                     Instant expiresAt, Instant createdAt) {
        this.email = email;
        this.purpose = purpose;
        this.codeHash = codeHash;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.attempts = 0;
    }

    /** Phát mã mới: thay mã cũ và đếm lại số lần sai từ 0. */
    public void renew(String codeHash, Instant expiresAt, Instant createdAt) {
        this.codeHash = codeHash;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.attempts = 0;
    }

    public void increaseAttempts() { this.attempts++; }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public OtpPurpose getPurpose() { return purpose; }
    public String getCodeHash() { return codeHash; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getCreatedAt() { return createdAt; }
    public int getAttempts() { return attempts; }
}