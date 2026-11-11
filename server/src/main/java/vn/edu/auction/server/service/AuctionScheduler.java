package vn.edu.auction.server.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Cứ mỗi giây quét các phiên hết hạn. Đổi chu kỳ bằng app.auction.close-interval-ms. */
@Component
public class AuctionScheduler {
    private final AuctionClosingService closingService;

    public AuctionScheduler(AuctionClosingService closingService) { this.closingService = closingService; }

    @Scheduled(fixedDelayString = "${app.auction.close-interval-ms:1000}")
    public void closeExpired() {
        closingService.closeExpiredAuctions();
    }
}
