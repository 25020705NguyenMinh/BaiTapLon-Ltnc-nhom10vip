package vn.edu.auction.server.service;

import java.math.BigDecimal;

public class BidSuccessEvent {
    private final Long auctionId;
    private final Long bidderId;
    private final BigDecimal amount;

    public BidSuccessEvent(Long auctionId, Long bidderId, BigDecimal amount) {
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.amount = amount;
    }



    public Long getAuctionId() {
        return auctionId;
    }

    public Long getBidderId() {
        return bidderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}