package vn.edu.auction.server.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import vn.edu.auction.shared.enums.AuctionStatus;

public record BidAcceptedEvent(Long auctionId, BigDecimal currentPrice, Long highestBidderId,
                               AuctionStatus status, LocalDateTime endTime) {}
