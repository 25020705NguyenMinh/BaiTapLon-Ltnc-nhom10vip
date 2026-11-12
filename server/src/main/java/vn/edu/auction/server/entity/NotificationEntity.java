package vn.edu.auction.server.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class NotificationEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(name = "user_id", nullable = false) public Long userId;
    @Column(nullable = false, length = 30) public String type; // NEW_BID | NEW_ITEM | AUCTION_ENDED | ITEM_REVIEW
    @Column(nullable = false, length = 500) public String message;
    @Column(name = "ref_id") public Long refId;
    @Column(name = "is_seen", nullable = false) public boolean seen;
    @Column(name = "created_at", nullable = false) public LocalDateTime createdAt;
}
