package vn.edu.auction.shared.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AuctionResponse {

    private Long id;
    private Long itemId;
    private String itemName;
    private BigDecimal startingPrice;
    private BigDecimal currentPrice;
    private Long highestBidderId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int status;

    public AuctionResponse(Long id, Long itemId, String itemName, BigDecimal startingPrice, BigDecimal currentPrice, Long highestBidderId, LocalDateTime startTime, LocalDateTime endTime, int status){
        this.id = id;
        this.itemId = itemId;
        this.startingPrice = startingPrice;
        this.currentPrice = currentPrice;
        this.highestBidderId = highestBidderId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public AuctionResponse(){}

    public Long getId(){return id;}
    public Long getItemId(){return itemId;}
    public String getItemName(){return itemName;}
    public BigDecimal getStartingPrice(){return startingPrice;}
    public BigDecimal getCurrentPrice(){return currentPrice;}
    public Long getHighestBidderId(){return highestBidderId;}
    public LocalDateTime getStarTime(){return startTime;}
    public LocalDateTime getEndTime(){return endTime;}
    public int getStatus(){return status;}

    public void setId(Long newId){this.id = newId;}
    public void setItemId(Long newItemId){this.itemId = newItemId;}
    public void setStartingPrice(BigDecimal newStartingPrice){this.startingPrice = newStartingPrice;}
    public void setCurrentPrice(BigDecimal newCurrentPrice){this.currentPrice = newCurrentPrice;}
    public void setHighestBidderId(Long newHighestBidderId){this.highestBidderId = newHighestBidderId;}
    public void setStartTime(LocalDateTime newStartTime){this.startTime = newStartTime;}
    public void setEndTIme(LocalDateTime newEndTime){this.endTime = newEndTime;}
    public void setStatus(int newStatus){this.status = newStatus;}
    
}
