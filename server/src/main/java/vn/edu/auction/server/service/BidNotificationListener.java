package vn.edu.auction.server.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import vn.edu.auction.server.service.BidSuccessEvent;

@Component
public class BidNotificationListener {

    // Lắng nghe event đặt giá thành công từ BidService
    // Phase = TransactionPhase.AFTER_COMMIT: Bắt buộc CHỈ CHẠY sau khi DB đã lưu thành công 100%.
    // Nếu ở BidService bị rollback (lỗi DB, hết hạn,...), hàm này sẽ KHÔNG bao giờ được gọi.
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBidSuccess(BidSuccessEvent event) {
        
        // Ví dụ logic bắn thông báo Realtime:
        // System.out.println("Đã commit DB thành công cho Auction ID: " + event.getAuctionId());
        // messagingTemplate.convertAndSend("/topic/auction/" + event.getAuctionId(), event);
        
    }
}