package vn.edu.auction.server.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import vn.edu.auction.server.entity.AuctionEntity;
import vn.edu.auction.server.entity.BidEntity;
import vn.edu.auction.server.exception.BadRequestException;
import vn.edu.auction.server.repository.AuctionRepository;
import vn.edu.auction.server.repository.BidRepository;
import vn.edu.auction.shared.dto.PlaceBidRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:auction-test;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=60000",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class BidServiceTest {

    @Autowired
    private BidService bidService;

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private BidRepository bidRepository;

    private Long auctionId;

    @BeforeEach
    void setUp() {
        bidRepository.deleteAll();
        auctionRepository.deleteAll();

        AuctionEntity auction = new AuctionEntity(
                1L, new BigDecimal("100.00"), "OPEN", LocalDateTime.now().plusMinutes(10));
        auctionId = auctionRepository.saveAndFlush(auction).getId();
    }

    @Test
    void concurrentBidsKeepTheHighestAmountAndBidderConsistent() throws InterruptedException {
        int numberOfConcurrentBidders = 100;
        int numberOfThreads = 16;
        BigDecimal expectedHighestBid = new BigDecimal("1090.00");
        CountDownLatch ready = new CountDownLatch(numberOfThreads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch latch = new CountDownLatch(numberOfConcurrentBidders);
        ConcurrentLinkedQueue<Throwable> unexpectedErrors = new ConcurrentLinkedQueue<>();
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        try {
            for (int i = 0; i < numberOfConcurrentBidders; i++) {
                final long bidderId = i + 2L;
                final BigDecimal bidAmount = BigDecimal.valueOf(100L + i * 10L);

                executor.execute(() -> {
                    ready.countDown();
                    try {
                        start.await();
                        bidService.placeBid(auctionId, bidderId, new PlaceBidRequest(bidAmount));
                    } catch (BadRequestException expected) {
                        // Lower bids can become invalid while another transaction holds the lock.
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        unexpectedErrors.add(ex);
                    } catch (Throwable ex) {
                        unexpectedErrors.add(ex);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            assertTrue(ready.await(10, TimeUnit.SECONDS), "Workers did not become ready");
            start.countDown();
            assertTrue(latch.await(60, TimeUnit.SECONDS), "Bid requests did not finish in time");
        } finally {
            start.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS), "Bid workers did not stop");
        }

        assertTrue(unexpectedErrors.isEmpty(), () -> "Unexpected bid errors: " + unexpectedErrors);

        AuctionEntity finalAuction = auctionRepository.findById(auctionId).orElseThrow();
        assertEquals(0, finalAuction.getCurrentPrice().compareTo(expectedHighestBid));
        assertEquals(101L, finalAuction.getHighestBidderId());

        List<BidEntity> bidHistory = bidRepository.findByAuctionIdOrderByCreatedAtDesc(auctionId);
        assertEquals(1, bidHistory.stream()
                .filter(bid -> bid.getAmount().compareTo(expectedHighestBid) == 0)
                .count());
    }
}