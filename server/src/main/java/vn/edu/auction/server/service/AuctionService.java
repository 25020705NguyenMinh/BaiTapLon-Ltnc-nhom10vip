package vn.edu.auction.server.service;

import java.time.*;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.auction.server.entity.*;
import vn.edu.auction.server.repository.*;
import vn.edu.auction.shared.dto.*;
import vn.edu.auction.shared.enums.*;

@Service
@Transactional(readOnly = true)
public class AuctionService {
    private final AuctionRepository auctions;
    private final ItemRepository items;
    private final AuctionUserService users;
    private final Clock clock;

    public AuctionService(AuctionRepository auctions, ItemRepository items, AuctionUserService users, Clock auctionClock) {
        this.auctions = auctions;
        this.items = items;
        this.users = users;
        this.clock = auctionClock;
    }

    public List<AuctionResponse> getAllAuctions() { return auctions.findAll().stream().map(this::response).toList(); }

    public AuctionResponse getAuctionById(Long id) {
        AuctionEntity auction = auctions.findById(id).orElse(null);
        AuctionRules.require(auction != null, HttpStatus.NOT_FOUND, "Khong tim thay phien dau gia.");
        return response(auction);
    }

    @Transactional
    public AuctionResponse createAuction(AuctionRequest request, Long sellerId) {
        users.requireRole(sellerId, UserRole.SELLER);
        AuctionRules.require(request != null && request.getItemId() != null && request.getStartTime() != null && request.getEndTime() != null,
                HttpStatus.BAD_REQUEST, "Thieu san pham hoac thoi gian dau gia.");
        ItemEntity item = items.findByIdForUpdate(request.getItemId()).orElse(null);
        AuctionRules.require(item != null && item.getStatus() != ItemStatus.REMOVED, HttpStatus.NOT_FOUND, "Khong tim thay san pham.");
        AuctionRules.require(item.getSellerId().equals(sellerId), HttpStatus.FORBIDDEN, "Ban khong so huu san pham nay.");
        AuctionRules.require(!auctions.existsByItemId(item.getId()), HttpStatus.CONFLICT, "San pham da co phien dau gia.");
        LocalDateTime now = LocalDateTime.now(clock);
        AuctionRules.require(request.getEndTime().isAfter(now) && request.getEndTime().isAfter(request.getStartTime()),
                HttpStatus.BAD_REQUEST, "Thoi gian ket thuc khong hop le.");
        AuctionEntity auction = new AuctionEntity();
        auction.setItemId(item.getId());
        auction.setCurrentPrice(item.getStartingPrice());
        auction.setStartTime(request.getStartTime());
        auction.setEndTime(request.getEndTime());
        auction.setStatus(request.getStartTime().isAfter(now) ? AuctionStatus.OPEN : AuctionStatus.RUNNING);
        item.setStatus(ItemStatus.ACTIVE);
        return response(auctions.save(auction));
    }

    private AuctionResponse response(AuctionEntity auction) {
        ItemEntity item = items.findById(auction.getItemId()).orElseThrow();
        return new AuctionResponse(auction.getId(), item.getId(), item.getName(), item.getStartingPrice(),
                auction.getCurrentPrice(), auction.getHighestBidderId(), auction.getStartTime(), auction.getEndTime(), auction.getStatus());
    }
}
