package vn.edu.auction.server.repository;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import vn.edu.auction.server.entity.BidTransactionEntity;
import vn.edu.auction.shared.enums.AuctionStatus;

public interface BidTransactionRepository extends JpaRepository<BidTransactionEntity, Long> {
    List<BidTransactionEntity> findByAuctionIdOrderByBidTimeDescIdDesc(Long auctionId);
    List<BidTransactionEntity> findByAuctionIdOrderByBidTimeDesc(Long auctionId);

    java.util.Optional<BidTransactionEntity> findFirstByAuctionIdAndStatusOrderByAmountDescIdAsc(
            Long auctionId, vn.edu.auction.shared.enums.BidStatus status);

    long countByAuctionId(Long auctionId);
}
