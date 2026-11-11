package vn.edu.auction.server.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import vn.edu.auction.server.entity.AuctionEntity;
import vn.edu.auction.shared.enums.AuctionStatus;

public class AuctionEvent {
    public static final String NEW_BID = "NEW_BID";
    public static final String AUCTION_FINISHED = "AUCTION_FINISHED";

    private final String type;
    private final Long auctionId;
    private final BigDecimal currentPrice;
    private final Long highestBidderId;
    private final AuctionStatus status;
    private final LocalDateTime endTime;

    public AuctionEvent(String type, Long auctionId, BigDecimal currentPrice,
                        Long highestBidderId, AuctionStatus status, LocalDateTime endTime) {
        this.type = type;
        this.auctionId = auctionId;
        this.currentPrice = currentPrice;
        this.highestBidderId = highestBidderId;
        this.status = status;
        this.endTime = endTime;
    }

    /** Chụp snapshot tại thời điểm gọi, để sau commit vẫn gửi đúng dữ liệu đã lưu. */
    public static AuctionEvent from(String type, AuctionEntity a) {
        return new AuctionEvent(type, a.getId(), a.getCurrentPrice(),
                a.getHighestBidderId(), a.getStatus(), a.getEndTime());
    }

    public String getType() { return type; }
    public Long getAuctionId() { return auctionId; }
    public BigDecimal getCurrentPrice() { return currentPrice; }
    public Long getHighestBidderId() { return highestBidderId; }
    public AuctionStatus getStatus() { return status; }
    public LocalDateTime getEndTime() { return endTime; }
}