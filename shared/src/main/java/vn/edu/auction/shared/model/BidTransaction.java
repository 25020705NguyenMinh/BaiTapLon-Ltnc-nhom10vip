package vn.edu.auction.shared.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import vn.edu.auction.shared.enums.BidStatus;

public class BidTransaction {

    private Long id;
    private Long auctionId;
    private Long bidderId;
    private BigDecimal amount;
    private LocalDateTime bidTime;
    private BidStatus status;

    public BidTransaction(Long id, Long auctionId, Long bidderId, BigDecimal amount, LocalDateTime bidTime, BidStatus status){
        this.id = id;
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.amount = amount;
        this.bidTime = bidTime;
        this.status = status;
    }

    public BidTransaction(){}

    public Long getId(){return id;}
    public Long getAuctionId(){return auctionId;}
    public Long getBidderId(){return bidderId;}
    public BigDecimal getAmount(){return amount;}
    public LocalDateTime getBidTime(){return bidTime;}
    public BidStatus getStatus(){return status;}

    public void setId(Long newId){this.id = newId;}
    public void setAuctionId(Long newAuctionId){this.auctionId = newAuctionId;}
    public void setBidderId(Long newBidderId){this.bidderId = newBidderId;}
    public void setAmount(BigDecimal newAmount){this.amount = newAmount;}
    public void setBidTime(LocalDateTime newBiidTime){this.bidTime = newBiidTime;}
    public void setStatus(BidStatus newStatus){this.status = newStatus;}
    
}
