package vn.edu.auction.server.entity;

import jakarta.persistence.*;

/** Người dùng theo dõi một phiên; muted = đã tắt thông báo của phiên đó. */
@Entity
@Table(name = "auction_watches",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "auction_id"}))
public class AuctionWatchEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(name = "user_id", nullable = false) public Long userId;
    @Column(name = "auction_id", nullable = false) public Long auctionId;
    @Column(nullable = false) public boolean muted;
}
