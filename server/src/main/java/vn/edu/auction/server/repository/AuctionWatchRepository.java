package vn.edu.auction.server.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.auction.server.entity.AuctionWatchEntity;

public interface AuctionWatchRepository extends JpaRepository<AuctionWatchEntity, Long> {
    Optional<AuctionWatchEntity> findByUserIdAndAuctionId(Long userId, Long auctionId);
    List<AuctionWatchEntity> findByAuctionIdAndMutedFalse(Long auctionId);
}
