package vn.edu.auction.server.repository;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import vn.edu.auction.server.entity.ItemEntity;
import vn.edu.auction.shared.enums.AuctionStatus;

public interface ItemRepository extends JpaRepository<ItemEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from ItemEntity i where i.id = :id")
    Optional<ItemEntity> findByIdForUpdate(@Param("id") Long id);
}
