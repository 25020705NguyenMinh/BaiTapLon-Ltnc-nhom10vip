package vn.edu.auction.shared.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import vn.edu.auction.shared.enums.BidStatus;

public class BidResponse {

    private Long bidId;
    private Long auctionId;
    private Long bidderId;
    private BigDecimal amount;
    private LocalDateTime bidTime;
    private BidStatus status;
    private String message;

    public BidResponse(Long bidId, Long auctionId, Long bidderId, BigDecimal amount, LocalDateTime bidTime, BidStatus status, String message){
        this.bidId = bidId;
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.amount = amount;
        this.bidTime = bidTime;
        this.status = status;
        this.message = message;
    }

    public BidResponse(){}

    public Long getBidId(){return bidId;}
    public Long getAuctionId(){return auctionId;}
    public Long getBidderId(){return bidderId;}
    public BigDecimal getAmount(){return amount;}
    public LocalDateTime getBidTime(){return bidTime;}
    public BidStatus getStatus(){return status;}
    public String getMessage(){return message;}

    public void setBidId(Long newBidId){this.bidId = newBidId;}
    public void setAuctionId(Long newAuctionId){this.auctionId = newAuctionId;}
    public void setBidderId(Long newBidderId){this.bidderId = newBidderId;}
    public void setAmount(BigDecimal newAmount){this.amount = newAmount;}
    public void setBidTime(LocalDateTime newBidTime){this.bidTime = newBidTime;}
    public void setStatus(BidStatus newStatus){this.status = newStatus;}
    public void setMessage(String newMessage){this.message = newMessage;}
    
}
