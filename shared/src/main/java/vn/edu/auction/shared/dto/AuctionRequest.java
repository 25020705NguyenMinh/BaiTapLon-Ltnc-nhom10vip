package vn.edu.auction.shared.dto;

import java.time.LocalDateTime;

public class AuctionRequest {

    private Long itemId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public AuctionRequest(Long itemId, LocalDateTime startTime, LocalDateTime endTime){
        this.itemId = itemId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public AuctionRequest(){}

    public Long getItemId(){return itemId;}
    public LocalDateTime getStarTime(){return startTime;}
    public LocalDateTime getEndTime(){return endTime;}

    public void setItemId(Long newItemId){this.itemId = newItemId;}
    public void setStartTime(LocalDateTime newStartTime){this.startTime = newStartTime;}
    public void setEndTIme(LocalDateTime newEndTime){this.endTime = newEndTime;}
    
}
