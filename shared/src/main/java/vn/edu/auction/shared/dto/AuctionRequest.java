package vn.edu.auction.shared.dto;

import java.time.LocalDateTime;

public class AuctionRequest {

    private Long itemId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public AuctionRequest() {
    }

    public AuctionRequest(Long itemId, LocalDateTime startTime,
                          LocalDateTime endTime) {
        this.itemId = itemId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Long getItemId() {
        return itemId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
