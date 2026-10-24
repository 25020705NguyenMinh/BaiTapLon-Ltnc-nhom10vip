package vn.edu.auction.shared.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import vn.edu.auction.shared.enums.AuctionStatus;

public class Auction {

    private Long id;
    private Long itemId;
    private BigDecimal currentPrice;
    private Long highestBidderId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private AuctionStatus status;

    public Auction(Long id, Long itemId, BigDecimal currentPrice, Long highestBidderId, LocalDateTime starTime, LocalDateTime endTime, AuctionStatus status){
        this.id = id;
        this.itemId = itemId;
        this.currentPrice = currentPrice;
        this.highestBidderId = highestBidderId;
        this.startTime = starTime;
        this.endTime = endTime;
        this.status = status;
    }

    public Auction(){}

    public Long getId(){return id;}
    public Long getItemId(){return itemId;}
    public BigDecimal getCurrentPrice(){return currentPrice;}
    public Long getHighestBidderId(){return highestBidderId;}
    public LocalDateTime getStartTime(){return startTime;}
    public LocalDateTime getEndTime(){return endTime;}
    public AuctionStatus getStatus(){return status;}

    public void setId(Long newId){this.id = newId;}
    public void setItemId(Long newItemId){this.itemId = newItemId;}
    public void setCurrentPrice(BigDecimal newCurrentPrice){this.currentPrice = newCurrentPrice;}
    public void serHighestBidderId(Long newHighestBidderId){this.highestBidderId = newHighestBidderId;}
    public void setStartTime(LocalDateTime newStarTime){this.startTime = newStarTime;}
    public void setEndTime(LocalDateTime newEndTime){this.endTime = newEndTime;}
    public void setStatus(AuctionStatus newStatus){this.status = newStatus;}
    
}
