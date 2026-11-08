package vn.edu.auction.server.config;

import java.time.Clock;
import org.springframework.context.annotation.*;

@Configuration
public class AuctionTimeConfig {
    @Bean
    public Clock auctionClock() { return Clock.systemUTC(); }
}
