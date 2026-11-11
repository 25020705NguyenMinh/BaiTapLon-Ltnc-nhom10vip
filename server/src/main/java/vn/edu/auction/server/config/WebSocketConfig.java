package vn.edu.auction.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import vn.edu.auction.server.event.AuctionEventPublisher;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final AuctionEventPublisher publisher;

    public WebSocketConfig(AuctionEventPublisher publisher) { this.publisher = publisher; }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(publisher, "/ws/auctions").setAllowedOrigins("*");
    }
}