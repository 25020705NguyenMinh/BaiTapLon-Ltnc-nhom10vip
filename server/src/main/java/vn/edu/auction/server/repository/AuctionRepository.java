package vn.edu.auction.server.repository;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import vn.edu.auction.server.entity.AuctionEntity;
import vn.edu.auction.shared.enums.AuctionStatus;

public interface AuctionRepository extends JpaRepository<AuctionEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AuctionEntity a where a.id = :id")
    Optional<AuctionEntity> findByIdForUpdate(@Param("id") Long id);

    boolean existsByItemId(Long itemId);
    List<AuctionEntity> findByStatus(AuctionStatus status);

    @Query("select a.id from AuctionEntity a where a.status in :statuses and a.endTime <= :now")
    List<Long> findExpiredIds(@Param("statuses") java.util.Collection<AuctionStatus> statuses,
                              @Param("now") java.time.LocalDateTime now);
}
