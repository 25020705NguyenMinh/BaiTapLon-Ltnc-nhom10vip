package vn.edu.auction.server.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Lịch sử giao dịch ví (nạp / rút). Dùng public field cho gọn. */
@Entity
@Table(name = "wallet_transactions")
public class WalletTransactionEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(name = "user_id", nullable = false) public Long userId;
    @Column(nullable = false, length = 20) public String type; // DEPOSIT | WITHDRAW
    @Column(precision = 19, scale = 2, nullable = false) public BigDecimal amount;
    @Column(name = "balance_after", precision = 19, scale = 2, nullable = false) public BigDecimal balanceAfter;
    @Column(name = "created_at", nullable = false) public LocalDateTime createdAt;
}
