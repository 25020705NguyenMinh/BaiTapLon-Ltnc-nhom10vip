package vn.edu.auction.server.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import vn.edu.auction.server.entity.AuctionEntity;
import vn.edu.auction.server.event.AuctionEventPublisher;
import vn.edu.auction.server.repository.*;
import vn.edu.auction.shared.enums.*;

@Service
public class AuctionClosingService {
    private static final Logger log = LoggerFactory.getLogger(AuctionClosingService.class);

    private final AuctionRepository auctions;
    private final ItemRepository items;
    private final BidTransactionRepository bids;
    private final AuctionEventPublisher publisher;
    private final Clock clock;
    private final TransactionTemplate tx;

    public AuctionClosingService(AuctionRepository auctions, ItemRepository items,
                                 BidTransactionRepository bids, AuctionEventPublisher publisher,
                                 Clock auctionClock, PlatformTransactionManager txManager) {
        this.auctions = auctions;
        this.items = items;
        this.bids = bids;
        this.publisher = publisher;
        this.clock = auctionClock;
        // Mỗi phiên đóng trong transaction riêng: 1 phiên lỗi không kéo theo phiên khác
        this.tx = new TransactionTemplate(txManager);
    }

    /** Trả về số phiên thực sự vừa được đóng. */
    public int closeExpiredAuctions() {
        List<Long> ids = auctions.findExpiredIds(
                List.of(AuctionStatus.OPEN, AuctionStatus.RUNNING), LocalDateTime.now(clock));
        int closed = 0;
        for (Long id : ids) {
            try {
                if (closeAuction(id)) closed++;
            } catch (RuntimeException e) {
                log.error("Đóng phiên {} thất bại, sẽ thử lại ở lượt sau", id, e);
            }
        }
        return closed;
    }

    /** Idempotent: gọi nhiều lần chỉ lần đầu có tác dụng (return true). */
    public boolean closeAuction(Long auctionId) {
        return Boolean.TRUE.equals(tx.execute(status -> doClose(auctionId)));
    }

    private boolean doClose(Long auctionId) {
        // Khóa ghi: scheduler / nhiều instance / bidder không thể chen vào cùng lúc
        AuctionEntity auction = auctions.findByIdForUpdate(auctionId).orElse(null);
        if (auction == null) return false;

        // Sau khi có khóa mới kiểm tra lại: phiên đã đóng bởi tiến trình khác => bỏ qua
        if (auction.getStatus() != AuctionStatus.OPEN && auction.getStatus() != AuctionStatus.RUNNING) return false;
        // Bid cuối có thể vừa gia hạn endTime (anti-sniping) => chưa hết hạn thật
        if (LocalDateTime.now(clock).isBefore(auction.getEndTime())) return false;

        // Người thắng + giá cuối lấy từ dữ liệu bid ĐÃ LƯU, không tin giá trong bộ nhớ
        bids.findFirstByAuctionIdAndStatusOrderByAmountDescIdAsc(auctionId, BidStatus.ACCEPTED)
                .ifPresentOrElse(top -> {
                    auction.setHighestBidderId(top.getBidderId());
                    auction.setCurrentPrice(top.getAmount());
                    items.findById(auction.getItemId()).ifPresent(item -> {
                        item.setStatus(ItemStatus.SOLD);
                        items.save(item);
                    });
                }, () -> auction.setHighestBidderId(null)); // không có bid => không có người thắng

        auction.setStatus(AuctionStatus.FINISHED); // KHÔNG tự chuyển PAID
        auctions.save(auction);

        publisher.publishAuctionFinished(auction); // chỉ thật sự gửi sau khi commit
        return true;
    }
}