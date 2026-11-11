package vn.edu.auction.server.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// @Entity báo cho Spring biết đây là một bảng (Table) trong Database
@Entity
@Table(name = "auctions")
public class AuctionEntity {

    // Khóa chính (Primary Key), tự động tăng (Auto Increment)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ID của người chủ bài đấu giá (ông bán hàng)
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    // Mức giá hiện tại của sản phẩm
    @Column(name = "current_price", nullable = false)
    private BigDecimal currentPrice;

    // ID của người đang giữ giá cao nhất hiện tại (có thể null nếu chưa ai bid)
    @Column(name = "highest_bidder_id")
    private Long highestBidderId;

    // Trạng thái phiên đấu giá: "OPEN" (đang mở) hoặc "CLOSED" (đã đóng)
    @Column(name = "status", nullable = false)
    private String status;

    // Thời gian phiên đấu giá kết thúc
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    // --- CONSTRUCTOR ---
    public AuctionEntity() {
    }

    public AuctionEntity(Long ownerId, BigDecimal currentPrice, String status, LocalDateTime endTime) {
        this.ownerId = ownerId;
        this.currentPrice = currentPrice;
        this.status = status;
        this.endTime = endTime;
    }

    // --- GETTER VÀ SETTER ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public Long getHighestBidderId() {
        return highestBidderId;
    }

    public void setHighestBidderId(Long highestBidderId) {
        this.highestBidderId = highestBidderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

}