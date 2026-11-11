package vn.edu.auction.server.repository;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import vn.edu.auction.server.entity.BidTransactionEntity;
import vn.edu.auction.shared.enums.AuctionStatus;
import vn.edu.auction.shared.enums.BidStatus;

public interface BidTransactionRepository extends JpaRepository<BidTransactionEntity, Long> {
    List<BidTransactionEntity> findByAuctionIdOrderByBidTimeDescIdDesc(Long auctionId);
    List<BidTransactionEntity> findByAuctionIdOrderByBidTimeDesc(Long auctionId);
    Optional<BidTransactionEntity> findFirstByAuctionIdAndStatusOrderByAmountDescIdAsc(
            Long auctionId, BidStatus status);
}
