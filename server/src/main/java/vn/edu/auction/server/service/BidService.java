package vn.edu.auction.server.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.auction.server.repository.AuctionRepository;
import vn.edu.auction.server.repository.BidRepository;
import vn.edu.auction.server.entity.AuctionEntity;
import vn.edu.auction.server.entity.BidEntity;
import vn.edu.auction.shared.dto.PlaceBidRequest;
import vn.edu.auction.server.exception.BadRequestException;
import vn.edu.auction.server.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class BidService {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final ApplicationEventPublisher eventPublisher;

    public BidService(AuctionRepository auctionRepository, BidRepository bidRepository, ApplicationEventPublisher eventPublisher) {
        this.auctionRepository = auctionRepository;
        this.bidRepository = bidRepository;
        this.eventPublisher = eventPublisher;
    }

    // The bid history and current auction price must commit or roll back together.
    @Transactional
    public void placeBid(Long auctionId, Long bidderId, PlaceBidRequest request) {
        if (auctionId == null || bidderId == null || request == null || request.getAmount() == null
                || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Mức giá đặt phải lớn hơn 0");
        }

        AuctionEntity auction = auctionRepository.findByIdForUpdate(auctionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiên đấu giá với ID: " + auctionId));

        if (!"OPEN".equals(auction.getStatus())
                || !LocalDateTime.now().isBefore(auction.getEndTime())) {
            throw new BadRequestException("Phiên đấu giá này đã đóng hoặc đã hết hạn. Không thể đặt giá nữa.");
        }

        if (Objects.equals(auction.getOwnerId(), bidderId)) {
            throw new BadRequestException("Chơi ăn gian à? Không được tự đẩy giá sản phẩm của chính mình!");
        }

        if (request.getAmount().compareTo(auction.getCurrentPrice()) <= 0) {
            throw new BadRequestException("Mức giá đưa ra (" + request.getAmount() + ") phải cao hơn giá hiện tại (" + auction.getCurrentPrice() + ")");
        }

        BidEntity newBid = new BidEntity();
        newBid.setAuctionId(auctionId);
        newBid.setBidderId(bidderId);
        newBid.setAmount(request.getAmount());
        newBid.setCreatedAt(LocalDateTime.now());
        bidRepository.save(newBid);

        auction.setCurrentPrice(request.getAmount());
        auction.setHighestBidderId(bidderId);
        auctionRepository.save(auction);

        eventPublisher.publishEvent(new BidSuccessEvent(auctionId, bidderId, request.getAmount()));
    }

    public List<BidEntity> getBidHistory(Long auctionId) {
        return bidRepository.findByAuctionIdOrderByCreatedAtDesc(auctionId);
    }
}