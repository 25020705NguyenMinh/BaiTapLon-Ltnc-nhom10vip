package vn.edu.auction.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import vn.edu.auction.server.entity.BidEntity;

public interface BidRepository extends JpaRepository<BidEntity, Long> {
    
    // Lấy danh sách lịch sử bid của 1 phiên đấu giá, sắp xếp mới nhất xếp lên đầu
    List<BidEntity> findByAuctionIdOrderByCreatedAtDesc(Long auctionId);
}