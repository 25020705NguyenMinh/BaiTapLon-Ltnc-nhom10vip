package vn.edu.auction.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import vn.edu.auction.server.event.AuctionEventPublisher;
import vn.edu.auction.server.event.UserSocketHandler;

/**
 * WebSocket thuần (không STOMP).
 *  /ws/auctions : phát NEW_BID / AUCTION_FINISHED cho mọi client đã đăng nhập.
 *  /ws/user     : thông báo + chat gửi riêng từng người.
 * Cả hai đi qua SecurityConfig nên client phải gửi header Authorization: Bearer lúc handshake.
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final AuctionEventPublisher publisher;
    private final UserSocketHandler userSocket;

    public WebSocketConfig(AuctionEventPublisher publisher, UserSocketHandler userSocket) {
        this.publisher = publisher;
        this.userSocket = userSocket;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(publisher, "/ws/auctions").setAllowedOrigins("*");
        registry.addHandler(userSocket, "/ws/user").setAllowedOrigins("*");
    }
}
