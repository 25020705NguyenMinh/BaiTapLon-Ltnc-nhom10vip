package vn.edu.auction.server.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import vn.edu.auction.server.entity.AuctionEntity;

/**
 * Vừa là WebSocket handler của /ws/auctions (giữ danh sách client), vừa là nơi phát event.
 * Chỉ gửi sau khi transaction COMMIT: rollback thì không client nào nhận event sai.
 */
@Component
public class AuctionEventPublisher extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(AuctionEventPublisher.class);

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper mapper;

    public AuctionEventPublisher(ObjectMapper mapper) { this.mapper = mapper; }

    // ---------- quản lý kết nối ----------
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // Decorator: nhiều thread cùng gửi cho 1 session không bị lỗi
        sessions.put(session.getId(), new ConcurrentWebSocketSessionDecorator(session, 5000, 64 * 1024));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable ex) {
        sessions.remove(session.getId());
    }

    // ---------- phát sự kiện ----------
    /** BidService phát BidAcceptedEvent trong transaction đặt giá; chỉ khi COMMIT xong mới vào đây. */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBidAccepted(BidAcceptedEvent e) {
        broadcast(new AuctionEvent(AuctionEvent.NEW_BID, e.auctionId(), e.currentPrice(),
                e.highestBidderId(), e.status(), e.endTime()));
    }

    /** Dùng khi nơi khác muốn phát NEW_BID trực tiếp (BidService hiện đã đi qua listener ở trên). */
    public void publishNewBid(AuctionEntity auction) {
        AuctionEvent ev = AuctionEvent.from(AuctionEvent.NEW_BID, auction);
        afterCommit(() -> broadcast(ev));
    }

    public void publishAuctionFinished(AuctionEntity auction) {
        AuctionEvent ev = AuctionEvent.from(AuctionEvent.AUCTION_FINISHED, auction);
        afterCommit(() -> broadcast(ev));
    }

    private void afterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { action.run(); }
            });
        } else {
            action.run();
        }
    }

    private void broadcast(AuctionEvent event) {
        final TextMessage message;
        try {
            message = new TextMessage(mapper.writeValueAsString(event));
        } catch (IOException e) {
            log.error("Không serialize được AuctionEvent", e);
            return;
        }
        sessions.values().forEach(s -> {
            try {
                if (s.isOpen()) s.sendMessage(message);
            } catch (Exception e) {
                log.warn("Gửi event thất bại, bỏ session {}", s.getId());
                sessions.remove(s.getId());
            }
        });
    }
}
