package vn.edu.auction.server.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bids")
public class BidEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Thuộc về phiên đấu giá nào
    @Column(name = "auction_id", nullable = false)
    private Long auctionId;

    // Ai là người đặt giá lượt này
    @Column(name = "bidder_id", nullable = false)
    private Long bidderId;

    // Số tiền đặt
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    // Thời điểm bấm nút đặt giá
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public BidEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(Long auctionId) {
        this.auctionId = auctionId;
    }

    public Long getBidderId() {
        return bidderId;
    }

    public void setBidderId(Long bidderId) {
        this.bidderId = bidderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}