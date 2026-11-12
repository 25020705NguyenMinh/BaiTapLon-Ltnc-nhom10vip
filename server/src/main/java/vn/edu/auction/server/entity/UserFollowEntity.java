package vn.edu.auction.server.entity;

import jakarta.persistence.*;

/** followerId theo dõi sellerId: sellerId đưa sản phẩm mới lên thì followerId nhận thông báo. */
@Entity
@Table(name = "user_follows",
        uniqueConstraints = @UniqueConstraint(columnNames = {"follower_id", "seller_id"}))
public class UserFollowEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(name = "follower_id", nullable = false) public Long followerId;
    @Column(name = "seller_id", nullable = false) public Long sellerId;
}
