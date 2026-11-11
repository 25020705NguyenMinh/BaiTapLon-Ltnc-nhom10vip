package vn.edu.auction.server.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.auction.server.entity.UserFollowEntity;

public interface UserFollowRepository extends JpaRepository<UserFollowEntity, Long> {
    Optional<UserFollowEntity> findByFollowerIdAndSellerId(Long followerId, Long sellerId);
    List<UserFollowEntity> findBySellerId(Long sellerId);
}
