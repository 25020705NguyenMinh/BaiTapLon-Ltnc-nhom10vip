package vn.edu.auction.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import vn.edu.auction.server.entity.AuctionEntity;

public interface AuctionRepository extends JpaRepository<AuctionEntity, Long> {

    //[cite: 1] Yêu cầu 1: Lấy phiên đấu giá có khóa ghi
    // Giải thích nôm na: PESSIMISTIC_WRITE giống như đi vào nhà vệ sinh công cộng và chốt cửa lại.
    // Nếu có 2 người (2 requests) cùng lao vào đặt giá, ai đến trước sẽ khóa cửa (lấy được row trong DB). 
    // Người thứ 2 phải đứng ngoài đợi đến khi người thứ 1 cập nhật giá xong và mở cửa (commit transaction).
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AuctionEntity a WHERE a.id = :id")
    Optional<AuctionEntity> findByIdForUpdate(@Param("id") Long id);
}