package vn.edu.auction.client.browse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import vn.edu.auction.shared.dto.AuctionResponse;
import vn.edu.auction.shared.dto.BidResponse;
import vn.edu.auction.shared.enums.AuctionStatus;
import vn.edu.auction.shared.enums.BidStatus;

final class DemoBidHistory {
    private DemoBidHistory() {}

    static List<BidResponse> forAuction(AuctionResponse auction) {
        if (auction.getStatus() != AuctionStatus.RUNNING || auction.getHighestBidderId() == null) return List.of();
        BigDecimal difference = auction.getCurrentPrice().subtract(auction.getStartingPrice());
        if (difference.signum() <= 0) return List.of();
        List<BidResponse> result = new ArrayList<>();
        LocalDateTime lastTime = LocalDateTime.now().minusSeconds(15);
        long seconds = Math.max(3, Duration.between(auction.getStartTime(), lastTime).getSeconds());
        for (int i = 1; i <= 3; i++) {
            BigDecimal amount = auction.getStartingPrice().add(difference.multiply(BigDecimal.valueOf(i))
                    .divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP));
            result.add(new BidResponse((long)i, auction.getId(),
                    i == 3 ? auction.getHighestBidderId() : 200L + i, amount,
                    auction.getStartTime().plusSeconds(seconds * i / 3), BidStatus.ACCEPTED, "Demo"));
        }
        return result;
    }
}
