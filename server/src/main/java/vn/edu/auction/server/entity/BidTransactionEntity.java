package vn.edu.auction.server.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import vn.edu.auction.shared.enums.*;

@Entity
@Table(name = "bids")
public class BidTransactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "auction_id", nullable = false)
    private Long auctionId;
    @Column(name = "bidder_id", nullable = false)
    private Long bidderId;
    @Column(name = "amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;
    @Column(name = "bid_time", nullable = false)
    private LocalDateTime bidTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BidStatus status;

    public BidTransactionEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAuctionId() { return auctionId; }
    public void setAuctionId(Long auctionId) { this.auctionId = auctionId; }

    public Long getBidderId() { return bidderId; }
    public void setBidderId(Long bidderId) { this.bidderId = bidderId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDateTime getBidTime() { return bidTime; }
    public void setBidTime(LocalDateTime bidTime) { this.bidTime = bidTime; }

    public BidStatus getStatus() { return status; }
    public void setStatus(BidStatus status) { this.status = status; }
}
